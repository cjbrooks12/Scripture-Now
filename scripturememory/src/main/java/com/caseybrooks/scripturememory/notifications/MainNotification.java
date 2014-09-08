package com.caseybrooks.scripturememory.notifications;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.caseybrooks.androidbibletools.enumeration.Flags;
import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.activities.MainActivity;
import com.caseybrooks.scripturememory.data.VersesDatabase;
import com.caseybrooks.scripturememory.data.MetaReceiver;
import com.caseybrooks.scripturememory.data.MetaSettings;

import java.util.EnumSet;

public class MainNotification {
//Data Members
//------------------------------------------------------------------------------

    private static MainNotification instance = null;

	private Notification notification;
    private NotificationManager manager;

	private int id;

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
        instance.id = MetaSettings.getVerseId(context);

        VersesDatabase db = new VersesDatabase(context);

        try {
            db.open();
            Passage verse = db.getEntryAt(instance.id);
            db.close();


            switch (MetaSettings.getVerseDisplayMode(context)) {
                case 0:
                    verse.setFlags(EnumSet.of(Flags.TEXT_NORMAL));
                    break;
                case 1:
                    verse.setFlags(EnumSet.of(Flags.TEXT_DASHES));
                    break;
                case 2:
                    verse.setFlags(EnumSet.of(Flags.TEXT_LETTERS));
                    break;
                case 3:
                    verse.setFlags(EnumSet.of(Flags.TEXT_DASHED_LETTERS));
                    break;
                default:
                    break;
            }

            boolean persists = MetaSettings.getNotificationPersist(context);

            //Opens the dashboard when clicked
            Intent resultIntent = new Intent(context, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPI = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            //Removes the notification when the user hits "Dismiss"
            Intent dismiss = new Intent(context, DismissVerseReceiver.class);
            PendingIntent dismissPI = PendingIntent.getBroadcast(context, 0, dismiss, PendingIntent.FLAG_CANCEL_CURRENT);

            //goes to the next verse when the user hits "Next"
            Intent next = new Intent(MetaReceiver.NEXT_VERSE);
            next.putExtra("notificationId", 1);
            next.putExtra("SQL_ID", MetaSettings.getVerseId(context));
            PendingIntent nextPI = PendingIntent.getBroadcast(context, 0, next, PendingIntent.FLAG_CANCEL_CURRENT);

            //Builds the notification
            NotificationCompat.Builder mb = new NotificationCompat.Builder(context);

            if (persists) {
                mb.setOngoing(true);
                mb.addAction(R.drawable.ic_action_cancel_dark, "Dismiss", dismissPI);
            }
            mb.setSmallIcon(R.drawable.ic_cross);
            mb.setContentTitle(verse.getReference());
            mb.setContentText(verse.getText());
            mb.setPriority(NotificationCompat.PRIORITY_LOW);
            mb.setStyle(new NotificationCompat.BigTextStyle().bigText(verse.getText()));
            mb.setContentIntent(resultPI);
            mb.addAction(R.drawable.ic_action_next_item_dark, "Next", nextPI);
            instance.notification = mb.build();

            return instance;
        }
        catch(SQLException e) {
            new QuickNotification(context, "Error Getting Verse", e.getMessage()).show();
            return instance;
        }
	}
	
	public void show() {
		instance.manager.notify(1, instance.notification);
		if(instance == null) Log.e("null instance", "");
	}
	
	public void dismiss() {
		instance.manager.cancel(1);
	}
	
//Broadcast Receivers relevant to this notification
//------------------------------------------------------------------------------
	
	//Button to dismiss the ongoing notification
    public static class DismissVerseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MetaSettings.putNotificationActive(context, false);
            MainNotification.getInstance().dismiss();
        }
    }
}
