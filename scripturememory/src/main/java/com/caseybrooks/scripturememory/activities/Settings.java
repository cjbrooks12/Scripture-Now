package com.caseybrooks.scripturememory.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.KeyEvent;
import android.widget.Toast;

import com.caseybrooks.androidbibletools.enumeration.Version;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.VersesDatabase;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.notifications.VOTDNotification;
import com.caseybrooks.scripturememory.widgets.VOTDWidget;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Settings extends PreferenceActivity {
	Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		int theme = MetaSettings.getAppTheme(this);
		if(theme == 0) setTheme(R.style.Theme_ScriptureMemory_Light);
		else setTheme(R.style.Theme_ScriptureMemory_Dark);
		
		super.onCreate(savedInstanceState);

		context = this;

		addPreferencesFromResource(R.xml.preferences);	

		findPreference("CONTACT_BUG").setOnPreferenceClickListener(bugClick);
		findPreference("CONTACT_FEATURE").setOnPreferenceClickListener(featureClick);
		findPreference("CONTACT_COMMENT").setOnPreferenceClickListener(commentClick);
		findPreference("RateThisApp").setOnPreferenceClickListener(rateAppClick);
		findPreference("ShareApp").setOnPreferenceClickListener(shareAppClick);
		findPreference("Backup").setOnPreferenceClickListener(backupClick);
		findPreference("Restore").setOnPreferenceClickListener(restoreClick);
		findPreference("PREF_VOTD_NOTIFICATION").setOnPreferenceChangeListener(VOTDCheckedChange);
		findPreference("PREF_VOTD_TIME").setOnPreferenceChangeListener(VOTDTimeChange);

		ListPreference appTheme = (ListPreference) findPreference("PREF_SELECTED_THEME");
		appTheme.setOnPreferenceChangeListener(appThemeChange);
		String[] themes = getResources().getStringArray(R.array.pref_themes);
		int appThemeSelection = MetaSettings.getAppTheme(this);
		appTheme.setSummary(themes[appThemeSelection]);

		//Populate Version list with versions available in AndroidBibleTools
		ListPreference selectVersion = (ListPreference) findPreference("PREF_SELECTED_VERSION");
		selectVersion.setOnPreferenceChangeListener(versionChange);
		selectVersion.setSummary(MetaSettings.getBibleVersion(context).getName());
		selectVersion.setEntries(Version.getAllNames());
		selectVersion.setEntryValues(Version.getAllNames());

		ListPreference prefDefaultScreen = (ListPreference) findPreference("PREF_DEFAULT_SCREEN");
		int defaultScreen = MetaSettings.getDefaultScreen(this);
		String[] screens = getResources().getStringArray(R.array.pref_default_screen);
		prefDefaultScreen.setSummary(screens[defaultScreen]);
		prefDefaultScreen.setOnPreferenceChangeListener(defaultScreenChange);
	}
	
	//Workaround because the theme does not carry to nested PreferenceScreen levels
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		super.onPreferenceTreeClick(preferenceScreen, preference);
		if (preference != null) {
			if (preference instanceof PreferenceScreen) {
				if (((PreferenceScreen) preference).getDialog() != null) {
					((PreferenceScreen) preference)
							.getDialog()
							.getWindow()
							.getDecorView()
							.setBackgroundDrawable(
									this
									.getWindow()
									.getDecorView()
									.getBackground()
									.getConstantState()
									.newDrawable()
							);
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			Intent i = new Intent(this, MainActivity.class);
			startActivity(i);
		}
		return super.onKeyDown(keyCode, event);
	}

//Rate App (go to Play Store) listener
//------------------------------------------------------------------------------	
	OnPreferenceClickListener rateAppClick = new OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			final Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
			final Intent rateAppIntent = new Intent(Intent.ACTION_VIEW, uri);

			if (getPackageManager().queryIntentActivities(rateAppIntent, 0).size() > 0)
			{
				startActivity(rateAppIntent);
			}
			else
			{
				Toast.makeText(getBaseContext(), "Google Play not installed", Toast.LENGTH_LONG).show();
			}
			return false;
		}
	};

	OnPreferenceClickListener shareAppClick = new OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			MetaSettings.putPromptOnStart(context, false);

			String shareMessage = getResources().getString(R.string.share_message);
			Intent intent = new Intent();
			intent.setType("text/plain");
			intent.setAction(Intent.ACTION_SEND);
			intent.putExtra(Intent.EXTRA_SUBJECT, "Try Scripture Memory Notifications for Android");
			intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
			startActivity(Intent.createChooser(intent, "Share To..."));

			return false;
		}
	};
	
