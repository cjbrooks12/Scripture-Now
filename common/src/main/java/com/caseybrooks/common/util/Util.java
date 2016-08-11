package com.caseybrooks.common.util;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.FloatRange;
import android.view.Display;
import android.view.animation.DecelerateInterpolator;

import java.util.Calendar;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    public static class Animations {
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

        //slide out right animation
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

    public static class Drawables {
        public static Drawable circle(int color) {
            GradientDrawable circle = new GradientDrawable();
            circle.setColor(Color.WHITE);
            circle.setShape(GradientDrawable.OVAL);
            circle.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
            circle.setSize(16, 16);

            return circle;
        }
    }

    public static String formatString(String message, Object... params) {
        if(params != null && params.length > 0) {
            Pattern pattern = Pattern.compile("\\{\\d+\\}");
            Matcher matcher = pattern.matcher(message);

            int lastIndex = 0;
            String output = "";
            while(matcher.find()) {
                output += message.substring(lastIndex, matcher.start());

                String token = matcher.group();
                int pos = Integer.parseInt(token.substring(1, token.length() - 1));

                if(pos < params.length) {
                    output += params[pos].toString();
                }
                else {
                    output += token;
                }

                lastIndex = matcher.end();
            }

            output += message.substring(lastIndex, message.length());

            return output;
        }
        else {
            return message;
        }
    }

    public static long getNearestTimeInFuture(int hourOfDay, int minute) {
        Calendar now = Calendar.getInstance();
        Calendar futureTime = Calendar.getInstance();

        futureTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        futureTime.set(Calendar.MINUTE, minute);

        if((futureTime.getTimeInMillis() <= now.getTimeInMillis()) || (now.get(Calendar.HOUR_OF_DAY) == hourOfDay && now.get(Calendar.MINUTE) == minute))
            futureTime.add(Calendar.DATE, 1);

        return futureTime.getTimeInMillis();
    }

    public static long getTimeToday(int hourOfDay, int minute) {
        Calendar now = Calendar.getInstance();

        now.set(Calendar.HOUR_OF_DAY, hourOfDay);
        now.set(Calendar.MINUTE, minute);

        return now.getTimeInMillis();
    }

    public static boolean shouldUseLightFont(int backgroundColor) {
        // Counting the perceptive luminance - human eye favors green color...
        double a = 1 - (0.299 * Color.red(backgroundColor) + 0.587 * Color.green(backgroundColor) + 0.114 * Color.blue(backgroundColor)) / 255;

        if(a < 0.5)
            return false;
        else
            return true;
    }

    /**
     * Darken the given color by the given factor. A factor less than 1 will darken, while a factor
     * greater than 1 will lighten.
     */
    public static int lighten(int color, @FloatRange(from = 0.0f) float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= factor;
        return Color.HSVToColor(hsv);
    }

    public static String generateUUID() {
        String uuid = "";

        uuid += Calendar.getInstance().getTimeInMillis() + "-";
        uuid += UUID.randomUUID().toString();

        return uuid;
    }

    public static float dp(Context context, int dp) {
        return context.getResources().getDisplayMetrics().density * dp;
    }
}
