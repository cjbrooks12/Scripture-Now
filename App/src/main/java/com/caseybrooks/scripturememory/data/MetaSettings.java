package com.caseybrooks.scripturememory.data;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Pair;

import com.caseybrooks.androidbibletools.enumeration.Version;
import com.caseybrooks.scripturememory.R;

public class MetaSettings {
    public static final String settings_file = "my_settings";

    //settings saved through app settings
    public static final String PERSIST_MAIN_NOTIFICATION = "PREF_PERSIST_NOTIFICATION";
    public static final String VOTD_SHOW_NOTIFICATION = "PREF_VOTD_NOTIFICATION";
    public static final String VOTD_NOTIFICATION_SOUND = "PREF_VOTD_SOUND";
    public static final String APP_THEME = "PREF_SELECTED_THEME";
    public static final String BIBLE_VERSION = "PREF_SELECTED_VERSION";

    //settings set throughout app
    public static final String FIRST_TIME = "FIRST_TIME";
    public static final String PROMPT_ON_START_INT = "PROMPT_ON_START_INT";

    public static final String MAIN_NOTIFICATION_ACTIVE = "ACTIVE";

    //the list one is memorizing verses from
    public static final String ACTIVE_LIST_GROUP = "ACTIVE_LIST_GROUP";
    public static final String ACTIVE_LIST_CHILD = "ACTIVE_LIST_CHILD";

    //the last fragment that was open
    public static final String DRAWER_SELECTED_GROUP = "DRAWER_SELECTED_GROUP";
    public static final String DRAWER_SELECTED_CHILD = "DRAWER_SELECTED_CHILD";

    //the fragment that should be open by default
    public static final String DEFAULT_SCREEN_GROUP = "PREF_DEFAULT_SCREEN_GROUP";
    public static final String DEFAULT_SCREEN_CHILD = "PREF_DEFAULT_SCREEN_CHILD";

    public static final String VERSE_ID = "SQL_ID";
    public static final String VERSE_DISPLAY_MODE = "VERSE_DISPLAY_MODE";
    public static final String RANDOMNESS_LEVEL = "RANDOMNESS_LEVEL";
    public static final String RANDOM_OFFSET = "RANDOM_OFFSET";
	public static final String SORT_BY = "SORT_BY";
    public static final String USER_LEARNED_DRAWER = "USER_LEARNED_DRAWER";

//Display settings
//------------------------------------------------------------------------------
    public static int getAppTheme(Context context) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(APP_THEME, "0"));
    }

    public static Version getBibleVersion(Context context) {
        String version = PreferenceManager.getDefaultSharedPreferences(context).getString(BIBLE_VERSION, "King James Version");
        return Version.parseVersion(version);
    }

    public static void putActiveList(Context context, int listType, int listId) {
        context.getSharedPreferences(settings_file, 0).edit().putInt(ACTIVE_LIST_GROUP, listType).commit();
        context.getSharedPreferences(settings_file, 0).edit().putInt(ACTIVE_LIST_CHILD, listId).commit();
    }

    public static Pair<Integer, Integer> getActiveList(Context context) {
        int group = context.getSharedPreferences(settings_file, 0).getInt(ACTIVE_LIST_GROUP, -1);
        int child = context.getSharedPreferences(settings_file, 0).getInt(ACTIVE_LIST_CHILD, -1);
        return new Pair<Integer, Integer>(group, child);
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

//Notification Preferences
//------------------------------------------------------------------------------
    public static boolean getNotificationPersist(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PERSIST_MAIN_NOTIFICATION, true);
    }

    public static boolean getVOTDShow(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(VOTD_SHOW_NOTIFICATION, false);
    }

    public static String getVOTDSound(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(VOTD_NOTIFICATION_SOUND, "DEFAULT_SOUND");
    }

//App settings
//------------------------------------------------------------------------------
    public static boolean getFirstTime(Context context) {
        return context.getSharedPreferences(settings_file, 0).getBoolean(FIRST_TIME, true);
    }

    public static void putFirstTime(Context context, boolean value) {
        context.getSharedPreferences(settings_file, 0).edit().putBoolean(FIRST_TIME, value).commit();
    }

	public static int getPromptOnStart(Context context) {
		return context.getSharedPreferences(settings_file, 0).getInt(PROMPT_ON_START_INT, 1);
	}

	public static void putPromptOnStart(Context context, int value) {
		context.getSharedPreferences(settings_file, 0).edit().putInt(PROMPT_ON_START_INT, value).commit();
	}

    public static int getVerseId(Context context) {
        return context.getSharedPreferences(settings_file, 0).getInt(VERSE_ID, -1);
    }

    public static void putVerseId(Context context, int value) {
        context.getSharedPreferences(settings_file, 0).edit().putInt(VERSE_ID, value).commit();
    }

    public static boolean getNotificationActive(Context context) {
        return context.getSharedPreferences(settings_file, 0).getBoolean(MAIN_NOTIFICATION_ACTIVE, false);
    }

    public static void putNotificationActive(Context context, boolean value) {
        context.getSharedPreferences(settings_file, 0).edit().putBoolean(MAIN_NOTIFICATION_ACTIVE, value).commit();
    }

    public static int getVerseDisplayMode(Context context) {
        return context.getSharedPreferences(settings_file, 0).getInt(VERSE_DISPLAY_MODE, R.id.radioNormal);
    }

    public static void putVerseDisplayMode(Context context, int value) {
        context.getSharedPreferences(settings_file, 0).edit().putInt(VERSE_DISPLAY_MODE, value).commit();
    }

    public static float getRandomnessLevel(Context context) {
        return context.getSharedPreferences(settings_file, 0).getFloat(RANDOMNESS_LEVEL, 0.5f);
    }

    public static void putRandomnessLevel(Context context, float value) {
        context.getSharedPreferences(settings_file, 0).edit().putFloat(RANDOMNESS_LEVEL, value).commit();
    }

    public static int getRandomSeedOffset(Context context) {
        return context.getSharedPreferences(settings_file, 0).getInt(RANDOM_OFFSET, 0);
    }

    public static void putRandomSeedOffset(Context context, int value) {
        context.getSharedPreferences(settings_file, 0).edit().putInt(RANDOM_OFFSET, value).commit();
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
