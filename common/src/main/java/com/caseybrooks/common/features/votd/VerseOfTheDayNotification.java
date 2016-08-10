package com.caseybrooks.common.features.votd;

import com.caseybrooks.common.app.notifications.NotificationBase;
import com.caseybrooks.common.app.notifications.NotificationConfiguration;

public class VerseOfTheDayNotification extends NotificationBase {

    @Override
    public NotificationConfiguration getConfiguration() {
        return notificationConfiguration;
    }

    public static NotificationConfiguration notificationConfiguration = new NotificationConfiguration() {

    };

    @Override
    public AppFeature getAppFeature() {
        return AppFeature.VerseOfTheDay;
    }
}
