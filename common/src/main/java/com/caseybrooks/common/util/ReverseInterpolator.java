package com.caseybrooks.common.util;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

public class ReverseInterpolator implements Interpolator {

    private Interpolator interpolator;

    public ReverseInterpolator(Class<? extends Interpolator> interpolator) {
        try {
            this.interpolator = (Interpolator) interpolator.newInstance();
        }
        catch(Exception e) {
            e.printStackTrace();
            this.interpolator = new AccelerateInterpolator();
        }
    }

    @Override
    public float getInterpolation(float input) {
        return 1 - interpolator.getInterpolation(input);
    }
}
