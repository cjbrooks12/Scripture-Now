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

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.AppSettings;
import com.caseybrooks.common.util.Util;
import com.caseybrooks.common.features.prayers.RealmPrayer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class ScheduledNotification {

    public static class ScheduledNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(1, getNotification(context));
            Log.i("ScheduledNotification", "Received notification broadcast");

            setNotificationAlarm(context);
        }
    }

//Scheduled recurring alarms
//--------------------------------------------------------------------------------------------------
    public static void setNotificationAlarm(Context context) {
        Intent notificationIntent = new Intent(context, ScheduledNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar now = Calendar.getInstance();

        Pair<Integer, Integer> startTime = AppSettings.getTime(context, "schedule_start");
        Pair<Integer, Integer> endTime = AppSettings.getTime(context, "schedule_end");
        Pair<Integer, Integer> interval = AppSettings.getTime(context, "schedule_interval");

        long millisStartToday = Util.getTimeToday(startTime.first, startTime.second);
        long millisEndToday = Util.getTimeToday(endTime.first, endTime.second);
        long intervalMillis = (interval.first*60 + interval.second)*60*1000;
        long futureMillis = now.getTimeInMillis() + intervalMillis;

        if(!(futureMillis > millisStartToday && futureMillis < millisEndToday)) {
            futureMillis = Util.getNearestTimeInFuture(startTime.first, startTime.second);
        }

        Calendar nextNotificationTime = Calendar.getInstance();
        nextNotificationTime.setTimeInMillis(futureMillis);

        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm", Locale.US);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, futureMillis, pendingIntent);
        Log.i("ScheduledNotification", Util.formatString("Set notification alarm. Start=[{0}:{1}], end=[{2}:{3}], interval=[{4}:{5}]. Will show at {6}",
                startTime.first, startTime.second, endTime.first, endTime.second, interval.first, interval.second, formatter.format(nextNotificationTime.getTime())));
    }

    public static void cancelNotificationAlarm(Context context) {
        Intent notificationIntent = new Intent(context, ScheduledNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        Log.i("ScheduledNotification", "Cancel notification alarm");
    }

    public static Notification getNotification(Context context) {
        Random random = new Random(Calendar.getInstance().getTimeInMillis());

        RealmConfiguration realmConfig = new RealmConfiguration.Builder(context).build();
        Realm realm = Realm.getInstance(realmConfig);
        RealmResults<RealmPrayer>  prayerList = realm.where(RealmPrayer.class).findAll();

        if(prayerList.size() > 0) {
            RealmPrayer prayer = prayerList.get(random.nextInt(prayerList.size()));
            Notification.Builder builder = new Notification.Builder(context);
            builder.setContentTitle("Random RealmPrayer");
            builder.setContentText(prayer.getTitle() + " - " + prayer.getDescription());
            builder.setSmallIcon(R.drawable.ic_clock_alert);
            return builder.build();
        }
        else {
            Notification.Builder builder = new Notification.Builder(context);
            builder.setContentTitle("Random RealmPrayer");
            builder.setContentText("No saved prayers!");
            builder.setSmallIcon(R.drawable.ic_clock_alert);
            return builder.build();
        }
    }
}
