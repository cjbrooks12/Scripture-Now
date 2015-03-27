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
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.caseybrooks.androidbibletools.basic.Tag;
import com.caseybrooks.androidbibletools.data.Bible;
import com.caseybrooks.androidbibletools.io.Download;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.Util;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.misc.NavigationCallbacks;
import com.caseybrooks.scripturememory.misc.PreferenceFragment;
import com.caseybrooks.scripturememory.nowcards.votd.VOTD;
import com.caseybrooks.scripturememory.nowcards.votd.VOTDNotification;

import org.jsoup.nodes.Document;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

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
		new AsycnBibleLanguages().execute();

		findPreference("Backup").setOnPreferenceClickListener(backupClick);
		findPreference("Restore").setOnPreferenceClickListener(restoreClick);
        findPreference("VOTD_ENABLED").setOnPreferenceChangeListener(VOTDCheckedChange);
		findPreference("VOTD_TIME").setOnPreferenceChangeListener(VOTDTimeChange);

		ListPreference appTheme = (ListPreference) findPreference("APP_THEME");
		appTheme.setOnPreferenceChangeListener(appThemeChange);

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

			appTheme.setSummary(MetaSettings.getAppTheme(context).substring(6));
			appTheme.setEntries(themeNames);
			appTheme.setEntryValues(themeValues);
		}
		catch(NoSuchFieldException nsfe) {

		}

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
				calendar.setTimeInMillis(VOTD.getNotificationTime(context));

				//if the set time is in the past due to the time being set before
				//today, then set the next time to fire the alarm to be tomorrow
				if(calendar.getTimeInMillis() < now.getTimeInMillis()) {
					calendar.set(Calendar.DATE, now.get(Calendar.DATE) + 1);
					VOTD.setNotificationTime(context, calendar.getTimeInMillis());
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

	OnPreferenceChangeListener languageChange = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			ListPreference lp = (ListPreference) preference;
			lp.setSummary(newValue.toString());

			new AsycnBibleVersions(newValue.toString()).execute();

			return true;
		}
	};

	OnPreferenceChangeListener versionChange = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			ListPreference lp = (ListPreference) preference;
			lp.setSummary(newValue.toString());

			return true;
		}
	};

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
					availableLanguages = Bible.getAvailableLanguages(languageDoc);
				}
				else if(Util.isConnected(context)) {
					languageDoc = Download.availableVersions(
							context.getResources().getString(R.string.bibles_org),
							null
					);

					Whitelist whitelist = new Whitelist();
					whitelist.addTags("version", "lang_name", "lang");

					Cleaner cleaner = new Cleaner(whitelist);
					languageDoc = cleaner.clean(languageDoc);

					Util.cacheDocument(context, languageDoc, languagesFile);
					availableLanguages = Bible.getAvailableLanguages(languageDoc);
				}
				else {
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
			dialog.dismiss();

			ListPreference selectLanguage = (ListPreference) findPreference("PREF_SELECTED_VERSION_LANGUAGE");

			LinkedHashSet<Map.Entry<String, String>> values = new LinkedHashSet<>();
			values.addAll(availableLanguages.entrySet());

			String[] entries = new String[values.size()];
			String[] entryValues = new String[values.size()];

			int i = 0;

			for(Map.Entry<String, String> entry : values) {
				entries[i] = entry.getKey();
				entryValues[i] = entry.getValue();

				i++;
			}

			selectLanguage.setEntries(entries);
			selectLanguage.setEntryValues(entryValues);

			selectLanguage.setOnPreferenceChangeListener(languageChange);
			selectLanguage.setSummary(MetaSettings.getBibleLanguage(context));
			new AsycnBibleVersions(MetaSettings.getBibleLanguage(context)).execute();
		}
	}

	public class AsycnBibleVersions extends AsyncTask<Void, Void, Void> {
		String langCode;
		AlertDialog dialog;

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
					availableBibles = Bible.getAvailableVersions(doc);
				}
				else if(Util.isConnected(context)) {
					doc = Download.availableVersions(
							context.getResources().getString(R.string.bibles_org),
							langCode
					);
					Util.cacheDocument(context, doc, versionsFile);
					availableBibles = Bible.getAvailableVersions(doc);
				}
				else {
					HashMap<String, Bible> defaultESV = new HashMap<>();
					defaultESV.put("ESV", new Bible(null));
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
			dialog.dismiss();

			ListPreference selectVersion = (ListPreference) findPreference("PREF_SELECTED_VERSION");

			ArrayList<Bible> values = new ArrayList<>();
			values.addAll(availableBibles.values());

			String[] entries = new String[values.size()];
			String[] entryValues = new String[values.size()];

			for(int i = 0; i < values.size(); i++) {
				Bible bible = values.get(i);
				entries[i] = bible.name;
				entryValues[i] = bible.getVersionId();
			}

			selectVersion.setEntries(entries);
			selectVersion.setEntryValues(entryValues);

			selectVersion.setOnPreferenceChangeListener(versionChange);
			selectVersion.setSummary(MetaSettings.getBibleVersion(context).abbr);
		}
	}
}
