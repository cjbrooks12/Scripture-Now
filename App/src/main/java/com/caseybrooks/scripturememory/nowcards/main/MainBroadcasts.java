package com.caseybrooks.scripturememory.nowcards.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.caseybrooks.scripturememory.fragments.DashboardFragment;

public class MainBroadcasts {
	public static void updateAll(Context context) {
		//update all the dashboard cards
		context.sendBroadcast(new Intent(DashboardFragment.REFRESH));

		//update all the widgets
		context.sendBroadcast(new Intent(context, MainWidget.class));

		//update the notification
		if(MainSettings.isActive(context)) {
			MainNotification.getInstance(context).create().show();
		}
	}

	public static class PreviousVerseReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Main mv = new Main(context);
			mv.getPreviousVerse();
			updateAll(context);
		}
	}

	public static class NextVerseReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Main mv = new Main(context);
			mv.getNextVerse();
			updateAll(context);
		}
	}

	public static class RandomVerseReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Main mv = new Main(context);
			mv.getRandomVerse();
			updateAll(context);
		}
	}

	public static class DismissVerseReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			MainSettings.setActive(context, false);
			MainNotification.getInstance(context).dismiss();
		}
	}

	public static class TextFullReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			MainSettings.setTextFull(context, !MainSettings.isTextFull(context));
			updateAll(context);
		}
	}

	public static class MainBootReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(MainSettings.isActive(context)) {
				MainNotification.getInstance(context).create().show();
			}
		}
	}
}
