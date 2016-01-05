package com.caseybrooks.scripturememory.misc;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.caseybrooks.androidbibletools.io.ABTUtility;
import com.caseybrooks.scripturememory.nowcards.votd.VOTDSettings;

import java.io.File;
import java.util.Calendar;

/**
 * A class that runs in the background at regular intervals and helps to maintain
 * cleanliness of the internal cache. It removes any files that are older than
 * two weeks old, and if possible (i.e. there is an internet connection) will
 * redownload the selected Bible every 5 days.
 */
public class CacheCleaner {

	public static void cleanCache(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		long millisNow = Calendar.getInstance().getTimeInMillis();

		File cache = context.getCacheDir();
		for(File file : cache.listFiles()) {
			//ignore the selectedBible for now
			if(file.getName().equals("selectedBible.xml")) {
				continue;
			}

			//check all other cached files to see if they are out of date, and remove them if so
			else {
				long cacheTime = prefs.getLong(file.getName(), millisNow);
				long cacheTimeout = ABTUtility.CacheTimeout.TwoWeeks.millis;

				if(millisNow - cacheTime >= cacheTimeout) {
					boolean deletedCorrectly = file.delete();
					if(deletedCorrectly) {
						prefs.edit().remove(file.getName()).commit();
					}
				}
			}
		}

		//now that we have cleared the cache, see if we need to update the selectedBible.xml
//		File selectedBible = new File(cache, "selectedBible.xml");
//		if(selectedBible.exists()) {
//			BiblePickerSettings.redownloadBible(context);
//		}

//		new QuickNotification(context, "Clean Cache", "Successfully cleaned cache").show();

	}

//Set an alarm for this cleaner to run everyday, starting when the device boots up
	public static void setAlarm(Context context) {
		//Create an alarm to go off at the user-selected time
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, CacheCleanerAlarmReceiver.class);
		PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

		//we'll use the same time set for the VOTD, since it is more likely that
		//the user will have chosen a time that they will have internet at that time
		long time = VOTDSettings.getNotificationTime(context);

		alarmManager.setRepeating(
				AlarmManager.RTC,
				time,
				1000 * 60 * 60 * 24,
				alarmIntent);
	}

	public static class CacheCleanerAlarmReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			CacheCleaner.cleanCache(context);
		}
	}
}
