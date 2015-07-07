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
		this.manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
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

		//if on lollipop, set the color of the notification icon circle
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mb.setColor(context.getResources().getColor(R.color.primary));
		}

		Main mv = new Main(context);
		if(mv.getMainPassage() != null) {
			//for small notifcation, always use full text
			mb.setContentTitle(mv.getMainPassage().getReference().toString());
			mb.setContentText(mv.getNormalText());

			//if Jelly Bean or later, add additional expanded notification
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				String text;
				if(MainSettings.isTextFull(context)) {
					text = mv.getNormalText();
				}
				else{
					text = mv.getFormattedText();
				}

				notification = mb.build();
				RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_main_verse);

				contentView.setOnClickPendingIntent(R.id.notification_main_previous, MainBroadcasts.getPreviousVersePendingIntent(context));
				contentView.setOnClickPendingIntent(R.id.notification_main_next, MainBroadcasts.getNextVersePendingIntent(context));
				contentView.setOnClickPendingIntent(R.id.notification_main_random, MainBroadcasts.getRandomVersePendingIntent(context));
				contentView.setOnClickPendingIntent(R.id.notification_main_show_full, MainBroadcasts.getTextFullPendingIntent(context));
				contentView.setOnClickPendingIntent(R.id.notification_main_dismiss, MainBroadcasts.getDismissVersePendingIntent(context));

				contentView.setImageViewResource(R.id.notification_main_icon, R.drawable.ic_cross);
				contentView.setTextViewText(R.id.notification_main_reference, mv.getMainPassage().getReference().toString());
				contentView.setTextViewText(R.id.notification_main_verse, text);

				notification.bigContentView = contentView;
			}
			else {
				mb.setContentTitle(mv.getMainPassage().getReference().toString());
				mb.setContentText(mv.getFormattedText());

				notification = mb.build();
			}
		}
		else {
			//no verse is set, so don't add actions

			//Just let the user know that no verse is set
			mb.setContentTitle("No verse set!");
			mb.setContentText("Why don't you try adding some more verses, or start memorizing a different list?");
			notification = mb.build();
		}

		return this;
    }

	public void show() {
		manager.notify(1, notification);
	}

	public void dismiss() {
		manager.cancel(1);
	}

}
