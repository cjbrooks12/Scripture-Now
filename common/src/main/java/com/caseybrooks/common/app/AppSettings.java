package com.caseybrooks.common.app;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Pair;

import com.caseybrooks.common.app.activity.FeatureConfiguration;

public class AppSettings {
    public static final String SettingsFile = "my_settings";

    //settings saved through app settings
    private static final String APP_THEME = "APP_THEME";
    private static final String FIRST_INSTALL = "FIRST_INSTALL";
    private static final String CURRENT_VERSION = "CURRENT_VERSION";
    private static final String USER_LEARNED_DRAWER = "USER_LEARNED_DRAWER";
    private static final String SELECTED_FEATURE = "SELECTED_FEATURE";
    private static final String SELECTED_FEATURE_ID = "SELECTED_FEATURE_ID";
    private static final String DEFAULT_FEATURE = "DEFAULT_FEATURE";
    private static final String DEFAULT_FEATURE_ID = "DEFAULT_FEATURE_ID";

    private static final String TIME_HOUR = "TIME_HOUR";
    private static final String TIME_MINUTE = "TIME_MINUTE";


    public static String getAppTheme(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(APP_THEME, "Light");
    }

    public static boolean getUserLearnedDrawer(Context context) {
        return context.getSharedPreferences(SettingsFile, 0).getBoolean(USER_LEARNED_DRAWER, false);
    }

    public static void putUserLearnedDrawer(Context context, boolean value) {
        context.getSharedPreferences(SettingsFile, 0).edit().putBoolean(USER_LEARNED_DRAWER, value).commit();
    }

    public static boolean isFirstInstall(Context context) {
        return context.getSharedPreferences(SettingsFile, 0).getBoolean(FIRST_INSTALL, true);
    }

    public static void putFirstInstall(Context context, boolean value) {
        context.getSharedPreferences(SettingsFile, 0).edit().putBoolean(FIRST_INSTALL, value).commit();
    }

    public static int getAppVersion(Context context) {
        return context.getSharedPreferences(SettingsFile, 0).getInt(CURRENT_VERSION, Integer.MAX_VALUE);
    }

    public static void putAppVersion(Context context, int value) {
        context.getSharedPreferences(SettingsFile, 0).edit().putInt(CURRENT_VERSION, value).apply();
    }

    public static void putLastVisitedFeature(Context context, FeatureConfiguration feature) {
        if(feature == null) {
            context.getSharedPreferences(SettingsFile, 0).edit().remove(SELECTED_FEATURE).apply();
        }
        else {
            context.getSharedPreferences(SettingsFile, 0).edit().putString(SELECTED_FEATURE, feature.getClass().getName()).apply();
        }
    }

    public static void putLastVisitedFeature(Context context, Class<? extends FeatureConfiguration> feature) {
        if(feature == null) {
            context.getSharedPreferences(SettingsFile, 0).edit().remove(SELECTED_FEATURE).apply();
        }
        else {
            context.getSharedPreferences(SettingsFile, 0).edit().putString(SELECTED_FEATURE, feature.getClass().getName()).apply();
        }
    }

    public static Class<? extends FeatureConfiguration> getLastVisitedFeature(Context context) {
        try {
            return Class
                .forName(context.getSharedPreferences(SettingsFile, 0).getString(SELECTED_FEATURE, null))
                .asSubclass(FeatureConfiguration.class);
        }
        catch(ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return null;
        }
        catch(ClassCastException cce) {
            cce.printStackTrace();
            return null;
        }
    }

    public static void putTime(Context context, String key, int hour, int minute) {
        context.getSharedPreferences(SettingsFile, 0)
                .edit()
                .putInt(TIME_HOUR + "_" + key, hour)
                .putInt(TIME_MINUTE + "_" + key, minute)
                .apply();
    }

    public static Pair<Integer, Integer> getTime(Context context, String key) {
        int hour = context.getSharedPreferences(SettingsFile, 0).getInt(TIME_HOUR + "_" + key, 8);
        int minute = context.getSharedPreferences(SettingsFile, 0).getInt(TIME_MINUTE + "_" + key, 0);
        return new Pair<>(hour, minute);
    }
}
