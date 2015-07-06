package com.caseybrooks.scripturememory.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.caseybrooks.androidbibletools.basic.Bible;
import com.caseybrooks.androidbibletools.basic.Tag;
import com.caseybrooks.androidbibletools.providers.abs.ABSBible;
import com.caseybrooks.common.features.NavigationCallbacks;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.Util;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.misc.PreferenceFragment;
import com.caseybrooks.scripturememory.nowcards.votd.VOTDNotification;
import com.caseybrooks.scripturememory.nowcards.votd.VOTDSettings;

import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class SettingsFragment extends PreferenceFragment {
	Context context;

    ListPreference prefDefaultScreenChild;
	NavigationCallbacks mCallbacks;

	HashMap<String, String> availableLanguages;
	HashMap<String, Bible> availableBibles;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = getActivity();

		addPreferencesFromResource(R.xml.settings);

		//Populate Version list with versions available in AndroidBibleTools
		setClicks();
		setThemeSpinners();
		setDefaultScreenSpinners();
//		setBibleVersionSpinners();
	}

//Settings Setup Routine
//------------------------------------------------------------------------------
	private void setClicks() {
		findPreference("BACKUP").setOnPreferenceClickListener(backupClick);
		findPreference("RESTORE").setOnPreferenceClickListener(restoreClick);
		findPreference("VOTD_ENABLED").setOnPreferenceChangeListener(VOTDCheckedChange);
		findPreference("VOTD_TIME").setOnPreferenceChangeListener(VOTDTimeChange);
	}

	private void setThemeSpinners() {
		ListPreference appTheme = (ListPreference) findPreference("APP_THEME");
		try {
			Field[] themes = new Field[]{
					R.style.class.getDeclaredField("Theme_Light"),
					R.style.class.getDeclaredField("Theme_Dark")};

			String[] themeNames = new String[themes.length];
			String[] themeValues = new String[themes.length];
			for(int i = 0; i < themes.length; i++) {
				themeNames[i] = themes[i].getName().substring(6);
				themeValues[i] = themes[i].getName();
			}


			appTheme.setOnPreferenceChangeListener(appThemeChange);

			appTheme.setSummary(MetaSettings.getAppTheme(context).substring(6));
			appTheme.setEntries(themeNames);
			appTheme.setEntryValues(themeValues);
		}
		catch(NoSuchFieldException nsfe) {
			appTheme.setEnabled(false);
			appTheme.setSummary("Themes disabled");
		}
	}

	private void setDefaultScreenSpinners() {
        Pair<Integer, Integer> defaultScreen = MetaSettings.getDefaultScreen(context);

        ListPreference prefDefaultScreenGroup = (ListPreference) findPreference("PREF_DEFAULT_SCREEN_GROUP");
		String[] screens = getResources().getStringArray(R.array.pref_default_screen);
		switch(defaultScreen.first) {
		case 0:
			prefDefaultScreenGroup.setSummary(screens[0]);
			break;
		case 1:
			prefDefaultScreenGroup.setSummary(screens[2]);
			break;
		case 2:
			prefDefaultScreenGroup.setSummary(screens[3]);
			break;
		case 3:
			prefDefaultScreenGroup.setSummary(screens[4]);
			break;
		case 4:
			prefDefaultScreenGroup.setSummary(screens[5]);
			break;
		case 5:
			prefDefaultScreenGroup.setSummary(screens[1]);
			break;
		}
        prefDefaultScreenGroup.setOnPreferenceChangeListener(defaultScreenChange);

        prefDefaultScreenChild = (ListPreference) findPreference("PREF_DEFAULT_SCREEN_CHILD");

        if(defaultScreen.first == 3) {
            prefDefaultScreenChild.setEnabled(true);

            VerseDB db = new VerseDB(context).open();
            prefDefaultScreenChild.setSummary(db.getStateName(defaultScreen.second));
            db.close();

            String[] states = getResources().getStringArray(R.array.state_names);
            String[] statesValues = getResources().getStringArray(R.array.state_ids);
            prefDefaultScreenChild.setEntries(states);
            prefDefaultScreenChild.setEntryValues(statesValues);
        }
        else if(defaultScreen.first == 4) {
            prefDefaultScreenChild.setEnabled(true);
            VerseDB db = new VerseDB(context).open();
            prefDefaultScreenChild.setSummary(db.getTag(defaultScreen.second).name);

            ArrayList<Tag> tags = db.getAllTags();

            String[] tagValues = new String[tags.size()];
			String[] tagNames = new String[tags.size()];
            for(int i = 0; i < tagValues.length; i++) {
                tagValues[i] = Integer.toString(tags.get(i).id);
				tagNames[i] = tags.get(i).name;
			}

            prefDefaultScreenChild.setEntries(tagNames);
            prefDefaultScreenChild.setEntryValues(tagValues);
            db.close();
        }
        else {
            prefDefaultScreenChild.setEnabled(false);
            prefDefaultScreenChild.setSummary("Not Available");
        }

        prefDefaultScreenChild.setOnPreferenceChangeListener(defaultScreenChildChange);
    }

	private void setBibleVersionSpinners() {
		new AsycnBibleLanguages().execute();
	}

