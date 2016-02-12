package com.caseybrooks.common.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.caseybrooks.common.app.AppSettings;

public class OnBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(AppSettings.isDailyNotificationEnabled(context))
            DailyNotification.setNotificationAlarm(context);

        if(AppSettings.isScheduledNotificationEnabled(context))
            ScheduledNotification.setNotificationAlarm(context);

    }
}
