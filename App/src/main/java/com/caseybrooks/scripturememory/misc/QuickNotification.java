package com.caseybrooks.scripturememory.misc;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.activities.MainActivity;

public class QuickNotification {
//Data Members
//------------------------------------------------------------------------------
	Context context;
	Notification notification;
	NotificationManager manager;

//Constructors and Initialization
//------------------------------------------------------------------------------
	public QuickNotification(Context context, String title, String message) {
		this.context = context;

		manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		//Opens the dashboard when clicked
		Intent resultIntent = new Intent(context, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
			stackBuilder.addParentStack(MainActivity.class);
			stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPI = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


		//Builds the notification
		NotificationCompat.Builder mb = new NotificationCompat.Builder(context);

		mb.setOngoing(false);
	    mb.setSmallIcon(R.drawable.ic_cross);
	    mb.setContentTitle(title);
		mb.setContentText(message);
		mb.setPriority(NotificationCompat.PRIORITY_DEFAULT);
		mb.setContentIntent(resultPI);
		notification = mb.build();

		show();
	}

	public QuickNotification show() {
		manager.notify(3, notification);
		return this;
	}

	public QuickNotification dismiss() {
		manager.cancel(3);
		return this;
	}


}