//Fragment Lifecycle
//------------------------------------------------------------------------------
    @Override
    public void onResume() {
        super.onResume();

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.color_toolbar, typedValue, true);

		mCallbacks.setToolBar("Settings", typedValue.data);

        MetaSettings.putDrawerSelection(context, 4, 0);
    }

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallbacks = (NavigationCallbacks) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException("Activity must implement NavigationCallbacks.");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = null;
	}

//Backup and Restore Preference Listeners
//------------------------------------------------------------------------------
	OnPreferenceClickListener backupClick = new OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setCancelable(true);
			builder.setTitle("Backup Verses");
			builder.setMessage("Backup verses? This will overwrite existing backup file.");
			builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					VerseDB db = new VerseDB(context).open();
                    String state = Environment.getExternalStorageState();
                    if (Environment.MEDIA_MOUNTED.equals(state)) {
                        String path = Environment.getExternalStorageDirectory().getPath() + "/scripturememory";
                        File folder = new File(path);

                        if(!folder.exists()) {
                            folder.mkdirs();
                        }
                        if(folder.exists()) {
                            File externalStorage = new File(path, "backup.xml");
                            db.exportToBackupFile(externalStorage);
                            Toast.makeText(context, "Backup Successful", Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        Toast.makeText(context, "Unable to open external storage. Check if SD card is installed properly", Toast.LENGTH_LONG).show();
                    }

					db.close();
				}
			});
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {

				}
			});

			AlertDialog dialog = builder.create();
			dialog.show();

			return false;
		}
	};

	OnPreferenceClickListener restoreClick = new OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
				String path = Environment.getExternalStorageDirectory().getPath() + "/scripturememory";
				File folder = new File(path);

				if(folder.exists()) {
					final File externalStorage = new File(path, "backup.xml");
					if(externalStorage.exists()) {
						AlertDialog.Builder builder = new AlertDialog.Builder(context);
						builder.setCancelable(true);
						builder.setTitle("Restore Verses");
						builder.setMessage("Restore verses from backup? This will replace all currently saved verses.");
						builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
                                VerseDB db = new VerseDB(context).open();
                                db.importFromBackupFile(externalStorage);
                                db.close();
                                Toast.makeText(context, "Restore Successful", Toast.LENGTH_LONG).show();
							}
						});
						builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

							}
						});

						AlertDialog dialog = builder.create();
						dialog.show();
					}
					else {
						Toast.makeText(context, "No Suitable Backup Exists", Toast.LENGTH_LONG).show();
					}
				}
			}
			return false;
		}
	};

//Verse of the Day Preference Listeners
//------------------------------------------------------------------------------
	OnPreferenceChangeListener VOTDCheckedChange = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if((Boolean) newValue) {
				Calendar now = Calendar.getInstance();

				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(VOTDSettings.getNotificationTime(context));

				//if the set time is in the past due to the time being set before
				//today, then set the next time to fire the alarm to be tomorrow
				if(calendar.getTimeInMillis() < now.getTimeInMillis()) {
					calendar.set(Calendar.DATE, now.get(Calendar.DATE) + 1);
					VOTDSettings.setNotificationTime(context, calendar.getTimeInMillis());
				}

				VOTDNotification.getInstance(context).setAlarm();
				Toast.makeText(context, "Notification will show daily at " + findPreference("VOTD_TIME").getSummary(), Toast.LENGTH_LONG).show();
			}
			else {
				VOTDNotification.getInstance(context).cancelAlarm();
				Toast.makeText(context, "Notification disabled ", Toast.LENGTH_LONG).show();
			}

			return true;
		}
	};

	OnPreferenceChangeListener VOTDTimeChange = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
            VOTDNotification.getInstance(context).setAlarm();
			Toast.makeText(context, "Notification will show daily at " + preference.getSummary(), Toast.LENGTH_LONG).show();

			return false;
		}
	};

