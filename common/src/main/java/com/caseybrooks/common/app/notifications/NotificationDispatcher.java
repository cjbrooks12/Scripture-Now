package com.caseybrooks.common.app.notifications;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This class is the entry point for all notification-related Intents. Notification intents will be
 * sent from this class, as well as recived into this class and dispatched to the appropriate
 * NotificationBase class and receiver method.
 *
 * When sending an intent, using this class and pass it a String with the name of a method that
 * will receive the action of the intent. This class will find that method and send you the result
 * of that Intent.
 */
public class NotificationDispatcher extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
