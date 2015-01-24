package com.caseybrooks.scripturememory.nowcards.main;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.activities.MainActivity;

public class MainNotification {
    private static MainNotification instance = null;

    private Notification notification;
    private NotificationManager manager;

//Constructors and Initialization
//------------------------------------------------------------------------------
    private MainNotification() {
    }

    public static MainNotification getInstance() {
        if(instance == null) instance = new MainNotification();
        return instance;
    }

    public static MainNotification notify(Context context) {
        getInstance();

        instance.manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        MainVerse mv = new MainVerse(context);
        mv.setPassageFormatted();
        Passage verse = mv.passage;

        //Opens the dashboard when clicked
        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPI = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        //Builds the notification
        NotificationCompat.Builder mb = new NotificationCompat.Builder(context);

        //Removes the notification when the user hits "Dismiss"
        Intent dismiss = new Intent(context, MainVerse.DismissVerseReceiver.class);
        PendingIntent dismissPI = PendingIntent.getBroadcast(context, 0, dismiss, PendingIntent.FLAG_CANCEL_CURRENT);
        mb.addAction(R.drawable.ic_action_clear_dark, "Dismiss", dismissPI);

        //goes to the next verse when the user hits "Next"
        if(verse != null) {
            Intent next = new Intent(context, MainVerse.NextVerseReceiver.class);
            PendingIntent nextPI = PendingIntent.getBroadcast(context, 0, next, PendingIntent.FLAG_CANCEL_CURRENT);
            mb.addAction(R.drawable.ic_action_arrow_right_dark, "Next", nextPI);

            mb.setContentTitle(verse.getReference().toString());
            mb.setContentText(verse.getText());
            mb.setStyle(new NotificationCompat.BigTextStyle().bigText(verse.getText()));
        }
        else {
            mb.setContentTitle("No verse set!");
            mb.setContentText("Why don't you try adding some more verses, or start memorizing a different list?");
        }

        mb.setOngoing(true);
        mb.setSmallIcon(R.drawable.ic_cross);
        mb.setPriority(NotificationCompat.PRIORITY_LOW);
        mb.setContentIntent(resultPI);
        instance.notification = mb.build();

        return instance;
    }

    public void show() {
        if(instance != null) instance.manager.notify(1, instance.notification);
    }

    public void dismiss() {
        if(instance != null) instance.manager.cancel(1);
    }

}