//Default Screen and Theme changes
//------------------------------------------------------------------------------
	OnPreferenceChangeListener defaultScreenChange = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String[] screens = getResources().getStringArray(R.array.pref_default_screen);
			int selection = Integer.parseInt(newValue.toString());

			switch(selection) {
			case 0:
				preference.setSummary(screens[0]);
				break;
			case 1:
				preference.setSummary(screens[2]);
				break;
			case 2:
				preference.setSummary(screens[3]);
				break;
			case 3:
				preference.setSummary(screens[4]);
				break;
			case 4:
				preference.setSummary(screens[5]);
				break;
			case 5:
				preference.setSummary(screens[1]);
				break;
			}

			VerseDB db = new VerseDB(context).open();

			if(selection == 3) {
				prefDefaultScreenChild.setEnabled(true);
				prefDefaultScreenChild.setSummary(db.getStateName(MetaSettings.getDefaultScreen(context).second));

				String[] states = getResources().getStringArray(R.array.state_names);
				String[] statesValues = getResources().getStringArray(R.array.state_ids);
				prefDefaultScreenChild.setEntries(states);
				prefDefaultScreenChild.setEntryValues(statesValues);
			}
			else if(selection == 4) {
				prefDefaultScreenChild.setEnabled(true);
				prefDefaultScreenChild.setSummary(db.getTag(MetaSettings.getDefaultScreen(context).second).name);

				ArrayList<Tag> tags = db.getAllTags();

				String[] tagValues = new String[tags.size()];
				String[] tagNames = new String[tags.size()];
				for(int i = 0; i < tagValues.length; i++) {
					tagValues[i] = Integer.toString(tags.get(i).id);
					tagNames[i] = tags.get(i).name;
				}

				prefDefaultScreenChild.setEntries(tagNames);
				prefDefaultScreenChild.setEntryValues(tagValues);
			}
			else {
				prefDefaultScreenChild.setEnabled(false);
			}

			db.close();

			return true;
		}
	};

	OnPreferenceChangeListener defaultScreenChildChange = new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            int newId = Integer.parseInt((String) newValue);

            VerseDB db = new VerseDB(context).open();

            if(MetaSettings.getDefaultScreen(context).first == 3) {
                prefDefaultScreenChild.setSummary(db.getStateName(newId));
            }
            else if(MetaSettings.getDefaultScreen(context).first == 4) {
                prefDefaultScreenChild.setSummary(db.getTag(newId).name);
            }

            db.close();

            return true;
        }
    };

	OnPreferenceChangeListener appThemeChange = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			preference.setSummary(((String) newValue).substring(6));

			Intent intent = getActivity().getIntent();
            getActivity().overridePendingTransition(0, android.R.anim.fade_out);
            getActivity().finish();
            getActivity().overridePendingTransition(android.R.anim.fade_in, 0);
			startActivity(intent);

			return true;
		}
	};

