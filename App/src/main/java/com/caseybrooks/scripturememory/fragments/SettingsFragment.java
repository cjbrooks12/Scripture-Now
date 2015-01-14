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
import android.util.Pair;
import android.util.TypedValue;
import android.widget.Toast;

import com.caseybrooks.androidbibletools.enumeration.Version;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.activities.MainActivity;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.misc.PreferenceFragment;
import com.caseybrooks.scripturememory.notifications.VOTDNotification;
import com.caseybrooks.scripturememory.widgets.VOTDWidget;

import java.io.File;

public class SettingsFragment extends PreferenceFragment {
	Context context;

    ListPreference prefDefaultScreenChild;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = getActivity();

		addPreferencesFromResource(R.xml.settings);

		findPreference("Backup").setOnPreferenceClickListener(backupClick);
		findPreference("Restore").setOnPreferenceClickListener(restoreClick);
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

        Pair<Integer, Integer> defaultScreen = MetaSettings.getDefaultScreen(context);


        ListPreference prefDefaultScreenGroup = (ListPreference) findPreference("PREF_DEFAULT_SCREEN_GROUP");
		String[] screens = getResources().getStringArray(R.array.pref_default_screen);
        prefDefaultScreenGroup.setSummary(screens[defaultScreen.first]);
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
            prefDefaultScreenChild.setSummary(db.getTagName(defaultScreen.second));

            String[] tags = db.getAllTagNames();

            String[] tagValues = new String[tags.length];
            for(int i = 0; i < tagValues.length; i++) {
                tagValues[i] = Long.toString(db.getTagID(tags[i]));
            }

            prefDefaultScreenChild.setEntries(tags);
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

    OnPreferenceChangeListener defaultScreenChildChange = new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            int newId = Integer.parseInt((String) newValue);

            VerseDB db = new VerseDB(context).open();

            if(MetaSettings.getDefaultScreen(context).first == 3) {
                prefDefaultScreenChild.setSummary(db.getStateName(newId));
            }
            else if(MetaSettings.getDefaultScreen(context).first == 4) {
                prefDefaultScreenChild.setSummary(db.getTagName(newId));
            }

            db.close();

            return true;
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
                prefDefaultScreenChild.setEnabled(true);
                prefDefaultScreenChild.setSummary(db.getStateName(MetaSettings.getDefaultScreen(context).second));

                String[] states = getResources().getStringArray(R.array.state_names);
                String[] statesValues = getResources().getStringArray(R.array.state_ids);
                prefDefaultScreenChild.setEntries(states);
                prefDefaultScreenChild.setEntryValues(statesValues);
            }
            else if(selection == 4) {
                prefDefaultScreenChild.setEnabled(true);
                prefDefaultScreenChild.setSummary(db.getTagName(MetaSettings.getDefaultScreen(context).second));

                String[] tags = db.getAllTagNames();

                String[] tagValues = new String[tags.length];
                for(int i = 0; i < tagValues.length; i++) {
                    tagValues[i] = db.getTagID(tags[i]) + "";
                }

                prefDefaultScreenChild.setEntries(tags);
                prefDefaultScreenChild.setEntryValues(tagValues);
            }
            else {
                prefDefaultScreenChild.setEnabled(false);
            }

            db.close();

			return true;
		}
	};
}