//Contact Developer Preference Listeners
//------------------------------------------------------------------------------
	OnPreferenceClickListener bugClick = new OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			
			//Gather device information
			String info = 
					"Carrier: " + 
					Build.BRAND + " " + 
					Build.DEVICE + "\n" +
					"Device: " + 
					Build.MANUFACTURER + " " + 
					Build.MODEL + "\n" + 
					"Kernel: " + 
					Build.DISPLAY + "\n" +
					"----------------\n" + 
					"Describe Bug: ";
			
			Intent Email = new Intent(Intent.ACTION_SEND);
			Email.setType("getText/email");
			Email.putExtra(Intent.EXTRA_EMAIL, new String[] { "scripture.memory.app@gmail.com" });
			Email.putExtra(Intent.EXTRA_SUBJECT, "Scripture Memory Notifications: Report Bug");
			Email.putExtra(Intent.EXTRA_TEXT, info);
			startActivity(Intent.createChooser(Email, "Send Feedback:"));
			
			return false;
		}
	};
	
	OnPreferenceClickListener featureClick = new OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			
			//Gather device information
			String info = 
					"Suggested Feature: ";
			
			Intent Email = new Intent(Intent.ACTION_SEND);
			Email.setType("getText/email");
			Email.putExtra(Intent.EXTRA_EMAIL, new String[] { "scripture.memory.app@gmail.com" });
			Email.putExtra(Intent.EXTRA_SUBJECT, "Scripture Memory Notifications: Suggest Feature");
			Email.putExtra(Intent.EXTRA_TEXT, info);
			startActivity(Intent.createChooser(Email, "Send Feedback:"));
			
			return false;
		}
	};
	
	OnPreferenceClickListener commentClick = new OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			
			//Gather device information
			String info = 
					"Comments: ";
			
			Intent Email = new Intent(Intent.ACTION_SEND);
			Email.setType("getText/email");
			Email.putExtra(Intent.EXTRA_EMAIL, new String[] { "scripture.memory.app@gmail.com" });
			Email.putExtra(Intent.EXTRA_SUBJECT, "Scripture Memory Notifications: Comment");
			Email.putExtra(Intent.EXTRA_TEXT, info);
			startActivity(Intent.createChooser(Email, "Send Feedback:"));
			
			return false;
		}
	};
	
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
					VersesDatabase db = new VersesDatabase(getBaseContext());
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
								Toast.makeText(getBaseContext(), "Backup Successful", Toast.LENGTH_LONG).show();
							}
						}
						else {
							Toast.makeText(getBaseContext(), "Unable to open external storage. Check if SD card is installed properly", Toast.LENGTH_LONG).show();
						}
					}
					catch(IOException e) {
						Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
									VersesDatabase db = new VersesDatabase(getBaseContext());
									db.open();
									db.importFromCSV(externalStorage);
									db.close();
									Toast.makeText(getBaseContext(), "Restore Successful", Toast.LENGTH_LONG).show();
								}
								catch(FileNotFoundException e) {
									Toast.makeText(getBaseContext(), "No Suitable Backup Exists", Toast.LENGTH_LONG).show();
								}
								catch(IOException e) {
									Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
						Toast.makeText(getBaseContext(), "No Suitable Backup Exists", Toast.LENGTH_LONG).show();
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
			VOTDNotification.setAlarm(getApplicationContext());
			
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

			Intent intent = getIntent();
			overridePendingTransition(0, android.R.anim.fade_out);
			finish();
			overridePendingTransition(android.R.anim.fade_in, 0);
			startActivity(intent);

			return true;
		}
	};

	OnPreferenceChangeListener versionChange = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			ListPreference lp = (ListPreference) preference;
			lp.setSummary(newValue.toString());

			sendBroadcast(new Intent(VOTDWidget.REFRESH_ALL));

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

			return true;
		}
	};
}
