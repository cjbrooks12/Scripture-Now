package com.caseybrooks.common.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;

public class ArrowDrawableToggle extends TintableImageView {

    private DrawerArrowDrawable arrowDrawable;
    private boolean isOpen;

    public ArrowDrawableToggle(Context context) {
        super(context);

        initialize(null);
    }

    public ArrowDrawableToggle(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(attrs);
    }

    public ArrowDrawableToggle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initialize(attrs);
    }

    private void initialize(AttributeSet attrs) {
        arrowDrawable = new DrawerArrowDrawable(getContext());
        arrowDrawable.setProgress(0f);
        arrowDrawable.setSpinEnabled(true);
        setImageDrawable(arrowDrawable);
        isOpen = false;
    }

    private void setProgress(float progress) {
        if(progress == 1f) {
            arrowDrawable.setVerticalMirror(true);
        }
        else if(progress == 0f) {
            arrowDrawable.setVerticalMirror(false);
        }
        arrowDrawable.setProgress(progress);
    }

    private float getProgress() {
        return arrowDrawable.getProgress();
    }

    /**
     * Set the drawable to its open state, which is the arrow icon
     */
    public void open() {
        setProgress(1f);
        isOpen = true;
    }

    /**
     * Set the drawable to its closed state, which is the hamburger icon
     */
    public void close() {
        setProgress(0f);
        isOpen = false;
    }

    /**
     * animates the drawable to its open state, which is the arrow icon
     */
    public void animateOpen() {
        isOpen = true;
        ValueAnimator anim = ValueAnimator.ofFloat(0.0f, 1.0f);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float slideOffset = (Float) valueAnimator.getAnimatedValue();
                setProgress(slideOffset);
            }
        });
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration(350);
        anim.start();
    }

    /**
     * animates the drawable to its closed state, which is the hamburger icon
     */
    public void animateClose() {
        isOpen = false;
        ValueAnimator anim = ValueAnimator.ofFloat(1.0f, 0.0f);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float slideOffset = (Float) valueAnimator.getAnimatedValue();
                setProgress(slideOffset);
            }
        });
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration(350);
        anim.start();
    }

    public boolean isOpen() {
        return isOpen;
    }
}
