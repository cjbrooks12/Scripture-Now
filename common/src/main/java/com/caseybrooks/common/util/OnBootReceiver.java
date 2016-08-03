package com.caseybrooks.common.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.caseybrooks.common.app.AppSettings;
import com.caseybrooks.common.features.joshuaproject.JoshuaProjectNotification;
import com.caseybrooks.common.features.votd.VerseOfTheDayNotification;

public class OnBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(AppSettings.isVerseOfTheDayNotificationEnabled(context)) {
            VerseOfTheDayNotification.setNotificationAlarm(context);
        }
        if(AppSettings.isJoshuaProjectNotificationEnabled(context)) {
            JoshuaProjectNotification.setNotificationAlarm(context);
        }
    }
}
