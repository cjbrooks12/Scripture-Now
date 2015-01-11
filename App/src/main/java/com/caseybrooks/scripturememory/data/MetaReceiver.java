package com.caseybrooks.scripturememory.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Pair;
import android.widget.Toast;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.data.Metadata;
import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
import com.caseybrooks.scripturememory.fragments.DashboardFragment;
import com.caseybrooks.scripturememory.fragments.VerseListFragment;
import com.caseybrooks.scripturememory.notifications.MainNotification;
import com.caseybrooks.scripturememory.notifications.QuickNotification;
import com.caseybrooks.scripturememory.notifications.VOTDNotification;
import com.caseybrooks.scripturememory.widgets.MainVerseWidget;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

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
        else if(intent.getAction().equals(Intent.ACTION_SEND)) {
            try {
                Uri contentUri = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
                FileInputStream in = new FileInputStream(contentUri.getPath());

                TransformerFactory factory = TransformerFactory.newInstance();
                Transformer transformer = factory.newTransformer();

                String path = Environment.getExternalStorageDirectory().getPath() + "/scripturememory";

                int lastIndex = contentUri.getPath().lastIndexOf("/");
                String fileName = contentUri.getPath().substring(lastIndex);

                Document doc = Jsoup.parse(in, null, null);
                if(doc.select("verses").size() > 0) {
                    transformer.transform(new StreamSource(in), new StreamResult(new File(path, fileName)));
                    Toast.makeText(context, "File added successfully", Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if(intent.getAction().equals(NEXT_VERSE)) { getNextVerse(); }
        else if(intent.getAction().equals(VOTD_ALARM)) { getVOTD(); }
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

    private void getVOTD() {
        if(MetaSettings.getVOTDShow(context)) {
            //Show VOTD if it can be downloaded now, or else just let user know
            //	that there is a new verse to be seen
            if(Util.isConnected(context)) {
                new VOTDNotification(context).retrieveInternetVerse();
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
