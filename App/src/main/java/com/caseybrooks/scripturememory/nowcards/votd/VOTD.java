package com.caseybrooks.scripturememory.nowcards.votd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.caseybrooks.androidbibletools.basic.Passage;
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

import java.io.File;
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
	ABSBible selectedVersion;

//Constructors and Initialization
//------------------------------------------------------------------------------

    public VOTD(Context context) {
        this.context = context;
		selectedVersion = MetaSettings.getBibleVersion(context);

		//try to get today's verse from cached file, or else download a new one
		new GetBible().execute();
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

	public void redownload() {
		File cacheFile = new File(context.getCacheDir(), cache_file);
		cacheFile.delete();

		setVOTDReference(context, "");

		new GetBible().execute();
	}

	private class GetBible extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			//We need to allow the user to enter a verse and check it as soon
			//as possible. But it takes several seconds to parse the selectedVersion
			//document, and the user may enter a verse before that is finished.
			//In that case, we must be able to detect that this thread is still
			//trying to initialize that information, and immediately use the
			//'defaultESV' object instead of 'selectedVersion' to check the book name
			//against.

			try {
				//try to get the information for selectedVersion, either from cache or download it
				Document doc = Util.getChachedDocument(context, "selectedVersion.xml");

				if(doc != null) {
					selectedVersion.parseDocument(doc);
					Log.i("VOTD: GET BIBLE", "Bible data was cached and successfully parsed");
				}
				else if(selectedVersion.isAvailable()) {
					if(Util.isConnected(context)) {
						doc = selectedVersion.getDocument();

						Util.cacheDocument(context, doc, "selectedVersion.xml");
						selectedVersion.parseDocument(doc);
						Log.i("VOTD: GET BIBLE", "Bible data was not cached, but was successfully downloaded and parsed");
					}
					else {
						selectedVersion = new ABSBible(
								context.getResources().getString(R.string.bibles_org),
								null
						);
						Log.i("VOTD: GET BIBLE", "Bible data was not cached and could not be downloaded, resorting to default ESV");
					}
				}
			}
			catch(IOException ioe) {
				ioe.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			new GetVerse().execute();
		}
	}

    private class GetVerse extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
				Document doc = Util.getChachedDocument(context, cache_file);

				if(doc != null) {
					currentVerse = new ABSPassage(
							context.getResources().getString(R.string.bibles_org),
							new Reference.Builder()
								.setBible(selectedVersion)
								.parseReference(getVOTDReference(context))
								.create()
					);
					currentVerse.parseDocument(doc);
					if(currentVerse.getText().length() > 0) {
						Log.i("VOTD: GET VERSE", "Verse data was cached and successfully parsed");
					}
					else {
						currentVerse.setText("Cached verse is from a different translation than your preferred version. Please redownload the verse.");
					}
					return null;
				}
				else {
					if(Util.isConnected(context)) {
						VerseOfTheDay votd = new VerseOfTheDay();
						votd.parseDocument(votd.getDocument());

						Passage passage = votd.getPassage();


						ABSPassage newVOTD = new ABSPassage(
								context.getResources().getString(R.string.bibles_org),
								new Reference.Builder()
										.setBible(selectedVersion)
										.parseReference(passage.getReference().toString())
										.create()
						);

						if(newVOTD.isAvailable()) {
							doc = newVOTD.getDocument();

							Util.cacheDocument(context, doc, cache_file);
							setVOTDReference(context, newVOTD.getReference().toString());

							newVOTD.parseDocument(doc);
							newVOTD.addTag(new Tag("VOTD"));
//							newVOTD.getMetadata().putInt(DefaultMetaData.STATE, currentVerse.getMetadata().getInt(DefaultMetaData.STATE));
							currentVerse = newVOTD;

							Log.i("VOTD: GET VERSE", "Verse data was not cached, but was successfully downloaded and parsed");

							return null;
						}
					}

					currentVerse.setText("Error Getting Verse");
					Log.i("VOTD: GET VERSE", "Verse data was not cached and could not be downloaded, resorting to default values");

					return null;
				}
            }
			catch (IOException ioe) {
                ioe.printStackTrace();
				currentVerse.setText("Error Getting Verse");

				return null;
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
