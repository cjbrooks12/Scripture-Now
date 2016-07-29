package com.caseybrooks.common.features.votd;

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
import com.caseybrooks.androidbibletools.providers.votd.VerseOfTheDay;
import com.caseybrooks.common.R;
import com.caseybrooks.common.app.AppSettings;
import com.caseybrooks.common.util.Util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class VerseOfTheDayNotification {

    public static class VerseOfTheDayNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            getNotification(context);
        }
    }

//Scheduled recurring alarms
//--------------------------------------------------------------------------------------------------
    public static void setNotificationAlarm(Context context) {
        Intent notificationIntent = new Intent(context, VerseOfTheDayNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Pair<Integer, Integer> votdTime = AppSettings.getTime(context, "votd_time");

        Calendar nextNotificationTime = Calendar.getInstance();
        nextNotificationTime.setTimeInMillis(Util.getNearestTimeInFuture(votdTime.first, votdTime.second));

        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm", Locale.US);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, nextNotificationTime.getTimeInMillis(), pendingIntent);
        Log.i("VOTDNotification", Util.formatString("Set Verse of the Day notification alarm. Will show at {0}", formatter.format(nextNotificationTime.getTime())));
    }

    public static void cancelNotificationAlarm(Context context) {
        Intent notificationIntent = new Intent(context, VerseOfTheDayNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        Log.i("VOTDNotification", "Cancelled Verse of the Day notification alarm");
    }

    public static void getNotification(final Context context) {

        final VerseOfTheDay verseOfTheDay = new VerseOfTheDay();
        verseOfTheDay.download(new OnResponseListener() {
            @Override
            public void responseFinished(boolean success) {
                Notification.Builder builder = new Notification.Builder(context);
                builder.setOngoing(false);
                builder.setAutoCancel(true);
                builder.setSmallIcon(R.drawable.ic_clock_alert);
                builder.setPriority(Notification.PRIORITY_DEFAULT);
                builder.setContentTitle("Verse of the Day");

                builder.setLights(context.getResources().getColor(R.color.memorized), 500, 4500);
                builder.setSound(Uri.parse(AppSettings.getVOTDSound(context)));

                if(success) {
                    builder.setContentText(Util.formatString("{0}: {1}", verseOfTheDay.getPassage().getReference().toString(), verseOfTheDay.getPassage().getText()));
                }
                else {
                    builder.setContentText("Click here to view today's verse!");
                }

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(0, builder.build());

                Log.i("ScheduledNotification", "Received Verse of the Day notification broadcast");

                setNotificationAlarm(context);
            }
        });
    }
}