//Bible Version Changes
//------------------------------------------------------------------------------
	OnPreferenceChangeListener languageChange = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String language = newValue.toString();

			preference.setSummary(availableLanguages.get(language));
			MetaSettings.putBibleLanguage(context, language);

			new AsycnBibleVersions(newValue.toString()).execute();

			return true;
		}
	};

	OnPreferenceChangeListener versionChange = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, final Object newValue) {
			ABSBible newlySelectedBible = null;

			if(availableBibles.get(newValue.toString()) != null) {
				newlySelectedBible = (ABSBible) availableBibles.get(newValue.toString());
			}
			else {
				for(String key : availableBibles.keySet()) {
					Bible bible = availableBibles.get(key);
					if(bible != null) {
						newlySelectedBible = (ABSBible) bible;
						break;
					}
				}
			}

			if(newlySelectedBible == null) {
				preference.setSummary("Not available");
				preference.setEnabled(false);
			}
			else {
				Log.i("NEW VERSION SELECTED", newlySelectedBible.getId());
				MetaSettings.putBibleVersion(context, newlySelectedBible);
				preference.setSummary(newlySelectedBible.getName());
				preference.setEnabled(true);

				new DownloadVersionInfo().execute(newlySelectedBible.getId());
			}

			return true;
		}
	};

	public class AsycnBibleLanguages extends AsyncTask<Void, Void, Void> {
		AlertDialog dialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			View view = LayoutInflater.from(context).inflate(R.layout.popup_progress, null);
			final AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setView(view);

			TextView tv = (TextView) view.findViewById(R.id.title);
			tv.setText("Retrieving Available Languages");

			view.findViewById(R.id.cancel_button).setVisibility(View.GONE);

			dialog = builder.create();
			dialog.show();
			dialog.setCancelable(false);
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				String languagesFile = "languages.xml";

				Document languageDoc = Util.getChachedDocument(context, languagesFile);
				if(languageDoc != null) {
					Log.i("ASYNC BIBLE LANGUAGES", "Parsing cached languages.xml file.");
					availableLanguages = ABSBible.getAvailableLanguages(languageDoc);
				}
				else if(Util.isConnected(context)) {
					Log.i("ASYNC BIBLE LANGUAGES", "No cached languages.xml file, downloading.");

					languageDoc = ABSBible.availableVersionsDoc(
							context.getResources().getString(R.string.bibles_org),
							null
					);

					Util.cacheDocument(context, languageDoc, languagesFile);
					availableLanguages = ABSBible.getAvailableLanguages(languageDoc);
				}
				else {
					Log.i("ASYNC BIBLE LANGUAGES", "No cached languages.xml file and no internet, resorting to default language of eng-us");

					HashMap<String, String> defaultEnglish = new HashMap<>();
					defaultEnglish.put("English (US)", "eng-us");
					availableLanguages = defaultEnglish;
				}
			}
			catch(IOException ioe) {
				ioe.printStackTrace();
				return null;
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			ListPreference selectLanguage = (ListPreference) findPreference("PREF_SELECTED_VERSION_LANGUAGE");

			ArrayList<Pair<String, String>> languagePairs = new ArrayList<>();

			//reserve English languages to put to the top of the list
			for(String key : availableLanguages.keySet()) {
				languagePairs.add(new Pair<>(key, availableLanguages.get(key)));
			}
			Comparator<Pair<String, String>> pairComparator = new Comparator<Pair<String, String>>() {
				@Override
				public int compare(Pair<String, String> lhs, Pair<String, String> rhs) {
					int compareFirst = lhs.first.compareTo(rhs.first);
					int compareSecond = lhs.second.compareTo(rhs.second);

					if(lhs.second.contains("English (")) return -2147483648;
					if(rhs.second.contains("English (")) return 2147483647;

					if(compareSecond == 0) {
						return compareFirst;
					}
					else return compareSecond;
				}
			};
			Collections.sort(languagePairs, pairComparator);

			String[] entryValues = new String[languagePairs.size()]; //keys
			String[] entries = new String[languagePairs.size()]; //values
			for(int i = 0; i < languagePairs.size(); i++) {
				entryValues[i] = languagePairs.get(i).first; //keys
				entries[i] = languagePairs.get(i).second; //values
			}

			selectLanguage.setEntries(entries); //values (display to user)
			selectLanguage.setEntryValues(entryValues); //keys

//			int storedSelection = selectLanguage.findIndexOfValue(MetaSettings.getBibleLanguage(context));
//			selectLanguage.setValueIndex(storedSelection);

			selectLanguage.setOnPreferenceChangeListener(languageChange);
			languageChange.onPreferenceChange(selectLanguage, MetaSettings.getBibleLanguage(context));
			dialog.dismiss();
		}
	}

	public class AsycnBibleVersions extends AsyncTask<Void, Void, Void> {
		AlertDialog dialog;
		String langCode;

		public AsycnBibleVersions(String langCode) {
			this.langCode = langCode;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			View view = LayoutInflater.from(context).inflate(R.layout.popup_progress, null);
			final AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setView(view);

			TextView tv = (TextView) view.findViewById(R.id.title);
			tv.setText("Retrieving Available Bibles");

			view.findViewById(R.id.cancel_button).setVisibility(View.GONE);

			dialog = builder.create();
			dialog.show();
			dialog.setCancelable(false);
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				String versionsFile = "versions";
				if(langCode != null) versionsFile += ":" + langCode;
				versionsFile += ".xml";

				Document doc = Util.getChachedDocument(context, versionsFile);
				if(doc != null) {
					availableBibles = ABSBible.parseAvailableVersions(doc);
				}
				else if(Util.isConnected(context)) {
					doc = ABSBible.availableVersionsDoc(
							context.getResources().getString(R.string.bibles_org),
							langCode
					);
					Util.cacheDocument(context, doc, versionsFile);
					availableBibles = ABSBible.parseAvailableVersions(doc);
				}
				else {
					HashMap<String, Bible> defaultESV = new HashMap<>();
					defaultESV.put("ESV", new Bible());
					availableBibles = defaultESV;
				}
			}
			catch(IOException ioe) {
				ioe.printStackTrace();
				return null;
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			ListPreference selectVersion = (ListPreference) findPreference("PREF_SELECTED_VERSION");

			ArrayList<Pair<String, Bible>> biblePairs = new ArrayList<>();

			//reserve English languages to put to the top of the list
			for(String key : availableBibles.keySet()) {
				Log.i("ASYNC BIBLE VERSIONS", "AvailableBibles Keyset: [" + key + "]");

				biblePairs.add(new Pair<>(key, availableBibles.get(key)));
			}
			Comparator<Pair<String, Bible>> pairComparator = new Comparator<Pair<String, Bible>>() {
				@Override
				public int compare(Pair<String, Bible> lhs, Pair<String, Bible> rhs) {
					int compareFirst = lhs.first.compareTo(rhs.first);
					int compareSecond = lhs.second.getName().compareTo(rhs.second.getName());

					if(compareSecond == 0) {
						return compareFirst;
					}
					else return compareSecond;
				}
			};
			Collections.sort(biblePairs, pairComparator);

			String[] entryValues = new String[biblePairs.size()]; //keys
			String[] entries = new String[biblePairs.size()]; //values
			for(int i = 0; i < biblePairs.size(); i++) {
				entryValues[i] = biblePairs.get(i).first; //keys
				entries[i] = biblePairs.get(i).second.getName(); //values

				Log.i("ASYNC BIBLE VERSIONS", "Key [" + entryValues[i] + "] - Value [" + entries[i] + "]");
			}

			selectVersion.setEntries(entries); //values (display to user)
			selectVersion.setEntryValues(entryValues); //keys

			selectVersion.setOnPreferenceChangeListener(versionChange);
			versionChange.onPreferenceChange(selectVersion, MetaSettings.getBibleVersion(context).getId());
			dialog.dismiss();
		}
	}

	private class DownloadVersionInfo extends AsyncTask<String, Void, Void> {

		AlertDialog dialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			View view = LayoutInflater.from(context).inflate(R.layout.popup_progress, null);
			final AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setView(view);

			TextView tv = (TextView) view.findViewById(R.id.title);
			tv.setText("Downloading Version Info");

			view.findViewById(R.id.cancel_button).setVisibility(View.GONE);

			dialog = builder.create();
			dialog.show();
			dialog.setCancelable(false);
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			dialog.dismiss();
		}

		@Override
		protected Void doInBackground(String... params) {
			try {
				if(Util.isConnected(context)) {
					ABSBible bible = (ABSBible) availableBibles.get(params[0]);
					ABSBible downloadableBible = new ABSBible(
						getResources().getString(R.string.bibles_org),
							bible.getId()
					);

					Document doc = downloadableBible.getDocument();

					if(doc != null) {
						Util.cacheDocument(context, doc, "selectedVersion.xml");
						Util.cacheDocument(context, doc, downloadableBible.getId());
					}
				}
			}
			catch(IOException ioe) {
				ioe.printStackTrace();
			}
			return null;
		}
	}
}
