package com.caseybrooks.common.pickers.biblepicker;

import android.content.Context;

import com.caseybrooks.androidbibletools.basic.Bible;

public class BiblePickerSettings {
	private static final String settings_file = "my_settings";
	private static final String PREFIX = "BIBLEPICKER_";

	public static final String NAME = "NAME";
	public static final String ABBR= "ABBR";

	public static String getSelectedBibleName(Context context) {
		return context.getSharedPreferences(settings_file, 0).getString(PREFIX + NAME, "English Standard Version");
	}

	public static String getSelectedBibleAbbr(Context context) {
		return context.getSharedPreferences(settings_file, 0).getString(PREFIX + ABBR, "ESV");
	}

	public static void setSelectedBible(Context context, Bible bible) {
		context.getSharedPreferences(settings_file, 0).edit()
			.putString(PREFIX + NAME, bible.getName())
			.putString(PREFIX + ABBR, bible.getAbbr()).commit();
	}

//	public static void cacheBiblesList(Context, Document doc) {
//
//	}
}
