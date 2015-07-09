package com.caseybrooks.common.pickers.biblepicker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.caseybrooks.androidbibletools.basic.Bible;

public class BiblePickerSettings {
	private static final String settings_file = "my_settings";
	private static final String PREFIX = "BIBLEPICKER_";

	public static final String NAME = "NAME";
	public static final String ABBR = "ABBR";
	public static final String LANG = "LANG";

	public static void setSelectedBible(Context context, Bible bible) {
		PreferenceManager.getDefaultSharedPreferences(context).edit()
			.putString(PREFIX + NAME, bible.getName())
			.putString(PREFIX + LANG, bible.getLanguage())
			.putString(PREFIX + ABBR, bible.getAbbreviation()).commit();
	}

	public static Bible getSelectedBible(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		Bible bible = new Bible();
		bible.setName(prefs.getString(PREFIX + NAME, ""));
		bible.setAbbreviation(prefs.getString(PREFIX + ABBR, ""));
		bible.setLanguage(prefs.getString(PREFIX + LANG, ""));

		if(bible.getName().equals("") ||
			bible.getAbbreviation().equals("") ||
			bible.getLanguage().equals(""))
		{
			return new Bible();
		}
		else {
			return bible;
		}
	}
}
