package com.caseybrooks.common.features;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.view.Display;
import android.view.animation.DecelerateInterpolator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;

public class Util {
    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null) && activeNetwork.isConnected();
    }

	private static String cacheTimestampPrefsFile = "cache_timestamps";

	/**
	 * Writes the given Jsoup document to the applications internal cache,
	 * overwriting any file that already has the given name
	 */
	public static boolean cacheDocument(Context context, Document doc, String filename) {
		try {
			File cacheFile = new File(context.getCacheDir(), filename);

			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(cacheFile), "UTF-8"));
			bufferedWriter.write(doc.toString());
			bufferedWriter.close();

			context.getSharedPreferences(cacheTimestampPrefsFile, 0)
					.edit()
					.putLong(filename, Calendar.getInstance().getTimeInMillis())
					.commit();

			return true;
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
			return false;
		}
	}

	public static Document getChachedDocument(Context context, String filename) {
		try {

			long cachedTime = context
					.getSharedPreferences(cacheTimestampPrefsFile, 0)
					.getLong(filename, 0);

			File cacheFile = new File(context.getCacheDir(), filename);

			if(cachedTime != 0 && cacheFile.exists()) {
				//if this file was cached more than 2 weeks ago, delete the file
				//and remove the preference timestamp. Data is stale and should
				//should not be used.
				if(Calendar.getInstance().getTimeInMillis() - cachedTime >= 1209600000) {
					boolean deletedCorrectly = cacheFile.delete();
					if(deletedCorrectly) {
						context.getSharedPreferences(cacheTimestampPrefsFile, 0)
								.edit().remove(filename);
					}
					return null;
				}
				else {
					return Jsoup.parse(cacheFile, "UTF-8");
				}
			}
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}

		return null;
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

		//slide out right animation
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
}
