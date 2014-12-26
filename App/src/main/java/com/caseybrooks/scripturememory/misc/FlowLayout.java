package com.caseybrooks.scripturememory.misc;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//https://gist.github.com/hzqtc/7940858

public class FlowLayout extends ViewGroup {
    public static final int DEFAULT_HORIZONTAL_SPACING = 5;
    public static final int DEFAULT_VERTICAL_SPACING = 5;
    private final int horizontalSpacing;
    private final int verticalSpacing;

    private final AdapterObserver observer = new AdapterObserver();
    private List<RowMeasurement> currentRows = Collections.emptyList();
    private ListAdapter adapter;

    private final List<OnClickListener> clickListeners = new LinkedList<OnClickListener>();
    private final List<OnLongClickListener> longClickListeners = new LinkedList<OnLongClickListener>();

    private final OnClickListener clickListener = new OnClickListener() {
        public void onClick(final View view) {
            synchronized (clickListeners) {
                for (final OnClickListener listener : clickListeners) {
                    listener.onClick(view);
                }
            }
        }
    };

    private final OnLongClickListener longClickListener = new OnLongClickListener() {
        public boolean onLongClick(final View view) {
            synchronized (longClickListeners) {
                for (final OnLongClickListener listener : longClickListeners) {
                    listener.onLongClick(view);
                }
            }
            return true;
        }
    };

    public FlowLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        float density = getResources().getDisplayMetrics().density;
        horizontalSpacing = (int) (4 * density);
        verticalSpacing = (int) (4 * density);
    }

    public void setOnItemClickListener(final OnClickListener listener) {
        synchronized (clickListeners) {
            clickListeners.add(listener);
        }
    }

    public void setOnItemLongClickListener(final OnLongClickListener listener) {
        synchronized (longClickListeners) {
            longClickListeners.add(listener);
        }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int maxInternalWidth = MeasureSpec.getSize(widthMeasureSpec) - getHorizontalPadding();
        final int maxInternalHeight = MeasureSpec.getSize(heightMeasureSpec) - getVerticalPadding();
        final List<RowMeasurement> rows = new ArrayList<RowMeasurement>();
        RowMeasurement currentRow = new RowMeasurement(maxInternalWidth, widthMode);
        rows.add(currentRow);
        for (final View child : getLayoutChildren()) {
            final android.view.ViewGroup.LayoutParams childLayoutParams = child.getLayoutParams();
            final int childWidthSpec = createChildMeasureSpec(childLayoutParams.width, maxInternalWidth, widthMode);
            final int childHeightSpec = createChildMeasureSpec(childLayoutParams.height, maxInternalHeight, heightMode);
            child.measure(childWidthSpec, childHeightSpec);
            final int childWidth = child.getMeasuredWidth();
            final int childHeight = child.getMeasuredHeight();
            if (currentRow.wouldExceedMax(childWidth)) {
                currentRow = new RowMeasurement(maxInternalWidth, widthMode);
                rows.add(currentRow);
            }
            currentRow.addChildDimensions(childWidth, childHeight);
        }

        int longestRowWidth = 0;
        int totalRowHeight = 0;
        for (int index = 0; index < rows.size(); index++) {
            final RowMeasurement row = rows.get(index);
            totalRowHeight += row.getHeight();
            if (index < rows.size() - 1) {
                totalRowHeight += verticalSpacing;
            }
            longestRowWidth = Math.max(longestRowWidth, row.getWidth());
        }
        setMeasuredDimension(widthMode == MeasureSpec.EXACTLY ? MeasureSpec.getSize(widthMeasureSpec) : longestRowWidth
                        + getHorizontalPadding(), heightMode == MeasureSpec.EXACTLY ? MeasureSpec.getSize(heightMeasureSpec)
                        : totalRowHeight + getVerticalPadding());
        currentRows = Collections.unmodifiableList(rows);
    }

    private int createChildMeasureSpec(final int childLayoutParam, final int max, final int parentMode) {
        int spec;
        if (childLayoutParam == LayoutParams.FILL_PARENT) {
            spec = MeasureSpec.makeMeasureSpec(max, MeasureSpec.EXACTLY);
        } else if (childLayoutParam == LayoutParams.WRAP_CONTENT) {
            spec = MeasureSpec.makeMeasureSpec(max, parentMode == MeasureSpec.UNSPECIFIED ? MeasureSpec.UNSPECIFIED: MeasureSpec.AT_MOST);
        } else {
            spec = MeasureSpec.makeMeasureSpec(childLayoutParam, MeasureSpec.EXACTLY);
        }
        return spec;
    }

    @Override
    protected void onLayout(final boolean changed, final int leftPosition, final int topPosition,
                    final int rightPosition, final int bottomPosition) {
        final int widthOffset = getMeasuredWidth() - getPaddingRight();
        int x = getPaddingLeft();
        int y = getPaddingTop();

        final Iterator<RowMeasurement> rowIterator = currentRows.iterator();
        RowMeasurement currentRow = rowIterator.next();
        for (final View child : getLayoutChildren()) {
            final int childWidth = child.getMeasuredWidth();
            final int childHeight = child.getMeasuredHeight();
            if (x + childWidth > widthOffset) {
                x = getPaddingLeft();
                y += currentRow.height + verticalSpacing;
                if (rowIterator.hasNext()) {
                    currentRow = rowIterator.next();
                }
            }
            child.layout(x, y, x + childWidth, y + childHeight);
            x += childWidth + horizontalSpacing;
        }
    }

    private List<View> getLayoutChildren() {
        final List<View> children = new ArrayList<View>();
        for (int index = 0; index < getChildCount(); index++) {
            final View child = getChildAt(index);
            if (child.getVisibility() != View.GONE) {
                    children.add(child);
            }
        }
        return children;
    }

    protected int getVerticalPadding() {
        return getPaddingTop() + getPaddingBottom();
    }

    protected int getHorizontalPadding() {
        return getPaddingLeft() + getPaddingRight();
    }

    private final class RowMeasurement {
        private final int maxWidth;
        private final int widthMode;
        private int width;
        private int height;

        public RowMeasurement(final int maxWidth, final int widthMode) {
            this.maxWidth = maxWidth;
            this.widthMode = widthMode;
        }

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }

        public boolean wouldExceedMax(final int childWidth) {
            return widthMode == MeasureSpec.UNSPECIFIED ? false : getNewWidth(childWidth) > maxWidth;
        }

        public void addChildDimensions(final int childWidth, final int childHeight) {
            width = getNewWidth(childWidth);
            height = Math.max(height, childHeight);
        }

        private int getNewWidth(final int childWidth) {
            return width == 0 ? childWidth : width + horizontalSpacing + childWidth;
        }
    }

    public ListAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(final ListAdapter adapter) {
        if (this.adapter != null) {
            this.adapter.unregisterDataSetObserver(observer);
        }
        this.adapter = adapter;
        this.adapter.registerDataSetObserver(observer);

        refresh();
    }

    public void refresh() {
        removeAllViews();

        for (int i = 0; i < adapter.getCount(); i++) {
            final View view = adapter.getView(i, null, this);
            view.setOnClickListener(clickListener);
            view.setOnLongClickListener(longClickListener);
            addView(view);
        }

        this.postInvalidate();
        this.requestLayout();
    }

    public class AdapterObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            refresh();
        }

        @Override
        public void onInvalidated() {

        }
    }
}