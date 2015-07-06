package com.caseybrooks.scripturememory.nowcards.votd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.caseybrooks.scripturememory.fragments.DashboardFragment;
import com.caseybrooks.scripturememory.nowcards.main.MainNotification;
import com.caseybrooks.scripturememory.nowcards.main.MainSettings;
import com.caseybrooks.scripturememory.nowcards.main.MainWidget;

public class VOTDBroadcasts {
//Broadcasts to send
//------------------------------------------------------------------------------
	public static void updateAll(Context context) {
		//update dashboard cards
		context.sendBroadcast(new Intent(DashboardFragment.REFRESH));

		//update the widgets: VOTD (obviously), Main because it may be the VOTD
		context.sendBroadcast(new Intent(context, MainWidget.class));
		context.sendBroadcast(new Intent(context, VOTDWidget.class));

		//update the notifications: Main because it may be the VOTD
		if(MainSettings.isActive(context)) {
			MainNotification.getInstance(context).create().show();
		}

		//update the notification to ensure it has the text that is in the database
		if(VOTDSettings.isActive(context)) {
			VOTDNotification.getInstance(context).create().show();
		}
	}

//Broadcasts to be received
//------------------------------------------------------------------------------
	public static class VOTDAlarmReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(VOTDSettings.isEnabled(context)) {
				VOTDNotification.getInstance(context).create().show();
			}
		}
	}

	public static class VOTDSaveVerseReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
//			VOTD votd = new VOTD(context);
//			if(votd.currentVerse != null) {
//				votd.saveVerse();
//				VOTDNotification.getInstance(context).dismiss();
//				new QuickNotification(context, "Verse of the Day", votd.currentVerse.getReference().toString() + " added to list").show();
//			}
		}
	}

	public static class VOTDNotificationDismissedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			VOTDSettings.setActive(context, false);
		}
	}

	//reset the alarm to show daily notification when the device boots
	public static class VOTDBootReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(VOTDSettings.isEnabled(context)) {
				VOTDNotification.getInstance(context).setAlarm();
			}
		}
	}
}
