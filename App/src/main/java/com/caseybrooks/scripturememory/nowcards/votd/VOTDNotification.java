package com.caseybrooks.scripturememory.nowcards.votd;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;

import com.caseybrooks.androidbibletools.basic.AbstractVerse;
import com.caseybrooks.androidbibletools.basic.Bible;
import com.caseybrooks.androidbibletools.widget.IVerseViewListener;
import com.caseybrooks.androidbibletools.widget.LoadState;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.activities.MainActivity;

public class VOTDNotification implements IVerseViewListener {
    Notification notification;
    NotificationManager manager;
    Context context;
    VOTD votd;

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
    }

    public void setAlarm() {
        //Create an alarm to go off at the user-selected time
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, VOTDBroadcasts.VOTDAlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarmManager.setRepeating(
                AlarmManager.RTC,
				VOTDSettings.getNotificationTime(context),
                1000*60*60*24,
                alarmIntent);
    }

	public void cancelAlarm() {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, VOTDBroadcasts.VOTDAlarmReceiver.class);
		PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

		alarmManager.cancel(alarmIntent);
	}

    public VOTDNotification create() {
        votd = new VOTD(context);
        votd.setListener(this);
        votd.loadTodaysVerse();

        return this;
    }

    public VOTDNotification createOffline() {
        onVerseLoaded(null, LoadState.Failed);
        return this;
    }

    public void show() {
        manager.notify(2, notification);
		VOTDSettings.setActive(context, true);
    }

    public void dismiss() {
        manager.cancel(2);
		VOTDSettings.setActive(context, false);
    }


    @Override
    public boolean onBibleLoaded(Bible bible, LoadState loadState) {
        return false;
    }

    @Override
    public boolean onVerseLoaded(final AbstractVerse abstractVerse, final LoadState loadState) {
        NotificationCompat.Builder mb = new NotificationCompat.Builder(context);
        mb.setOngoing(false);
        mb.setAutoCancel(true);
        mb.setSmallIcon(R.drawable.ic_cross);
        mb.setPriority(NotificationCompat.PRIORITY_DEFAULT);

        mb.setLights(context.getResources().getColor(R.color.memorized), 500, 4500);
        mb.setSound(Uri.parse(VOTDSettings.getSound(context)));

        //Opens the dashboard when clicked
        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPI = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mb.setContentIntent(resultPI);

        Intent deleteIntent = new Intent(context, VOTDBroadcasts.VOTDNotificationDismissedReceiver.class);
        PendingIntent deletePI = PendingIntent.getBroadcast(context, 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        mb.setDeleteIntent(deletePI);

        if(abstractVerse != null && loadState != LoadState.Failed) {
            //Stores the verse when the user hits "Save"
            Intent save = new Intent(context, VOTDBroadcasts.VOTDSaveVerseReceiver.class);
            PendingIntent savePI = PendingIntent.getBroadcast(context, 0, save, PendingIntent.FLAG_CANCEL_CURRENT);
            mb.addAction(R.drawable.ic_action_save_dark, "Save Verse", savePI);

            mb.setContentTitle("Verse of the Day");
            mb.setContentText(abstractVerse.getReference().toString());
            mb.setStyle(new NotificationCompat.BigTextStyle().bigText(abstractVerse.getReference().toString() + " - " + abstractVerse.getText()));
        }
        else {
            //VOTD has not been downloaded yet today, so don't add actions

            //Just let the user know that no verse has been downloaded yet
            mb.setContentTitle("Verse of the Day");
            mb.setContentText("Click here to see today's new Scripture!");
        }

        notification = mb.build();
        show();

        return true;
    }
}
