package com.caseybrooks.scripturememory.misc;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.caseybrooks.scripturememory.nowcards.votd.VOTDBroadcasts;
import com.caseybrooks.scripturememory.nowcards.votd.VOTDNotification;
import com.caseybrooks.scripturememory.nowcards.votd.VOTDSettings;

import java.util.Calendar;

/**
 * A class that runs in the background at regular intervals and helps to maintain
 * cleanliness of the internal cache. It removes any files that are older than
 * two weeks old, and if possible (i.e. there is an internet connection) will
 * redownload the selected Bible every 5 days.
 */
public class CacheCleaner {
	Context context;

	public CacheCleaner(Context context) {
		this.context = context;
	}

	public void cleanCache() {

	}

//Set an alarm for this cleaner to run everyday, starting when the device boots up
	public void setAlarm() {
		//Create an alarm to go off at the user-selected time
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, VOTDBroadcasts.VOTDAlarmReceiver.class);
		PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR, 1);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1);

		alarmManager.setRepeating(
				AlarmManager.RTC,
				calendar.getTimeInMillis(),
				1000*60*60*24,
				alarmIntent);
	}

	public static class CacheCleanerAlarmReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(VOTDSettings.isEnabled(context)) {
				VOTDNotification.getInstance(context).create();
			}
		}
	}
}
