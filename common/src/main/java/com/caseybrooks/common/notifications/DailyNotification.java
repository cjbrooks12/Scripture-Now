package com.caseybrooks.common.notifications;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Pair;

import com.caseybrooks.common.app.AppSettings;
import com.caseybrooks.common.R;
import com.caseybrooks.common.app.Util;
import com.caseybrooks.common.prayers.Prayer;
import com.caseybrooks.common.prayers.PrayerProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DailyNotification {

    public static class DailyNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0, getNotification(context));
            Log.i("ScheduledNotification", "Received notification broadcast");

            setNotificationAlarm(context);
        }
    }

//Scheduled recurring alarms
//--------------------------------------------------------------------------------------------------
    public static void setNotificationAlarm(Context context) {
        Intent notificationIntent = new Intent(context, DailyNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Pair<Integer, Integer> votdTime = AppSettings.getTime(context, "votd_time");

        Calendar nextNotificationTime = Calendar.getInstance();
        nextNotificationTime.setTimeInMillis(Util.getNearestTimeInFuture(votdTime.first, votdTime.second));

        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm", Locale.US);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, nextNotificationTime.getTimeInMillis(), pendingIntent);
        Log.i("DailyNotification", Util.formatString("Set daily notification alarm. Will show at {0}", formatter.format(nextNotificationTime.getTime())));
    }

    public static void cancelNotificationAlarm(Context context) {
        Intent notificationIntent = new Intent(context, DailyNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        Log.i("DailyNotification", "Cancel notification alarm");
    }

    public static Notification getNotification(Context context) {
        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;

        Prayer prayer = PrayerProvider.getPrayerForDay(context, day);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle("Daily Prayer");
        builder.setContentText(prayer.getTitle() + " - " + prayer.getDescription());
        builder.setSmallIcon(R.drawable.ic_clock_alert);
        return builder.build();
    }
}
