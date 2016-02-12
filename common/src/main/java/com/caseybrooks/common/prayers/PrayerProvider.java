package com.caseybrooks.common.prayers;

import android.content.Context;

import com.caseybrooks.common.R;

import java.util.Calendar;
import java.util.Random;

public class PrayerProvider {
    public static int SUNDAY = 0;
    public static int MONDAY = 1;
    public static int TUESDAY = 2;
    public static int WEDNESDAY = 3;
    public static int THURSDAY = 4;
    public static int FRIDAY = 5;
    public static int SATURDAY = 6;

    public static Prayer getPrayerForDay(Context context, int day) {
        return DummyPrayerProvider.getPrayerForDay(context, day);
    }

    public static Prayer getRandomPrayer(Context context) {
        return DummyPrayerProvider.getRandomPrayer(context);
    }

    private static class DummyPrayerProvider {
        public static Prayer getPrayerForDay(Context context, int day) {
            return new Prayer(
                    context.getResources().getStringArray(R.array.dummy_prayer_titles)[day],
                    context.getResources().getStringArray(R.array.dummy_prayer_descriptions)[day]);
        }

        public static Prayer getRandomPrayer(Context context) {
            int sizeOfArray = context.getResources().getStringArray(R.array.dummy_prayer_titles).length;

            Random rand = new Random(Calendar.getInstance().getTimeInMillis());
            int randomPrayer = rand.nextInt(sizeOfArray);

            return new Prayer(
                    context.getResources().getStringArray(R.array.dummy_prayer_titles)[randomPrayer],
                    context.getResources().getStringArray(R.array.dummy_prayer_descriptions)[randomPrayer]);
        }
    }
}
