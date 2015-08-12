package com.caseybrooks.scripturememory.misc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.caseybrooks.scripturememory.nowcards.votd.VOTDNotification;
import com.caseybrooks.scripturememory.nowcards.votd.VOTDSettings;

public class AppOnBootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if(VOTDSettings.isEnabled(context)) {
			VOTDNotification.getInstance(context).setAlarm();
		}

		new CacheCleaner(context).setAlarm();
	}
}
