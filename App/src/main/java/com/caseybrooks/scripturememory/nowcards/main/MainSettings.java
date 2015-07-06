package com.caseybrooks.scripturememory.nowcards.main;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Pair;

public class MainSettings {
	private static final String settings_file = "my_settings";
	private static final String PREFIX = "MAIN_";

	//the list that the user is working within
	public static final String WORKING_LIST_GROUP = "WORKING_LIST_GROUP";
	public static final String WORKING_LIST_CHILD = "WORKING_LIST_CHILD";
	public static final String MAIN_ID = "MAIN_ID";
	public static final String ACTIVE_ID = "ACTIVE_ID";

	public static void putWorkingList(Context context, int group, int child) {
		context.getSharedPreferences(settings_file, 0).edit().putInt(PREFIX + WORKING_LIST_GROUP, group).commit();
		context.getSharedPreferences(settings_file, 0).edit().putInt(PREFIX + WORKING_LIST_CHILD, child).commit();
	}
	public static Pair<Integer, Integer> getWorkingList(Context context) {
		return new Pair<>(
				context.getSharedPreferences(settings_file, 0).getInt(PREFIX + WORKING_LIST_GROUP, -1),
				context.getSharedPreferences(settings_file, 0).getInt(PREFIX + WORKING_LIST_CHILD, -1));
	}

	//whether the user has the main notification actively displayed
	private static final String ACTIVE = "ACTIVE";
	public static boolean isActive(Context context) {
		return context.getSharedPreferences(settings_file, 0).getBoolean(PREFIX + ACTIVE, false);
	}
	public static void setActive(Context context, boolean value) {
		context.getSharedPreferences(settings_file, 0).edit().putBoolean(PREFIX + ACTIVE, value).commit();
	}

	//how to view the main notification
	public static final String DISPLAY_MODE = "DISPLAY_MODE";
	public static int getDisplayMode(Context context) {
		return context.getSharedPreferences(settings_file, 0).getInt(PREFIX + DISPLAY_MODE, 0);
	}
	public static void putDisplayMode(Context context, int value) {
		context.getSharedPreferences(settings_file, 0).edit().putInt(PREFIX + DISPLAY_MODE, value).commit();
	}

	//whether to show the full or formatted text in the notification
	private static final String FULL_TEXT = "FULL_TEXT";
	public static boolean isTextFull(Context context) {
		return context.getSharedPreferences(settings_file, 0).getBoolean(PREFIX + FULL_TEXT, false);
	}
	public static void setTextFull(Context context, boolean value) {
		context.getSharedPreferences(settings_file, 0).edit().putBoolean(PREFIX + FULL_TEXT, value).commit();
	}

	//if the display mode is "Random Words", how random and at what offest
	private static final String RANDOMNESS_LEVEL = "RANDOMNESS_LEVEL";
	private static final String RANDOMNESS_OFFSET = "RANDOMNESS_OFFSET";
	public static Pair<Float, Integer> getRandomness(Context context) {
		return new Pair<>(
				context.getSharedPreferences(settings_file, 0).getFloat(PREFIX + RANDOMNESS_LEVEL, 0.5f),
				context.getSharedPreferences(settings_file, 0).getInt(PREFIX + RANDOMNESS_OFFSET, 0));
	}
	public static void putRandomness(Context context, float level, int offset) {
		if(level >= 0) context.getSharedPreferences(settings_file, 0).edit().putFloat(PREFIX + RANDOMNESS_LEVEL, level).commit();
		if(offset >= 0) context.getSharedPreferences(settings_file, 0).edit().putInt(PREFIX + RANDOMNESS_OFFSET, offset).commit();
	}

	public static int getMainId(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getInt(PREFIX + MAIN_ID, -1);
	}

	public static void setMainId(Context context, int value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(PREFIX + MAIN_ID, value).commit();
	}

	public static int getActiveId(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getInt(PREFIX + ACTIVE_ID, -1);
	}

	public static void setActiveId(Context context, int value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(PREFIX + ACTIVE_ID, value).commit();
	}

}
