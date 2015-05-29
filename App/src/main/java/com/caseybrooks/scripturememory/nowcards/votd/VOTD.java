package com.caseybrooks.scripturememory.nowcards.votd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.caseybrooks.androidbibletools.basic.Reference;
import com.caseybrooks.androidbibletools.basic.Tag;
import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
import com.caseybrooks.androidbibletools.providers.abs.ABSBible;
import com.caseybrooks.androidbibletools.providers.abs.ABSPassage;
import com.caseybrooks.androidbibletools.providers.votd.VerseOfTheDay;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.Util;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.fragments.DashboardFragment;
import com.caseybrooks.scripturememory.fragments.VerseListFragment;
import com.caseybrooks.scripturememory.misc.QuickNotification;
import com.caseybrooks.scripturememory.nowcards.main.Main;
import com.caseybrooks.scripturememory.nowcards.main.MainNotification;
import com.caseybrooks.scripturememory.nowcards.main.MainWidget;

import org.jsoup.nodes.Document;

import java.io.IOException;

public class VOTD {
//Shared preferences related to the Verse of the Day
	public static final String settings_file = "my_settings";
	public static final String cache_file = "votd_cache.xml";

	private static final String PREFIX = "VOTD_";
	private static final String ENABLED = "ENABLED";
	private static final String SOUND = "SOUND";
	private static final String ACTIVE = "ACTIVE";
	private static final String TIME = "TIME";
	private static final String REFERENCE = "REFERENCE";

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

	public static String getVOTDReference(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(PREFIX + REFERENCE, "");
	}

	public static void setVOTDReference(Context context, String value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREFIX + REFERENCE, value).commit();
	}

//Data Members
//------------------------------------------------------------------------------
    Context context;
    public ABSPassage currentVerse;

//Constructors and Initialization
//------------------------------------------------------------------------------

    public VOTD(Context context) {
        this.context = context;

        //try to get today's verse from cached file, or else download a new one
        getCurrentVerse();
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

	public boolean isSaved() {
		VerseDB db = new VerseDB(context).open();
		if(db.getVerse(db.getVerseId(currentVerse.getReference())) == null) {
			db.close();
			return false;
		}
		else {
			db.close();
			return true;
		}
	}

    public boolean saveVerse() {
		if(currentVerse != null) {
			VerseDB db = new VerseDB(context).open();
			currentVerse.getMetadata().putInt(DefaultMetaData.STATE, VerseDB.CURRENT_NONE);
			currentVerse.addTag(new Tag("VOTD"));
			db.insertVerse(currentVerse);
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
		Document doc = Util.getChachedDocument(context, cache_file);

		if(doc != null) {
			currentVerse = new ABSPassage(
					context.getResources().getString(R.string.bibles_org),
					new Reference.Builder()
							.parseReference(getVOTDReference(context))
							.create());
			currentVerse.parseDocument(doc);
			Log.e("GET CURRENT VERSE", "DOC NOT NULL " +
					currentVerse.getReference().toString() +
					currentVerse.getText());

//			updateAll();
		}
		else {
			if(Util.isConnected(context)) {
				Log.e("GET CURRENT VERSE", "DOC NULL, DOC NOT CACHED. DOWNLOADING NOW");

				new DownloadCurrentVerse().execute();
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
				VerseOfTheDay votd = new VerseOfTheDay();
				votd.parseDocument(votd.getDocument());

				ABSBible bible = MetaSettings.getBibleVersion(context);
				if(bible.getId() == null || bible.getId().length() == 0) {
					bible = new ABSBible("eng-ESV");
				}

				ABSPassage downloadedVerse = new ABSPassage(
						context.getResources().getString(R.string.bibles_org),
						votd.getPassage().getReference()
				);

				downloadedVerse.setId(
						MetaSettings.getBibleVersion(context).getId()
						+ ":" + votd.getPassage().getReference().book.getAbbreviation()
						+ "." + votd.getPassage().getReference().chapter);

                if (downloadedVerse.isAvailable()) {
					Document verseDoc = downloadedVerse.getDocument();

					if(verseDoc != null) {
						Util.cacheDocument(context, verseDoc, cache_file);
						setVOTDReference(context, currentVerse.getReference().toString());

						downloadedVerse.parseDocument(verseDoc);
						downloadedVerse.addTag(new Tag("VOTD"));
						downloadedVerse.getMetadata().putInt(DefaultMetaData.STATE, votdState);
						currentVerse = downloadedVerse;
					}
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
