package com.caseybrooks.scripturememory.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.support.v7.app.ActionBar;
import android.util.TypedValue;
import android.widget.Toast;

import com.caseybrooks.androidbibletools.enumeration.Version;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.activities.DetailActivity;
import com.caseybrooks.scripturememory.activities.MainActivity;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.data.VersesDatabase;
import com.caseybrooks.scripturememory.misc.PreferenceFragment;
import com.caseybrooks.scripturememory.notifications.VOTDNotification;
import com.caseybrooks.scripturememory.widgets.VOTDWidget;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SettingsFragment extends PreferenceFragment {
	Context context;

    ListPreference prefDefaultScreenList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = getActivity();

		addPreferencesFromResource(R.xml.settings);

		findPreference("Backup").setOnPreferenceClickListener(backupClick);
		findPreference("Restore").setOnPreferenceClickListener(restoreClick);
        findPreference("Import").setOnPreferenceClickListener(importClick);
        findPreference("PREF_VOTD_NOTIFICATION").setOnPreferenceChangeListener(VOTDCheckedChange);
		findPreference("PREF_VOTD_TIME").setOnPreferenceChangeListener(VOTDTimeChange);

		ListPreference appTheme = (ListPreference) findPreference("PREF_SELECTED_THEME");
		appTheme.setOnPreferenceChangeListener(appThemeChange);
		String[] themes = getResources().getStringArray(R.array.pref_themes);
		int appThemeSelection = MetaSettings.getAppTheme(context);
		appTheme.setSummary(themes[appThemeSelection]);

		//Populate Version list with versions available in AndroidBibleTools
		ListPreference selectVersion = (ListPreference) findPreference("PREF_SELECTED_VERSION");
		selectVersion.setOnPreferenceChangeListener(versionChange);
		selectVersion.setSummary(MetaSettings.getBibleVersion(context).getName());
		selectVersion.setEntries(Version.getAllNames());
		selectVersion.setEntryValues(Version.getAllNames());

		ListPreference prefDefaultScreen = (ListPreference) findPreference("PREF_DEFAULT_SCREEN");
		int defaultScreen = MetaSettings.getDefaultScreen(context);
		String[] screens = getResources().getStringArray(R.array.pref_default_screen);
		prefDefaultScreen.setSummary(screens[defaultScreen]);
		prefDefaultScreen.setOnPreferenceChangeListener(defaultScreenChange);

        prefDefaultScreenList = (ListPreference) findPreference("PREF_DEFAULT_SCREEN_LIST");

        if(MetaSettings.getDefaultScreen(context) == 3) {
            prefDefaultScreenList.setEnabled(true);

            VerseDB db = new VerseDB(context).open();
            prefDefaultScreenList.setSummary(db.getStateName(MetaSettings.getDefaultScreenList(context)));
            db.close();

            String[] states = getResources().getStringArray(R.array.state_names);
            String[] statesValues = getResources().getStringArray(R.array.state_ids);
            prefDefaultScreenList.setEntries(states);
            prefDefaultScreenList.setEntryValues(statesValues);
        }
        else if(MetaSettings.getDefaultScreen(context) == 4) {
            prefDefaultScreenList.setEnabled(true);
            VerseDB db = new VerseDB(context).open();
            prefDefaultScreenList.setSummary(db.getTagName(MetaSettings.getDefaultScreenList(context)));

            String[] tags = db.getAllTagNames();

            String[] tagValues = new String[tags.length];
            for(int i = 0; i < tagValues.length; i++) {
                tagValues[i] = Long.toString(db.getTagID(tags[i]));
            }

            prefDefaultScreenList.setEntries(tags);
            prefDefaultScreenList.setEntryValues(tagValues);
            db.close();
        }
        else {
            prefDefaultScreenList.setEnabled(false);
            prefDefaultScreenList.setSummary("Not Available");
        }

        prefDefaultScreenList.setOnPreferenceChangeListener(defaultListChange);
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar ab = ((MainActivity) context).getSupportActionBar();
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.color_toolbar, typedValue, true);
        int color = typedValue.data;
        ColorDrawable colorDrawable = new ColorDrawable(color);
        ab.setBackgroundDrawable(colorDrawable);
        ab.setTitle("Settings");

        MetaSettings.putDrawerSelection(context, 4, 0);
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
					VersesDatabase db = new VersesDatabase(context);
					db.open();
					try {
						String state = Environment.getExternalStorageState();
						if (Environment.MEDIA_MOUNTED.equals(state)) {
							String path = Environment.getExternalStorageDirectory().getPath() + "/scripturememory";
							File folder = new File(path);

							if(!folder.exists()) {
								folder.mkdirs();
							}
							if(folder.exists()) {
								File externalStorage = new File(path, "backup.csv");
								db.exportToCSV(externalStorage);
								Toast.makeText(context, "Backup Successful", Toast.LENGTH_LONG).show();
							}
						}
						else {
							Toast.makeText(context, "Unable to open external storage. Check if SD card is installed properly", Toast.LENGTH_LONG).show();
						}
					}
					catch(IOException e) {
						Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
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
					final File externalStorage = new File(path, "backup.csv");
					if(externalStorage.exists()) {
						AlertDialog.Builder builder = new AlertDialog.Builder(context);
						builder.setCancelable(true);
						builder.setTitle("Restore Verses");
						builder.setMessage("Restore verses from backup? This will replace all currently saved verses.");
						builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								try {
									VersesDatabase db = new VersesDatabase(context);
									db.open();
									db.importFromCSV(externalStorage);
                                    db.migrate();
									db.close();
									Toast.makeText(context, "Restore Successful", Toast.LENGTH_LONG).show();
								}
								catch(FileNotFoundException e) {
									Toast.makeText(context, "No Suitable Backup Exists", Toast.LENGTH_LONG).show();
								}
								catch(IOException e) {
									Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
								}
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

    OnPreferenceClickListener importClick = new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Intent intent = new Intent(context, DetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("FRAGMENT", 1);
            intent.putExtras(bundle);
            startActivity(intent);

            return false;
        }
    };
	
//Verse of the Day Preference Listeners
//------------------------------------------------------------------------------
	OnPreferenceChangeListener VOTDCheckedChange = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if((Boolean) newValue) {
				//If user turned notifications on
				VOTDNotification.setAlarm(context);
			}
			else {
				//If user turned notifications off
//				new VOTDNotification(getApplicationContext()).cancelAlarm();
			}
			
			return true;
		}
	};
	
	OnPreferenceChangeListener VOTDTimeChange = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			VOTDNotification.setAlarm(context);
			
			return false;
		}
	};

    OnPreferenceChangeListener defaultListChange = new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            int newId = Integer.parseInt((String) newValue);
            int currentGroup = MetaSettings.getDefaultScreen(context);

            VerseDB db = new VerseDB(context).open();

            if(currentGroup == 3) {
                prefDefaultScreenList.setSummary(db.getStateName(newId));
            }
            else {
                prefDefaultScreenList.setSummary(db.getTagName(newId));
            }

            db.close();

            return false;
        }
    };

	OnPreferenceChangeListener appThemeChange = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			ListPreference lp = (ListPreference) preference;
			String[] themes = getResources().getStringArray(R.array.pref_themes);
			int selection = Integer.parseInt(newValue.toString());
			lp.setSummary(themes[selection]);

			Intent intent = getActivity().getIntent();
            getActivity().overridePendingTransition(0, android.R.anim.fade_out);
            getActivity().finish();
            getActivity().overridePendingTransition(android.R.anim.fade_in, 0);
			startActivity(intent);

			return true;
		}
	};

	OnPreferenceChangeListener versionChange = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			ListPreference lp = (ListPreference) preference;
			lp.setSummary(newValue.toString());

            getActivity().sendBroadcast(new Intent(VOTDWidget.REFRESH_ALL));

			return true;
		}
	};

	OnPreferenceChangeListener defaultScreenChange = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			ListPreference lp = (ListPreference) preference;
			String[] screens = getResources().getStringArray(R.array.pref_default_screen);
			int selection = Integer.parseInt(newValue.toString());
			lp.setSummary(screens[selection]);

            VerseDB db = new VerseDB(context).open();


            if(selection == 3) {
                prefDefaultScreenList.setEnabled(true);
                prefDefaultScreenList.setSummary(db.getStateName(MetaSettings.getDefaultScreenList(context)));

                String[] states = getResources().getStringArray(R.array.state_names);
                String[] statesValues = getResources().getStringArray(R.array.state_ids);
                prefDefaultScreenList.setEntries(states);
                prefDefaultScreenList.setEntryValues(statesValues);
            }
            else if(selection == 4) {
                prefDefaultScreenList.setEnabled(true);
                prefDefaultScreenList.setSummary(db.getTagName(MetaSettings.getDefaultScreenList(context)));

                String[] tags = db.getAllTagNames();

                String[] tagValues = new String[tags.length];
                for(int i = 0; i < tagValues.length; i++) {
                    tagValues[i] = db.getTagID(tags[i]) + "";
                }

                prefDefaultScreenList.setEntries(tags);
                prefDefaultScreenList.setEntryValues(tagValues);
            }
            else {
                prefDefaultScreenList.setEnabled(false);
            }

            db.close();

			return true;
		}
	};
}
