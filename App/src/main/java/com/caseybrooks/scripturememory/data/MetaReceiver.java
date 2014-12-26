package com.caseybrooks.scripturememory.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.caseybrooks.scripturememory.fragments.DashboardFragment;
import com.caseybrooks.scripturememory.notifications.MainNotification;
import com.caseybrooks.scripturememory.notifications.QuickNotification;
import com.caseybrooks.scripturememory.notifications.VOTDNotification;
import com.caseybrooks.scripturememory.widgets.MainVerseWidget;

public class MetaReceiver extends BroadcastReceiver {
    public static final String NEXT_VERSE = ".MetaReceiver.NEXT_VERSE";
    public static final String VOTD_ALARM = ".MetaReceiver.VOTD_ALARM";
    public static final String UPDATE_ALL = ".MetaReceiver.UPDATE_ALL";
//  public static final String SAVE_VERSE = ".MetaReceiver.SAVE_VERSE";

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) { onBootCompleted(); }
        else if(intent.getAction().equals(NEXT_VERSE)) { getNextVerse(); }
        else if(intent.getAction().equals(VOTD_ALARM)) { getVOTD(); }
        else if(intent.getAction().equals(UPDATE_ALL)) { updateAll(); }
        else { throw new UnsupportedOperationException("Not yet implemented"); }
    }

    //methods to actually do the stuff when the intent comes in
    private void getNextVerse() {
//        VersesDatabase db = new VersesDatabase(context);
//        db.open();
//        try {
//            Passage verse = db.getEntryAfter(MetaSettings.getVerseId(context), "current");
//            MetaSettings.putVerseId(context, (int)verse.getId());
//            updateAll();
//        }
//        catch(SQLException e) {
//            new QuickNotification(context, "Error Getting Verse", e.getMessage());
//        }
//        db.close();
    }

    private void getVOTD() {
        if(MetaSettings.getVOTDShow(context)) {
            //Show VOTD if it can be downloaded now, or else just let user know
            //	that there is a new verse to be seen
            if(Util.isConnected(context)) {
                new VOTDNotification(context).retrieveInternetVerse();
                //startService(new Intent(context, VOTDService.class).putExtraBoolean(true));
            }
            else {
                new QuickNotification(context, "Verse of the Day", "Click here to see today's new Scripture!");
            }
        }
    }

    private void updateAll() {
        MainNotification.notify(context).show();

        context.sendBroadcast(new Intent(MainVerseWidget.UPDATE_ALL_WIDGETS));
        //update dashboard card
        context.sendBroadcast(new Intent(DashboardFragment.REFRESH));
    }

    private void onBootCompleted() {
        VOTDNotification.setAlarm(context);
        //If notification was set before the device was turned off, show it again
        if(MetaSettings.getNotificationActive(context)) {
            MainNotification.notify(context).show();
        }
        //startService(new Intent(context, VOTDService.class).putExtraBoolean(false));
        //Reset the VOTD alarm, because it gets canceled during reset
        VOTDNotification.setAlarm(context);
    }
}
