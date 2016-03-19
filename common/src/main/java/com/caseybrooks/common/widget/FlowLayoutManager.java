package com.caseybrooks.common.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import com.caseybrooks.common.app.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Taken and modified from https://github.com/ApmeM/android-flowlayout to better suit my needs
 *
 * MAJOR MODIFICATIONS:
 *      **DONE** Make this all a one-class layoutmanager, so that it might later be shared in a Gist
 *      -Replace normal view Gravity with Alignment that matches what one would expect from text alignment
 *      **DONE** Make rows all the minimum height necessary rather than evenly spaced throughout the available space
 *      **DONE** Enabled scrolling when the available height is less than the necessary height
 *      -Original did not handle any kind of view recycling, so that is implemented here as well
 */
public class FlowLayoutManager extends RecyclerView.LayoutManager {
    List<LineDefinition> lines = new ArrayList<>();
    List<ViewDefinition> views = new ArrayList<>();
    Context context;

    private int maxLineWidth;
    private int maxLineHeight;

    private int maxScrollAmount;
    private int scrollAmount;

    public FlowLayoutManager(Context context) {
        this.context = context;
    }

    /**
     * Force children to wrap their content so we can fit them into lines
     */
    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);

        scrollAmount = 0;
        views.clear();
        lines.clear();
        for(int i = 0; i < this.getItemCount(); i++) {
            View child = recycler.getViewForPosition(i);
            addView(child);
            measureChildWithMargins(child, 0, 0);

            final RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();

            ViewDefinition view = new ViewDefinition(child);
            view.width = child.getMeasuredWidth();
            view.height = child.getMeasuredHeight();
            view.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, lp.bottomMargin);
            views.add(view);
        }

        maxLineWidth  = this.getWidth() - this.getPaddingRight() - this.getPaddingLeft();
        maxLineHeight = this.getHeight() - this.getPaddingTop() - this.getPaddingBottom();

        fillLines();
        calculateLinesAndChildPosition();

        int contentLength = 0;
        for(int i = 0; i < lines.size(); i++) {
            LineDefinition line = lines.get(i);
            Log.i("FlowLayoutManager", Util.formatString("Line [{0}]: offsetX={1}, width={2}, offsetY={3}, height={4}", i, line.offsetX, line.width, line.offsetY, line.height));
            contentLength = Math.max(contentLength, line.width);
        }

        LineDefinition currentLine = lines.get(lines.size() - 1);
        int contentHeight = currentLine.offsetY + currentLine.height;
        int realControlLength = maxLineWidth;
        int realControlThickness = Math.min(contentHeight, maxLineHeight);

        Log.i("FlowLayoutManager", Util.formatString("realCOntrolThickness={0}, getHeight()={1}, contentHeight={2}", realControlThickness, getHeight(), contentHeight));


        if(getHeight() > contentHeight) {
            maxScrollAmount = 0;
        }
        else {
            maxScrollAmount = getHeight() - contentHeight;
        }

        applyGravityToLines(realControlLength, realControlThickness);
        applyPositionsToLines();
    }

    /**
     * Assign children to lines based on their width
     */
    public void fillLines() {
        LineDefinition currentLine = new LineDefinition();
        lines.add(currentLine);
        final int count = views.size();
        for(int i = 0; i < count; i++) {
            final ViewDefinition child = views.get(i);

            //TODO: add ability for children to force line break here
            if(!currentLine.canFit(child)) {
                currentLine = new LineDefinition();
                lines.add(currentLine);
            }

            currentLine.addView(child);
        }
    }

    /**
     * Calculate where the lines should go, and where the children should go in those lines
     */
    public void calculateLinesAndChildPosition() {
        int previousLineOffsetY = scrollAmount;
        final int linesCount = lines.size();
        for(int i = 0; i < linesCount; i++) {
            final LineDefinition line = lines.get(i);
            line.offsetY = previousLineOffsetY;
            previousLineOffsetY += line.height;
            int prevChildThickness = 0;
            final List<ViewDefinition> childViews = line.getViews();
            final int childCount = childViews.size();
            for(int j = 0; j < childCount; j++) {
                ViewDefinition child = childViews.get(j);
                child.offsetX = prevChildThickness;
                prevChildThickness += child.width + child.getSpacingX();
            }
        }
    }

    private void applyPositionsToLines() {
        for(int i = 0; i < lines.size(); i++) {
            LineDefinition line = lines.get(i);
            List<ViewDefinition> childViews = line.getViews();
            for(int j = 0; j < childViews.size(); j++) {
                final ViewDefinition child = childViews.get(j);
                final View view = child.getView();
                measureChildWithMargins(view, child.width, child.height);

                layoutDecorated(view,
                        this.getPaddingLeft() + line.offsetX + child.offsetX,
                        this.getPaddingTop() + line.offsetY + child.offsetY,
                        this.getPaddingLeft() + line.offsetX + child.offsetX + child.width,
                        this.getPaddingTop() + line.offsetY + child.offsetY + child.height
                );
            }
        }
    }

    public void applyGravityToLines(int realControlLength, int realControlThickness) {
        if(lines.size() <= 0) {
            return;
        }

        LineDefinition lastLine = lines.get(lines.size() - 1);
        int excessThickness = realControlThickness - (lastLine.height + lastLine.offsetY);

        if(excessThickness < 0) {
            excessThickness = 0;
        }

        int excessOffset = 0;
        for(int i = 0; i < lines.size(); i++) {
            final LineDefinition child = lines.get(i);
            int gravity = Gravity.TOP | Gravity.LEFT;
            int extraThickness = Math.round(excessThickness / lines.size());

            final int childLength = child.width;
            final int childThickness = child.height;

            Rect container = new Rect();
            container.top = excessOffset;
            container.left = 0;
            container.right = realControlLength;
            container.bottom = childThickness + extraThickness + excessOffset;

            Rect result = new Rect();
            Gravity.apply(gravity, childLength, childThickness, container, result);

            excessOffset += extraThickness;
            child.offsetX = child.offsetX + result.left;
            child.offsetY = child.offsetY + result.top;
            child.width = result.width();
            child.height = result.height();

            applyGravityToLine(child);
        }
    }

    public void applyGravityToLine(LineDefinition line) {
        final List<ViewDefinition> views = line.getViews();
        final int viewCount = views.size();
        if(viewCount <= 0) {
            return;
        }

        ViewDefinition lastChild = views.get(viewCount - 1);
        int excessLength = line.width - (lastChild.width + lastChild.getSpacingX() + lastChild.offsetX);
        int excessOffset = 0;
        for(int i = 0; i < viewCount; i++) {
            final ViewDefinition child = views.get(i);
            int gravity = Gravity.TOP | Gravity.LEFT;
            int extraLength;
                extraLength = excessLength / viewCount;

            final int childLength = child.width + child.getSpacingX();
            final int childThickness = child.height + child.getSpacingY();

            Rect container = new Rect();
            container.top = 0;
            container.left = excessOffset;
            container.right = childLength + extraLength + excessOffset;
            container.bottom = line.height;

            Rect result = new Rect();
            Gravity.apply(gravity, childLength, childThickness, container, result);

            excessOffset += extraLength;
            child.offsetX = result.left + child.offsetX;
            child.offsetY = (result.top);
            child.width = result.width() - child.getSpacingX();
            child.height = result.height() - child.getSpacingY();
        }
    }

    public class ViewDefinition {
        private final View view;

        public int width;
        public int height;
        public int offsetX;
        public int offsetY;

        private int leftMargin;
        private int topMargin;
        private int rightMargin;
        private int bottomMargin;

        public ViewDefinition(View child) {
            this.view = child;
        }

        public int getSpacingX() {
            return this.leftMargin + this.rightMargin;
        }

        public int getSpacingY() {
            return this.topMargin + this.bottomMargin;
        }

        public View getView() {
            return view;
        }

        public void setMargins(int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
            this.leftMargin = leftMargin;
            this.topMargin = topMargin;
            this.rightMargin = rightMargin;
            this.bottomMargin = bottomMargin;
        }
    }

    public class LineDefinition {
        private final List<ViewDefinition> views = new ArrayList<>();
        public int width;
        public int height;
        public int offsetX;
        public int offsetY;

        public LineDefinition() {
            this.offsetX = 0;
            this.offsetY = 0;
        }

        public void addView(ViewDefinition child) {
            this.addView(this.views.size(), child);
        }

        public void addView(int i, ViewDefinition child) {
            this.views.add(i, child);

            this.width = this.width + child.width + child.getSpacingX();
            this.height = Math.max(this.height, child.height + child.getSpacingY());
        }

        public boolean canFit(ViewDefinition child) {
            return width + child.width + child.getSpacingX() <= maxLineWidth;
        }

        public List<ViewDefinition> getViews() {
            return views;
        }
    }

    @Override
    public boolean canScrollVertically() {
        //We do allow scrolling
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        //content is too short to scroll
        if(maxScrollAmount == 0)
            return 0;

        int actualDy;

        int scrollCase;

        int previousScrollAmount = scrollAmount;
        scrollAmount -= dy;

        //attempting to scroll before the beginning of the items
        if(scrollAmount >= 0) {
            scrollCase = 0;

            actualDy = -previousScrollAmount;
            scrollAmount = 0;
        }
        //attempting to scroll past the end of the items
        else if(scrollAmount <= maxScrollAmount) {
            scrollCase = 1;

            actualDy = maxScrollAmount - previousScrollAmount;
            scrollAmount = maxScrollAmount;
        }
        //handle the actual scrolling
        else {
            scrollCase = 2;

            actualDy = dy;
        }

        Log.i("FlowLayoutManager", Util.formatString("Scrolling: scrollCase={0}, dy={1}, actualDy={2}, maxScrollAmount={3}, scrollAmount={4}", scrollCase, dy, actualDy, maxScrollAmount, scrollAmount));

        calculateLinesAndChildPosition();
        applyPositionsToLines();
        return actualDy;
    }
}
