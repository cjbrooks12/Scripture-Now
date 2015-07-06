package com.caseybrooks.scripturememory.nowcards.votd;

import android.content.Context;
import android.preference.PreferenceManager;

public class VOTDSettings {
	public static final String settings_file = "my_settings";

	private static final String PREFIX = "VOTD_";
	private static final String ENABLED = "ENABLED";
	private static final String SOUND = "SOUND";
	private static final String ACTIVE = "ACTIVE";
	private static final String TIME = "TIME";
	private static final String ID = "ID";

	public static boolean isEnabled(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREFIX + ENABLED, false);
	}

	public static boolean isActive(Context context) {
		return context.getSharedPreferences(settings_file, 0).getBoolean(PREFIX + ACTIVE, false);
	}

	public static void setActive(Context context, boolean value) {
		context.getSharedPreferences(settings_file, 0).edit().putBoolean(PREFIX + ACTIVE, value).commit();
	}

	public static String getSound(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(PREFIX + SOUND, "DEFAULT_SOUND");
	}

	public static long getNotificationTime(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getLong(PREFIX + TIME, 0);
	}

	public static void setNotificationTime(Context context, long value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(PREFIX + TIME, value).commit();
	}

	public static int getVOTDId(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getInt(PREFIX + ID, -1);
	}

	public static void setVOTDId(Context context, int value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(PREFIX + ID, value).commit();
	}
}
