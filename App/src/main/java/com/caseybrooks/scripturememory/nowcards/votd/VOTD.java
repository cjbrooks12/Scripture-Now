package com.caseybrooks.scripturememory.nowcards.votd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.Util;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.fragments.DashboardFragment;
import com.caseybrooks.scripturememory.fragments.VerseListFragment;
import com.caseybrooks.scripturememory.misc.QuickNotification;
import com.caseybrooks.scripturememory.nowcards.main.MainNotification;
import com.caseybrooks.scripturememory.nowcards.main.MainWidget;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;

public class VOTD {
    //Data Members
//------------------------------------------------------------------------------
    Context context;
    public Passage currentVerse;

//Constructors and Initialization
//------------------------------------------------------------------------------

    public VOTD(Context context) {
        this.context = context;

        //try to get today's verse from databse
        getCurrentVerse();

        //if verse has not been downloaded or wasn't today's verse, it will be null. attempt to redownload it
        if(currentVerse == null && Util.isConnected(context)) {
            new DownloadCurrentVerse().execute();
        }
    }

//VOTD lifecycle and verse management
//------------------------------------------------------------------------------
    public void updateAll() {
		//update dashboard cards
		context.sendBroadcast(new Intent(DashboardFragment.REFRESH));

        //update the widgets: VOTD (obviously), Main because it may be the VOTD
		context.sendBroadcast(new Intent(context, MainWidget.class));
        context.sendBroadcast(new Intent(context, VOTDWidget.class));

        //update the notifications: Main because it may be the VOTD
        if(MetaSettings.getNotificationActive(context)) {
			MainNotification.getInstance(context).create().show();
        }
    }

    public boolean saveVerse() {
		if(currentVerse != null) {
			VerseDB db = new VerseDB(context).open();
			currentVerse.getMetadata().putInt(DefaultMetaData.STATE, VerseDB.CURRENT_NONE);
			currentVerse.addTag("VOTD");
			int id = db.getVerseId(currentVerse.getReference());
			if(id == -1) {
				id = db.insertVerse(currentVerse);
				currentVerse.getMetadata().putInt(DefaultMetaData.ID, id);
			}
			else {
				currentVerse.getMetadata().putInt(DefaultMetaData.ID, id);
				db.updateVerse(currentVerse);
			}
			db.close();
			return true;
		}
		else return false;
    }

    public void setAsNotification() {
        saveVerse();
        VerseDB db = new VerseDB(context).open();
        int id = db.getVerseId(currentVerse.getReference());
        MetaSettings.putVerseId(context, id);
        MetaSettings.putNotificationActive(context, true);
        MetaSettings.putActiveList(context, VerseListFragment.TAGS, (int) db.getTagID("VOTD"));
        db.close();

        updateAll();
    }

    public void getCurrentVerse() {
        //get all verses that are either tagged or in the state of VOTD
        VerseDB db = new VerseDB(context).open();
        currentVerse = db.getMostRecentVOTD();
        db.close();

        //no VOTD exist in database at this time
        if(currentVerse != null) {
            //check the timestamp of the most recent verse against the current time.
            //if the current verse is on the same day as today, then it is current.
            //if the current verse is not today, it needs to get updated
            Calendar today = Calendar.getInstance();
            Calendar current = Calendar.getInstance();
            current.setTimeInMillis(currentVerse.getMetadata().getLong(DefaultMetaData.TIME_CREATED));

            boolean isCurrent =
                    (today.get(Calendar.ERA) == current.get(Calendar.ERA)
                            && today.get(Calendar.YEAR) == current.get(Calendar.YEAR)
                            && today.get(Calendar.DAY_OF_YEAR) == current.get(Calendar.DAY_OF_YEAR));

            if(!isCurrent) {
                db.open();
                db.deleteVerse(currentVerse);
                db.close();
                currentVerse = null;
            }
        }
    }

    public void downloadCurrentVerseAsync() {
        new DownloadCurrentVerse().execute();
    }

    private class DownloadCurrentVerse extends AsyncTask<Void, Void, Void> {
        int votdState;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(currentVerse != null) {
                votdState = currentVerse.getMetadata().getInt(DefaultMetaData.STATE, VerseDB.VOTD);
            }
            else {
                votdState = VerseDB.VOTD;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            updateAll();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            updateAll();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Document doc = Jsoup.connect("http://verseoftheday.com").get();

                Elements reference = doc.select("meta[property=og:title]");

                try {
                    currentVerse = new Passage(reference.attr("content").substring(18));
                }
                catch(ParseException e) {
                    currentVerse = null;
                }

                if (currentVerse != null) {
                    currentVerse.setVersion(MetaSettings.getBibleVersion(context));
                    currentVerse.retrieve();
                    currentVerse.addTag("VOTD");
                    currentVerse.getMetadata().putInt(DefaultMetaData.STATE, votdState);

                    VerseDB db = new VerseDB(context).open();
                    int id = db.getVerseId(currentVerse.getReference());
                    if (id == -1) {
                        id = db.insertVerse(currentVerse);
                        currentVerse.getMetadata().putInt(DefaultMetaData.ID, id);
                    } else {
                        currentVerse.getMetadata().putInt(DefaultMetaData.ID, id);
                        db.updateVerse(currentVerse);
                    }
                    db.close();
                }

                return null;
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return null;
            }
        }
    }

//Broadcast Receivers
//------------------------------------------------------------------------------

    public static class VOTDAlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(MetaSettings.getVOTDShow(context)) {
				VOTDNotification.getInstance(context).create().show();
            }
        }
    }

    public static class VOTDSaveVerseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            VOTD votd = new VOTD(context);
            if(votd.currentVerse != null) {
                votd.saveVerse();
                VOTDNotification.getInstance(context).dismiss();
                new QuickNotification(context, "Verse of the Day", votd.currentVerse.getReference().toString() + " added to list").show();
            }
        }
    }

	//reset the alarm to show daily notification when the device boots
	public static class VOTDBootReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(MetaSettings.getVOTDShow(context)) {
				VOTDNotification.getInstance(context).setAlarm();
			}
		}
	}
}
