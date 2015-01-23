package com.caseybrooks.scripturememory.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.caseybrooks.scripturememory.nowcards.main.MainNotification;
import com.caseybrooks.scripturememory.nowcards.votd.VOTDNotification;

public class MetaReceiver extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) { onBootCompleted(); }
        else { throw new UnsupportedOperationException("Not yet implemented"); }
    }

    private void onBootCompleted() {
        //Reset the VOTD alarm, because it gets canceled during reset
        VOTDNotification.getInstance(context).setAlarm();

        //If notification was set before the device was turned off, show it again
        if(MetaSettings.getNotificationActive(context)) {
            MainNotification.getInstance().show();
        }
    }
}
