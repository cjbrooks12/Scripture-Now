package com.caseybrooks.scripturememory.nowcards.main;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.caseybrooks.scripturememory.fragments.DashboardFragment;

import java.util.Calendar;

public class MainBroadcasts {

	public static PendingIntent getPreviousVersePendingIntent(Context context) {
		int now = ((int) Calendar.getInstance().getTimeInMillis()) + 1;

		Intent intent = new Intent(context, MainBroadcasts.PreviousVerseReceiver.class);
		intent.setAction(now + "");
		return PendingIntent.getBroadcast(context, now, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}
	public static PendingIntent getNextVersePendingIntent(Context context) {
		int now = ((int) Calendar.getInstance().getTimeInMillis()) + 2;

		Intent intent = new Intent(context, NextVerseReceiver.class);
		intent.setAction(now + "");
		return PendingIntent.getBroadcast(context, now, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}
	public static PendingIntent getRandomVersePendingIntent(Context context) {
		int now = ((int) Calendar.getInstance().getTimeInMillis()) + 3;

		Intent intent = new Intent(context, RandomVerseReceiver.class);
		intent.setAction(now + "");
		return PendingIntent.getBroadcast(context, now, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}
	public static PendingIntent getDismissVersePendingIntent(Context context) {
		int now = ((int) Calendar.getInstance().getTimeInMillis()) + 4;

		Intent intent = new Intent(context, DismissVerseReceiver.class);
		intent.setAction(now + "");
		return PendingIntent.getBroadcast(context, now, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}
	public static PendingIntent getTextFullPendingIntent(Context context) {
		int now = ((int) Calendar.getInstance().getTimeInMillis()) + 5;

		Intent intent = new Intent(context, TextFullReceiver.class);
		intent.setAction(now + "");
		return PendingIntent.getBroadcast(context, now, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}

	public static class PreviousVerseReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Main.getPreviousVerse(context);
			MainNotification.getInstance(context).create().show();
			context.sendBroadcast(new Intent(DashboardFragment.REFRESH));
			context.sendBroadcast(new Intent(context, MainWidget.class));
		}
	}

	public static class NextVerseReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Main.getNextVerse(context);
			MainNotification.getInstance(context).create().show();
			context.sendBroadcast(new Intent(DashboardFragment.REFRESH));
			context.sendBroadcast(new Intent(context, MainWidget.class));
		}
	}

	public static class RandomVerseReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Main.getRandomVerse(context);
			MainNotification.getInstance(context).create().show();
			context.sendBroadcast(new Intent(DashboardFragment.REFRESH));
			context.sendBroadcast(new Intent(context, MainWidget.class));
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
			MainNotification.getInstance(context).create().show();
			context.sendBroadcast(new Intent(context, MainWidget.class));
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
