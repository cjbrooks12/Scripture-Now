package com.caseybrooks.common.notifications;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.caseybrooks.common.R;

import java.util.Calendar;

public class StaticNotification {

    public static class AppNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            Notification notification = intent.getParcelableExtra("notification");
            int id = intent.getIntExtra("notification_id", 0);
            notificationManager.notify(id, notification);
            Log.i("ScheduledNotification", "Received notification broadcast");

            setNotificationAlarm(context);
        }
    }

//Schedule recurring alarms
//--------------------------------------------------------------------------------------------------
    public static void setNotificationAlarm(Context context) {
        Intent notificationIntent = new Intent(context, StaticNotification.AppNotificationReceiver.class);
        notificationIntent.putExtra("notification_id", 1);
        notificationIntent.putExtra("notification", getNotification(context, "15 second delay"));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = Calendar.getInstance().getTimeInMillis() + 15*1000;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, futureInMillis, pendingIntent);
        Log.i("ScheduledNotification", "Set notification alarm");
    }

    public static void cancelNotificationAlarm(Context context) {
        Intent notificationIntent = new Intent(context, StaticNotification.AppNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        Log.i("ScheduledNotification", "Cancel notification alarm");
    }

    public static Notification getNotification(Context context, String content) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle("Scheduled Notification");
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.ic_clock_alert);
        return builder.build();
    }
}
