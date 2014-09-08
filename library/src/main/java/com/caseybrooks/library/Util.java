package com.caseybrooks.library;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.view.Display;
import android.view.animation.DecelerateInterpolator;

public class Util {
    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null) && activeNetwork.isConnected();
    }

    public static class Animations {

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        public static Animator slideInLeft(Activity activity) {
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            ObjectAnimator slideInLeft = new ObjectAnimator();
            slideInLeft.setFloatValues(-1 * size.x, 0);
            slideInLeft.setPropertyName("translationX");
            slideInLeft.setDuration(500);
            slideInLeft.setInterpolator(new DecelerateInterpolator());

            return slideInLeft;
        }

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        public static Animator slideOutRight(Activity activity) {
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            ObjectAnimator slideInLeft = new ObjectAnimator();
            slideInLeft.setFloatValues(0, size.x - 1);
            slideInLeft.setPropertyName("translationX");
            slideInLeft.setDuration(500);
            slideInLeft.setInterpolator(new DecelerateInterpolator());

            return slideInLeft;
        }
    }

}
