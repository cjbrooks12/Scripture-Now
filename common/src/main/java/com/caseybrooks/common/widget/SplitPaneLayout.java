package com.caseybrooks.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.Util;
import com.caseybrooks.common.util.SemiCircleDrawable;

public class SplitPaneLayout extends ViewGroup {
    public static final int ORIENTATION_HORIZONTAL = 0;
    public static final int ORIENTATION_VERTICAL = 1;

    private int mOrientation;
    private int mSplitterSize;
    private int mSplitterPosition = Integer.MIN_VALUE;
    private int mShadowSize;

    private Drawable mSplitterThumb;
    private Paint mShadowPaint;

    private Rect mSplitterThumbRect = new Rect();
    private Rect mShadowRect = new Rect();
    private boolean isDragging = false;
    private int distanceFromBottom;

    public SplitPaneLayout(Context context) {
        super(context);
    }

    public SplitPaneLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        extractAttributes(context, attrs);
    }

    public SplitPaneLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        extractAttributes(context, attrs);
    }

    private void extractAttributes(Context context, AttributeSet attrs) {
        mSplitterSize = (int) getResources().getDisplayMetrics().density;
        mShadowSize = (int) getResources().getDisplayMetrics().density * 4;

        if(attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SplitPaneLayout);
            mOrientation = a.getInt(R.styleable.SplitPaneLayout_orientation, ORIENTATION_VERTICAL);

            TypedValue value = a.peekValue(R.styleable.SplitPaneLayout_splitterPosition);
            if(value != null) {
                if(value.type == TypedValue.TYPE_DIMENSION) {
                    mSplitterPosition = (int) (a.getDimension(R.styleable.SplitPaneLayout_splitterPosition, Integer.MIN_VALUE));
                }
            }

            mSplitterThumb = new SemiCircleDrawable(Color.parseColor("#FFCCCCCC"), 90, 180);

            if(mOrientation == ORIENTATION_HORIZONTAL) {
                mShadowPaint = new Paint();
                mShadowPaint.setShader(new LinearGradient(
                        mShadowSize,
                        0,
                        0,
                        0,
                        Color.parseColor("#00000000"), Color.parseColor("#35000000"),
                        Shader.TileMode.CLAMP));
            }
            else {
                mShadowPaint = new Paint();
                mShadowPaint.setShader(new LinearGradient(
                        0,
                        mShadowSize,
                        0,
                        0,
                        Color.parseColor("#35000000"), Color.parseColor("#00000000"),
                        Shader.TileMode.CLAMP));
            }

            a.recycle();
        }
    }

//Measurement methods
//--------------------------------------------------------------------------------------------------

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if(getChildCount() != 2) {
            throw new RuntimeException("SplitPaneLayout must have exactly two child views.");
        }

//        Log.i("SplitPaneLayout", Util.formatString("mSplitterPosition={0}, height={1}, width={2}", mSplitterPosition, heightSize, widthSize));


        if(widthSize > 0 && heightSize > 0) {
            measureVertical(widthSize, heightSize);
        }
    }

    private void measureVertical(int width, int height) {
        if(mSplitterPosition == Integer.MIN_VALUE) {
            mSplitterPosition = height / 2;
        }
        else if(mSplitterPosition < 0) {
            distanceFromBottom = mSplitterPosition;
            mSplitterPosition = height - Math.abs(mSplitterPosition);
        }
        else if(distanceFromBottom < 0) {
            mSplitterPosition = height - Math.abs(distanceFromBottom);
        }

        int widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int child0HeightSpec = MeasureSpec.makeMeasureSpec(mSplitterPosition, MeasureSpec.EXACTLY);
        int child1HeightSpec = MeasureSpec.makeMeasureSpec(height - mSplitterPosition, MeasureSpec.EXACTLY);

        getChildAt(0).measure(widthSpec, child0HeightSpec);
        getChildAt(1).measure(widthSpec, child1HeightSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = r - l;
        int height = b - t;

        layoutVertical(width, height);
    }

    private void layoutVertical(int width, int height) {
//        Log.i("SplitPaneLayout", Util.formatString("mSplitterPosition={0}", mSplitterPosition));

        getChildAt(0).layout(0, 0, width, mSplitterPosition);

        //draw shadow just above splitter line
        mShadowRect.set(
                0,
                mSplitterPosition - mShadowSize,
                width,
                mSplitterPosition);

        //layout thumb to right of splitter line
        int thumbWidth = (mSplitterThumb.getIntrinsicWidth() > 0) ? mSplitterThumb.getIntrinsicWidth() : (int) getResources().getDisplayMetrics().density * 16;
        int thumbHeight = (mSplitterThumb.getIntrinsicHeight() > 0) ? mSplitterThumb.getIntrinsicHeight() : (int) getResources().getDisplayMetrics().density * 32;
        int x = width - thumbWidth;
        int y = mSplitterPosition - (thumbHeight / 2);

        mSplitterThumbRect.set(x, y, x + thumbWidth, y + thumbHeight);

        getChildAt(1).layout(0, mSplitterPosition, width, height);
        distanceFromBottom = 0;
    }

//Handle touches of moving the splitter
//--------------------------------------------------------------------------------------------------
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        if(mSplitterThumbRect.contains(x, y) || mShadowRect.contains(x, y)) {
            return true;
        }
        else {
            return super.onInterceptTouchEvent(event);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch(event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            if(mSplitterThumbRect.contains(x, y) || mShadowRect.contains(x, y)) {
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                isDragging = true;
            }
            break;
        case MotionEvent.ACTION_MOVE:
        case MotionEvent.ACTION_UP:
            if(isDragging) {
                mSplitterPosition = mOrientation == ORIENTATION_HORIZONTAL ? x : y;

                if(mSplitterPosition < 0)
                    mSplitterPosition = 0;
                else if(mSplitterPosition > getHeight())
                    mSplitterPosition = getHeight();

                remeasure();
                requestLayout();
            }
            break;
        }
        return true;
    }

//Drawing splitter components methods
//--------------------------------------------------------------------------------------------------

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        drawSplitterShadow(canvas);
        drawSplitterThumb(canvas);
    }

    private void drawSplitterShadow(Canvas canvas) {
        if(mShadowPaint != null) {
            canvas.save();
            if(mOrientation == ORIENTATION_HORIZONTAL) {
                //TODO: draw shadow for horizontal orientation
            }
            else if(mOrientation == ORIENTATION_VERTICAL) {
                canvas.translate(0, mSplitterPosition - mShadowSize);
                canvas.drawRect(getLeft(), 0, getRight(), mShadowSize, mShadowPaint);

            }
            canvas.restore();
        }
    }

    private void drawSplitterThumb(Canvas canvas) {
        if(mSplitterThumb != null) {
            if(mOrientation == ORIENTATION_HORIZONTAL) {
                //TODO: draw thumb at correct position for horizontal orientation
            }
            else if(mOrientation == ORIENTATION_VERTICAL) {
                mSplitterThumb.setBounds(mSplitterThumbRect);
                mSplitterThumb.draw(canvas);
            }
        }
    }

//Helper methods
//--------------------------------------------------------------------------------------------------

    private void remeasure() {
        measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY)
        );
    }
}
