package com.caseybrooks.common.pickers.biblepicker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.caseybrooks.androidbibletools.basic.Bible;
import com.caseybrooks.androidbibletools.defaults.DefaultBible;
import com.caseybrooks.androidbibletools.providers.abs.ABSBible;
import com.caseybrooks.common.R;
import com.caseybrooks.common.features.Util;

import org.jsoup.nodes.Document;

public class BiblePickerSettings {
	private static final String PREFIX = "BIBLEPICKER_";

	public static final String NAME = "NAME";
	public static final String ABBR = "ABBR";
	public static final String LANG = "LANG";

	public static final String ID = "ID";


	public static void setSelectedBible(Context context, Bible bible) {
		PreferenceManager.getDefaultSharedPreferences(context).edit()
			.putString(PREFIX + NAME, bible.getName())
			.putString(PREFIX + LANG, bible.getLanguage())
			.putString(PREFIX + ABBR, bible.getAbbreviation()).commit();

		if(bible instanceof ABSBible) {
			ABSBible absBible = (ABSBible) bible;

			PreferenceManager.getDefaultSharedPreferences(context).edit()
					.putString(PREFIX + ID, absBible.getId()).commit();
		}
	}

	public static Bible getSelectedBible(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		String name = prefs.getString(PREFIX + NAME, "") + "";
		String abbreviation=  prefs.getString(PREFIX + ABBR, "") + "";
		String language = prefs.getString(PREFIX + LANG, "") + "";
		String id = prefs.getString(PREFIX + ID, "") + "";

		if(name.equalsIgnoreCase("") ||
			abbreviation.equalsIgnoreCase("") ||
			language.equalsIgnoreCase(""))
		{
			return new ABSBible(context.getResources().getString(R.string.bibles_org_key), DefaultBible.defaultBibleId);
		}
		else {
			if(!id.equalsIgnoreCase("")) {
				ABSBible bible = new ABSBible(context.getResources().getString(R.string.bibles_org_key), id);
				bible.setName(name);
				bible.setLanguage(language);
				bible.setAbbreviation(abbreviation);
				return bible;
			}
			else {
				Bible bible = new Bible();
				bible.setName(name);
				bible.setLanguage(language);
				bible.setAbbreviation(abbreviation);
				return bible;
			}
		}
	}

	public static Bible getCachedBible(Context context) {
		Bible bible = getSelectedBible(context);

		if(bible instanceof ABSBible) {
			ABSBible absBible = (ABSBible) bible;

			Document doc = Util.getChachedDocument(context, "selectedBible.xml");

			if(doc != null) {
				absBible.parseDocument(doc);
			}

			return absBible;
		}
		else {
			return bible;
		}
	}
}
