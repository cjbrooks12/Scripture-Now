package com.caseybrooks.scripturememory.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.data.Metadata;
import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
import com.caseybrooks.scripturememory.fragments.DashboardFragment;
import com.caseybrooks.scripturememory.fragments.VerseListFragment;
import com.caseybrooks.scripturememory.notifications.MainNotification;
import com.caseybrooks.scripturememory.views.VOTD;
import com.caseybrooks.scripturememory.widgets.MainVerseWidget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MetaReceiver extends BroadcastReceiver {
    public static final String NEXT_VERSE = ".MetaReceiver.NEXT_VERSE";
    public static final String UPDATE_ALL = ".MetaReceiver.UPDATE_ALL";

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) { onBootCompleted(); }
        else if(intent.getAction().equals(NEXT_VERSE)) { getNextVerse(); }
        else if(intent.getAction().equals(UPDATE_ALL)) { updateAll(); }
        else { throw new UnsupportedOperationException("Not yet implemented"); }
    }

    //methods to actually do the stuff when the intent comes in
    private void getNextVerse() {
        Pair<Integer, Integer> activeList = MetaSettings.getActiveList(context);
        if(activeList.first == -1) return;

        int currentVerseId = MetaSettings.getVerseId(context);
        VerseDB db = new VerseDB(context).open();

        ArrayList<Passage> passages;
        if(activeList.first == VerseListFragment.STATE) {
            passages = db.getStateVerses(activeList.second);
        }
        else if(activeList.first == VerseListFragment.TAGS) {
            passages = db.getTaggedVerses(activeList.second);
        }
        else return;

        db.close();

        Comparator comparator;

        switch(MetaSettings.getSortBy(context)) {
            case 0:
                comparator = new Metadata.Comparator(DefaultMetaData.TIME_CREATED);
                break;
            case 1:
                comparator = new Metadata.Comparator(Metadata.Comparator.KEY_REFERENCE);
                break;
            case 2:
                comparator = new Metadata.Comparator(Metadata.Comparator.KEY_REFERENCE_ALPHABETICAL);
                break;
            case 3:
                comparator = new Metadata.Comparator(DefaultMetaData.STATE);
                break;
            default:
                comparator = new Metadata.Comparator("ID");
                break;
        }

        Collections.sort(passages, comparator);

        for(int i = 0; i < passages.size(); i++) {
            if(passages.get(i).getMetadata().getInt(DefaultMetaData.ID) == currentVerseId) {
                if(i == passages.size() - 1) {
                    int oldId = currentVerseId;
                    currentVerseId = passages.get(0).getMetadata().getInt(DefaultMetaData.ID);
                    MetaSettings.putVerseId(context, currentVerseId);
                    break;
                }
                else {
                    int oldId = currentVerseId;
                    currentVerseId = passages.get(i+1).getMetadata().getInt(DefaultMetaData.ID);
                    MetaSettings.putVerseId(context, currentVerseId);
                    break;
                }
            }
        }
        updateAll();
    }

    private void updateAll() {
        MainNotification.notify(context).show();

        context.sendBroadcast(new Intent(MainVerseWidget.UPDATE_ALL_WIDGETS));
        //update dashboard card
        context.sendBroadcast(new Intent(DashboardFragment.REFRESH));
    }

    private void onBootCompleted() {
        //Reset the VOTD alarm, because it gets canceled during reset
        VOTD.VOTDNotification.getInstance(context).setAlarm();

        //If notification was set before the device was turned off, show it again
        if(MetaSettings.getNotificationActive(context)) {
            MainNotification.notify(context).show();
        }
    }
}
