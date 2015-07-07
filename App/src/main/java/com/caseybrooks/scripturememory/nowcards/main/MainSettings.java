package com.caseybrooks.scripturememory.nowcards.main;

import android.content.Context;
import android.util.Pair;

public class MainSettings {
	private static final String settings_file = "my_settings";
	private static final String PREFIX = "MAIN_";

	//information about the main notification
	private static final String ACTIVE = "ACTIVE";
	public static final String DISPLAY_MODE = "DISPLAY_MODE";
	private static final String FULL_TEXT = "FULL_TEXT";

	//the list that the user is working within
	public static final String WORKING_LIST_GROUP = "WORKING_LIST_GROUP";
	public static final String WORKING_LIST_CHILD = "WORKING_LIST_CHILD";
	public static final String ID = "ID";
	public static final String NEXT_COUNT = "NEXT_COUNT";
	public static final String PREVIOUS_COUNT = "PREVIOUS_COUNT";
	public static final String RANDOM_COUNT = "RANDOM_COUNT";

	private static final String RANDOMNESS_LEVEL = "RANDOMNESS_LEVEL";
	private static final String RANDOMNESS_OFFSET = "RANDOMNESS_OFFSET";

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
	public static boolean isActive(Context context) {
		return context.getSharedPreferences(settings_file, 0).getBoolean(PREFIX + ACTIVE, false);
	}
	public static void setActive(Context context, boolean value) {
		context.getSharedPreferences(settings_file, 0).edit().putBoolean(PREFIX + ACTIVE, value).commit();
	}

	//how to view the main notification
	public static int getDisplayMode(Context context) {
		int mode = context.getSharedPreferences(settings_file, 0).getInt(PREFIX + DISPLAY_MODE, 1);
		return (mode >= 0 && mode <= 3) ? mode : 1;
	}
	public static void putDisplayMode(Context context, int value) {
		context.getSharedPreferences(settings_file, 0).edit().putInt(PREFIX + DISPLAY_MODE, value).commit();
	}

	//whether to show the full or formatted text in the notification
	public static boolean isTextFull(Context context) {
		return context.getSharedPreferences(settings_file, 0).getBoolean(PREFIX + FULL_TEXT, false);
	}
	public static void setTextFull(Context context, boolean value) {
		context.getSharedPreferences(settings_file, 0).edit().putBoolean(PREFIX + FULL_TEXT, value).commit();
	}

	//if the display mode is "Random Words", how random and at what offest
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
		return context.getSharedPreferences(settings_file, 0).getInt(PREFIX + ID, -1);
	}

	public static void setMainId(Context context, int value) {
		context.getSharedPreferences(settings_file, 0).edit().putInt(PREFIX + ID, value).commit();
	}

	public static int getCounter(Context context, String key) {
		return context.getSharedPreferences(settings_file, 0).getInt(PREFIX + key, 0);
	}

	public static void incrementCounter(Context context, String key) {
		int count = getCounter(context, key) + 1;
		context.getSharedPreferences(settings_file, 0).edit().putInt(PREFIX + key, count).commit();
	}
}
