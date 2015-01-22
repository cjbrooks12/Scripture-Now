package com.caseybrooks.scripturememory.nowcards.votd;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.activities.MainActivity;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.notifications.QuickNotification;

import java.util.Calendar;

public class VOTDNotification {
    Notification notification;
    NotificationManager manager;
    boolean isActive;
    Context context;

    static VOTDNotification instance = null;

    public static VOTDNotification getInstance(Context context) {
        if(instance == null) {
            instance = new VOTDNotification(context);
        }
        return instance;
    }

    private VOTDNotification(Context context) {
        this.context = context;
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        isActive = false;
    }

    public void setAlarm() {
        //Get the time the alarm should go off
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Calendar storedTime = Calendar.getInstance();
        storedTime.setTimeInMillis(
                preferences.getLong("PREF_VOTD_TIME", storedTime.getTimeInMillis()));

        Calendar alarmTime = Calendar.getInstance();
        alarmTime.set(Calendar.HOUR_OF_DAY, storedTime.get(Calendar.HOUR_OF_DAY));
        alarmTime.set(Calendar.MINUTE, storedTime.get(Calendar.MINUTE));

        if(alarmTime.get(Calendar.HOUR_OF_DAY) < Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            alarmTime.add(Calendar.DATE, 1);
        }
        else if(alarmTime.get(Calendar.HOUR_OF_DAY) == Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            if(alarmTime.get(Calendar.MINUTE) <= Calendar.getInstance().get(Calendar.MINUTE)) {
                alarmTime.add(Calendar.DATE, 1);
            }
        }

        //Create an alarm to go off at that time
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, VOTD.VOTDAlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                alarmTime.getTimeInMillis(),
                1000*60*60*24,
                alarmIntent);
    }

    public VOTDNotification create() {
        VOTD votd = new VOTD(context);

        if(votd.currentVerse != null) {
            int ledColor = context.getResources().getColor(R.color.forest_green);
            Uri ringtone = Uri.parse(MetaSettings.getVOTDSound(context));

            //Opens the dashboard when clicked
            Intent resultIntent = new Intent(context, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPI = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            //Stores the verse when the user hits "Save"
            Intent save = new Intent(context, VOTD.VOTDSaveVerseReceiver.class);
            PendingIntent savePI = PendingIntent.getBroadcast(context, 0, save, PendingIntent.FLAG_CANCEL_CURRENT);

            //Builds the notification
            NotificationCompat.Builder mb = new NotificationCompat.Builder(context);

            mb.setOngoing(false);
            mb.setSmallIcon(R.drawable.ic_cross);
            mb.setContentTitle("Verse of the Day");
            mb.setContentText(votd.currentVerse.getReference().toString());
            mb.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            mb.setStyle(new NotificationCompat.BigTextStyle().bigText(votd.currentVerse.getReference().toString() + " - " + votd.currentVerse.getText()));
            mb.setContentIntent(resultPI);
            mb.setAutoCancel(true);
            mb.addAction(R.drawable.ic_action_save_dark, "Save Verse", savePI);
            mb.setLights(ledColor, 500, 4500);
            notification = mb.build();
            notification.sound = ringtone;
        }
        else {
            new QuickNotification(context, "Verse of the Day", "Click here to see today's new Scripture!");
        }
        return this;
    }

    public boolean isActive() {
        return isActive;
    }

    public VOTDNotification show() {
        manager.notify(2, notification);
        isActive = true;
        return this;
    }

    public VOTDNotification dismiss() {
        manager.cancel(2);
        isActive = false;
        return this;
    }
}
