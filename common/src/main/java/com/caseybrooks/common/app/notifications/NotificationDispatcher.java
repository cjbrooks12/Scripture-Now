package com.caseybrooks.common.app.notifications;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;

import com.caseybrooks.common.app.activity.ActivityBase;
import com.caseybrooks.common.app.application.ApplicationBase;

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

    public static PendingIntent getBaseIntent(Context context, NotificationBase notificationBase) {
        ApplicationBase application = (ApplicationBase) context.getApplicationContext();
        Class<? extends ActivityBase> activityClass = application.getActivityClass();

        Intent resultIntent = new Intent(context, NotificationDispatcher.class);

        Bundle intentArgs = new Bundle();
        intentArgs.putString("notificationBaseClass", notificationBase.getClass().getName());
        resultIntent.putExtras(intentArgs);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(activityClass);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPI = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        return resultPI;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //determine how to handle the incoming intent:

        //open the main activity to the given DrawerFeature


        //handle the intent in the background


    }



    public void openActivity() {

    }

    public void dispatchIntent() {

    }
}
