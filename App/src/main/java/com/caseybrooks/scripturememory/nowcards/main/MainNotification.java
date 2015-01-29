package com.caseybrooks.scripturememory.nowcards.main;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.activities.MainActivity;

public class MainNotification {
	Notification notification;
	NotificationManager manager;
	Context context;

	static MainNotification instance = null;

//Constructors and Initialization
//------------------------------------------------------------------------------
	public static MainNotification getInstance(Context context) {
		if(instance == null) {
			instance = new MainNotification(context);
		}
		return instance;
	}

	private MainNotification(Context context) {
		this.context = context;
		manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

    public MainNotification create() {

		NotificationCompat.Builder mb = new NotificationCompat.Builder(context);
		mb.setOngoing(true);
		mb.setSmallIcon(R.drawable.ic_cross);
		mb.setPriority(NotificationCompat.PRIORITY_LOW);

		//Opens the app when the notification is clicked
		Intent resultIntent = new Intent(context, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPI = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mb.setContentIntent(resultPI);

		MainVerse mv = new MainVerse(context);
		if(mv.passage != null) {

			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				if(MainVerse.isTextFull(context)) {
					mv.setPassageNormal();
				}
				else{
					mv.setPassageFormatted();
				}

				notification = mb.build();
				RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_main);

				//click to cancel
				Intent dismiss = new Intent(context, MainVerse.DismissVerseReceiver.class);
				PendingIntent dismissPI = PendingIntent.getBroadcast(context, 0, dismiss, PendingIntent.FLAG_CANCEL_CURRENT);
				contentView.setOnClickPendingIntent(R.id.notification_main_dismiss, dismissPI);

				//go to the next verse
				Intent nextVerse = new Intent(context, MainVerse.NextVerseReceiver.class);
				PendingIntent nextVersePI = PendingIntent.getBroadcast(context, 0, nextVerse, PendingIntent.FLAG_CANCEL_CURRENT);
				contentView.setOnClickPendingIntent(R.id.notification_main_next, nextVersePI);

				//toggle full and formatted text in notification
				Intent showFull = new Intent(context, MainVerse.TextFullReceiver.class);
				PendingIntent showFullPI = PendingIntent.getBroadcast(context, 0, showFull, PendingIntent.FLAG_CANCEL_CURRENT);
				contentView.setOnClickPendingIntent(R.id.notification_main_show_full, showFullPI);

				contentView.setImageViewResource(R.id.notification_main_icon, R.drawable.ic_cross);
				contentView.setTextViewText(R.id.notification_main_reference, mv.passage.getReference().toString());
				contentView.setTextViewText(R.id.notification_main_verse, mv.passage.getText());

				notification.bigContentView = contentView;
			}
			else {
				mv.setPassageFormatted();
				mb.setContentTitle(mv.passage.getReference().toString());
				mb.setContentText(mv.passage.getText());

				notification = mb.build();
			}


			//goes to the next verse when the user hits "Next"
//			Intent showFull = new Intent(context, MainVerse.TextFullReceiver.class);
//			PendingIntent showFullPI = PendingIntent.getBroadcast(context, 0, showFull, PendingIntent.FLAG_CANCEL_CURRENT);
//			mb.addAction(R.drawable.ic_action_text_format_dark, "Show all", showFullPI);
//
//			//Removes the notification when the user hits "Dismiss"
//			Intent dismiss = new Intent(context, MainVerse.DismissVerseReceiver.class);
//			PendingIntent dismissPI = PendingIntent.getBroadcast(context, 0, dismiss, PendingIntent.FLAG_CANCEL_CURRENT);
//			mb.addAction(R.drawable.ic_action_clear_dark, "Dismiss", dismissPI);
//
//			//goes to the next verse when the user hits "Next"
//			Intent next = new Intent(context, MainVerse.NextVerseReceiver.class);
//			PendingIntent nextPI = PendingIntent.getBroadcast(context, 0, next, PendingIntent.FLAG_CANCEL_CURRENT);
//			mb.addAction(R.drawable.ic_action_arrow_right_dark, "Next", nextPI);
//
//			//set the Reference and the text of the notification
//			mb.setContentTitle(mv.passage.getReference().toString());
//			mb.setContentText(mv.passage.getText());
//			mb.setStyle(new NotificationCompat.BigTextStyle().bigText(mv.passage.getText()));
		}
		else {
			//no verse is set, so don't add actions

			//Just let the user know that no verse is set
			mb.setContentTitle("No verse set!");
			mb.setContentText("Why don't you try adding some more verses, or start memorizing a different list?");
			notification = mb.build();
		}

//		notification = mb.build();

		return this;
    }

	public void show() {
		manager.notify(1, notification);
	}

	public void dismiss() {
		manager.cancel(1);
	}

}
