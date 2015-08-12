package com.caseybrooks.scripturememory.nowcards.workingverse;

import android.content.Context;

public class WorkingVerse {
	private static final String settings_file = "my_settings";
	private static final String PREFIX = "WORKINGVERSE_";
	public static final String ID = "ID";

	public static int getWorkingVerseId(Context context) {
		return context.getSharedPreferences(settings_file, 0).getInt(PREFIX + ID, -1);
	}

	public static void setWorkingVerseId(Context context, int value) {
		context.getSharedPreferences(settings_file, 0).edit().putInt(PREFIX + ID, value).commit();
	}
}
