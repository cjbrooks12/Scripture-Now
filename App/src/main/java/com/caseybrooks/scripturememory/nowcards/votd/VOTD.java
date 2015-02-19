package com.caseybrooks.scripturememory.nowcards.votd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.basic.Tag;
import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.Util;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.fragments.DashboardFragment;
import com.caseybrooks.scripturememory.fragments.VerseListFragment;
import com.caseybrooks.scripturememory.misc.QuickNotification;
import com.caseybrooks.scripturememory.nowcards.main.Main;
import com.caseybrooks.scripturememory.nowcards.main.MainNotification;
import com.caseybrooks.scripturememory.nowcards.main.MainWidget;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;

public class VOTD {
//Shared preferences related to the Verse of the Day
	public static final String settings_file = "my_settings";

	private static final String PREFIX = "VOTD_";
	private static final String ENABLED = "ENABLED";
	private static final String SOUND = "SOUND";
	private static final String ACTIVE = "ACTIVE";
	private static final String TIME = "TIME";

	public static boolean isEnabled(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREFIX + ENABLED, false);
	}

	public static boolean isActive(Context context) {
		return context.getSharedPreferences(settings_file, 0).getBoolean(PREFIX + ACTIVE, false);
	}

	public static void setActive(Context context, boolean value) {
		context.getSharedPreferences(settings_file, 0).edit().putBoolean(PREFIX + ACTIVE, value).commit();
	}

	public static String getSound(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(PREFIX + SOUND, "DEFAULT_SOUND");
	}

	public static long getNotificationTime(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getLong(PREFIX + TIME, 0);
	}

	public static void setNotificationTime(Context context, long value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(PREFIX + TIME, value).commit();
	}

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
        if(Main.isActive(context)) {
			MainNotification.getInstance(context).create().show();
        }

		//update the notification to ensure it has the text that is in the database
		if(isActive(context)) {
			VOTDNotification.getInstance(context).create().show();
		}
    }

    public boolean saveVerse() {
		if(currentVerse != null) {
			VerseDB db = new VerseDB(context).open();
			currentVerse.getMetadata().putInt(DefaultMetaData.STATE, VerseDB.CURRENT_NONE);
			currentVerse.addTag(new Tag("VOTD"));
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
        Main.putVerseId(context, id);
        Main.setActive(context, true);
        Main.putWorkingList(context, VerseListFragment.TAGS, db.getTag("VOTD").id);
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
				if(currentVerse.getMetadata().getInt(DefaultMetaData.STATE) != VerseDB.VOTD) {
					db.open();
					db.deleteVerse(currentVerse);
					db.close();
				}
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
                    currentVerse.addTag(new Tag("VOTD"));
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
            if(isEnabled(context)) {
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

	public static class VOTDNotificationDismissedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			VOTD.setActive(context, false);
		}
	}

	//reset the alarm to show daily notification when the device boots
	public static class VOTDBootReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(isEnabled(context)) {
				VOTDNotification.getInstance(context).setAlarm();
			}
		}
	}
}
