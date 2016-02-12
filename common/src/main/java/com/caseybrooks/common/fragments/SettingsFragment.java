package com.caseybrooks.common.fragments;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.util.Pair;

import com.caseybrooks.common.app.AppFeature;
import com.caseybrooks.common.app.PreferenceFragmentBase;
import com.caseybrooks.common.R;
import com.caseybrooks.common.notifications.DailyNotification;
import com.caseybrooks.common.notifications.ScheduledNotification;

public class SettingsFragment extends PreferenceFragmentBase {
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle data = new Bundle();
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        super.onCreatePreferences(bundle, s);
        addPreferencesFromResource(R.xml.settings);
        settingsScreens.push(getPreferenceScreen());

        findPreference("votd_enabled").setOnPreferenceChangeListener(votdChangedListener);
        findPreference("votd_time").setOnPreferenceChangeListener(votdChangedListener);

        findPreference("schedule_enabled").setOnPreferenceChangeListener(scheduledChangedListener);
        findPreference("schedule_start").setOnPreferenceChangeListener(scheduledChangedListener);
        findPreference("schedule_end").setOnPreferenceChangeListener(scheduledChangedListener);
        findPreference("schedule_interval").setOnPreferenceChangeListener(scheduledChangedListener);

        findPreference("APP_THEME").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                getActivity().recreate();
                return true;
            }
        });
    }

    @Override
    public Pair<AppFeature, Integer> getFeatureForFragment() {
        return new Pair<>(AppFeature.Settings, 0);
    }

    private Preference.OnPreferenceChangeListener votdChangedListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            if(o instanceof Boolean) {
                boolean value = (boolean) o;

                if(value) {
                    DailyNotification.setNotificationAlarm(getContext());
                }
                else {
                    DailyNotification.cancelNotificationAlarm(getContext());
                }
            }
            else {
                DailyNotification.setNotificationAlarm(getContext());
            }

            return true;
        }
    };

    private Preference.OnPreferenceChangeListener scheduledChangedListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            if(o instanceof Boolean) {
                boolean value = (boolean) o;

                if(value) {
                    ScheduledNotification.setNotificationAlarm(getContext());
                }
                else {
                    ScheduledNotification.cancelNotificationAlarm(getContext());
                }
            }
            else {
                ScheduledNotification.setNotificationAlarm(getContext());
            }

            return true;
        }
    };
}
