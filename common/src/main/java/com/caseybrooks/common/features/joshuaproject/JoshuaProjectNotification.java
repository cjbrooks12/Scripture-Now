package com.caseybrooks.common.features.joshuaproject;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import com.caseybrooks.androidbibletools.data.OnResponseListener;
import com.caseybrooks.androidbibletools.providers.joshuaproject.JoshuaProject;
import com.caseybrooks.common.R;
import com.caseybrooks.common.app.AppSettings;
import com.caseybrooks.common.util.Util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class JoshuaProjectNotification {

    public static class JoshuaProjectNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            JoshuaProjectNotification.getNotification(context);
            Log.i("JPNotification", "Received Joshua Project notification broadcast");
        }
    }

//Scheduled recurring alarms
//--------------------------------------------------------------------------------------------------
    public static void setNotificationAlarm(Context context) {
        Intent notificationIntent = new Intent(context, JoshuaProjectNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Pair<Integer, Integer> jpTime = AppSettings.getTime(context, "jp_time");

        Calendar nextNotificationTime = Calendar.getInstance();
        nextNotificationTime.setTimeInMillis(Util.getNearestTimeInFuture(jpTime.first, jpTime.second));

        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm", Locale.US);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, nextNotificationTime.getTimeInMillis(), pendingIntent);
        Log.i("JPNotification", Util.formatString("Set Joshua Project notification alarm. Will show at {0}", formatter.format(nextNotificationTime.getTime())));
    }

    public static void cancelNotificationAlarm(Context context) {
        Intent notificationIntent = new Intent(context, JoshuaProjectNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        Log.i("JPNotification", "Cancelled Joshua Project notification alarm");
    }

    public static void getNotification(final Context context) {
        final JoshuaProject joshuaProject = new JoshuaProject();
        joshuaProject.download(new OnResponseListener() {
            @Override
            public void responseFinished(boolean success) {
                Notification.Builder builder = new Notification.Builder(context);
                builder.setOngoing(false);
                builder.setAutoCancel(true);
                builder.setSmallIcon(R.drawable.ic_clock_alert);
                builder.setPriority(Notification.PRIORITY_DEFAULT);
                builder.setContentTitle("Joshua Project");

                builder.setLights(context.getResources().getColor(R.color.memorized), 500, 4500);
                builder.setSound(Uri.parse(AppSettings.getJPSound(context)));

                if(success) {
                    builder.setContentText(Util.formatString("Please pray for the {0} of {1}",
                            joshuaProject.getPeopleNameInCountry(),
                            joshuaProject.getCountry()));
                }
                else {
                    builder.setContentText("Click here to pray for today's unreached people group");
                }

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1, builder.build());

                Log.i("JPNotification", "Showing today's unreached peoples notification");

                setNotificationAlarm(context);
            }
        });
    }
}
