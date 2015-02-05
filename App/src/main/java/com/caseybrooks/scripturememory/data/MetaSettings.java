package com.caseybrooks.scripturememory.data;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Pair;

import com.caseybrooks.androidbibletools.enumeration.Version;

public class MetaSettings {
    public static final String settings_file = "my_settings";

    //settings saved through app settings
    public static final String APP_THEME = "APP_THEME";
    public static final String BIBLE_VERSION = "PREF_SELECTED_VERSION";

    //settings set throughout app
    public static final String FIRST_TIME = "FIRST_TIME_V3.0";
    public static final String CURRENT_VERSION = "CURRENT_VERSION";
    public static final String PROMPT_ON_START_INT = "PROMPT_ON_START_INT";

    //the last fragment that was open
    public static final String DRAWER_SELECTED_GROUP = "DRAWER_SELECTED_GROUP";
    public static final String DRAWER_SELECTED_CHILD = "DRAWER_SELECTED_CHILD";

    //the fragment that should be open by default
    public static final String DEFAULT_SCREEN_GROUP = "PREF_DEFAULT_SCREEN_GROUP";
    public static final String DEFAULT_SCREEN_CHILD = "PREF_DEFAULT_SCREEN_CHILD";

	public static final String SORT_BY = "SORT_BY";
    public static final String USER_LEARNED_DRAWER = "USER_LEARNED_DRAWER";

//Display settings
//------------------------------------------------------------------------------
    public static String getAppTheme(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(APP_THEME, "0");
    }

    public static Version getBibleVersion(Context context) {
        String version = PreferenceManager.getDefaultSharedPreferences(context).getString(BIBLE_VERSION, "King James Version");
        return Version.parseVersion(version);
    }

    public static Pair<Integer, Integer> getDefaultScreen(Context context) {
        int group = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(DEFAULT_SCREEN_GROUP, "-1"));
        int child = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(DEFAULT_SCREEN_CHILD, "-1"));

        return new Pair<Integer, Integer>(group, child);
    }

    public static void putDrawerSelection(Context context, int group, int child) {
        context.getSharedPreferences(settings_file, 0).edit().putInt(DRAWER_SELECTED_GROUP, group).commit();
        context.getSharedPreferences(settings_file, 0).edit().putInt(DRAWER_SELECTED_CHILD, child).commit();
    }

    public static Pair<Integer, Integer> getDrawerSelection(Context context) {
        int group = context.getSharedPreferences(settings_file, 0).getInt(DRAWER_SELECTED_GROUP, -1);
        int child = context.getSharedPreferences(settings_file, 0).getInt(DRAWER_SELECTED_CHILD, -1);
        return new Pair<Integer, Integer>(group, child);
    }

//App settings
//------------------------------------------------------------------------------
    public static boolean getFirstTime(Context context) {
        return context.getSharedPreferences(settings_file, 0).getBoolean(FIRST_TIME, true);
    }

    public static void putFirstTime(Context context, boolean value) {
        context.getSharedPreferences(settings_file, 0).edit().putBoolean(FIRST_TIME, value).commit();
    }

    public static int getAppVersion(Context context) {
        return context.getSharedPreferences(settings_file, 0).getInt(CURRENT_VERSION, 1);
    }

    public static void putAppVersion(Context context, int value) {
        context.getSharedPreferences(settings_file, 0).edit().putInt(CURRENT_VERSION, value).commit();
    }

	public static int getPromptOnStart(Context context) {
		return context.getSharedPreferences(settings_file, 0).getInt(PROMPT_ON_START_INT, 1);
	}

	public static void putPromptOnStart(Context context, int value) {
		context.getSharedPreferences(settings_file, 0).edit().putInt(PROMPT_ON_START_INT, value).commit();
	}

	public static int getSortBy(Context context) {
		return context.getSharedPreferences(settings_file, 0).getInt(SORT_BY, 0);
	}

	public static void putSortBy(Context context, int value) {
		context.getSharedPreferences(settings_file, 0).edit().putInt(SORT_BY, value).commit();
	}

    public static boolean getUserLearnedDrawer(Context context) {
        return context.getSharedPreferences(settings_file, 0).getBoolean(USER_LEARNED_DRAWER, false);
    }

    public static void putUserLearnedDrawer(Context context, boolean value) {
        context.getSharedPreferences(settings_file, 0).edit().putBoolean(USER_LEARNED_DRAWER, value).commit();
    }
}
