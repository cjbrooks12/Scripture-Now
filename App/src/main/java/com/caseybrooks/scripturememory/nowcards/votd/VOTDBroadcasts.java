package com.caseybrooks.scripturememory.nowcards.votd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.caseybrooks.androidbibletools.basic.AbstractVerse;
import com.caseybrooks.androidbibletools.basic.Bible;
import com.caseybrooks.androidbibletools.widget.IVerseViewListener;
import com.caseybrooks.androidbibletools.widget.LoadState;
import com.caseybrooks.common.Util;
import com.caseybrooks.scripturememory.fragments.DashboardFragment;
import com.caseybrooks.scripturememory.misc.QuickNotification;
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

		//update the notification to ensure it has the text that is in the ic_database
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
                if(Util.isConnected(context)) {
                    VOTDNotification.getInstance(context).create();
                }
                else {
                    VOTDNotification.getInstance(context).createOffline();
                }
			}
		}
	}

	public static class VOTDSaveVerseReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(final Context context, Intent intent) {
			new QuickNotification(context, "Verse of the Day", "saving verse...").show();
			VOTDNotification.getInstance(context).dismiss();

			final VOTD votd = new VOTD(context);
			votd.setListener(new IVerseViewListener() {
				@Override
				public boolean onBibleLoaded(Bible bible, LoadState loadState) {
					return false;
				}

				@Override
				public boolean onVerseLoaded(AbstractVerse abstractVerse, LoadState loadState) {
					votd.saveVerse();
					new QuickNotification(context, "Verse of the Day", abstractVerse.getReference().toString() + " saved").show();
					return false;
				}
			});
			votd.loadTodaysVerse();
		}
	}

	public static class VOTDNotificationDismissedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			VOTDSettings.setActive(context, false);
		}
	}
}
