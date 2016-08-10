package com.caseybrooks.common.features.votd;


import android.content.Context;

import com.caseybrooks.common.app.FeatureConfiguration;
import com.caseybrooks.common.app.features.dashboard.DashboardCardConfiguration;
import com.caseybrooks.common.app.notifications.NotificationConfiguration;

public class VerseOfTheDayFeatureConfiguration extends FeatureConfiguration {

    private static VerseOfTheDayFeatureConfiguration instance;

    public static VerseOfTheDayFeatureConfiguration getInstance() {
        if(instance == null) {
            instance = new VerseOfTheDayFeatureConfiguration();
        }

        return instance;
    }

    @Override
    public AppFeature getAppFeature() {
        return AppFeature.VerseOfTheDay;
    }

    @Override
    public DashboardCardConfiguration getDashboardCardConfiguration(Context context) {
        return VerseOfTheDayCard.configuration;
    }

    @Override
    public NotificationConfiguration getNotificationConfiguration(Context context) {
        return VerseOfTheDayNotification.notificationConfiguration;
    }
}
