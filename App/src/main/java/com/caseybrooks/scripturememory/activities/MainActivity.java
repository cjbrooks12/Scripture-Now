package com.caseybrooks.scripturememory.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.caseybrooks.androidbibletools.providers.abs.ABSBible;
import com.caseybrooks.common.features.NavigationCallbacks;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.fragments.BibleReaderFragment;
import com.caseybrooks.scripturememory.fragments.DashboardFragment;
import com.caseybrooks.scripturememory.fragments.DebugCacheFragment;
import com.caseybrooks.scripturememory.fragments.DebugDatabaseFragment;
import com.caseybrooks.scripturememory.fragments.DebugSharedPreferencesFragment;
import com.caseybrooks.scripturememory.fragments.HelpFragment;
import com.caseybrooks.scripturememory.fragments.ImportVersesFragment;
import com.caseybrooks.scripturememory.fragments.NavigationDrawerFragment;
import com.caseybrooks.scripturememory.fragments.SettingsFragment;
import com.caseybrooks.scripturememory.fragments.TopicalBibleFragment;
import com.caseybrooks.scripturememory.fragments.VerseListFragment;
import com.caseybrooks.scripturememory.misc.CacheCleaner;
import com.caseybrooks.scripturememory.nowcards.main.MainSettings;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class MainActivity extends AppCompatActivity implements NavigationCallbacks {
//Data members
//------------------------------------------------------------------------------
    Toolbar toolbar;
    FrameLayout expandedToolbar;
    Context context;

//Lifecycle and Initialization
//------------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        context = this;

		setTheme();

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        // Set up the drawer
        toolbar = (Toolbar) findViewById(R.id.activity_toolbar);
        expandedToolbar = (FrameLayout) findViewById(R.id.activity_toolbar_expanded);

        setSupportActionBar(toolbar);

        NavigationDrawerFragment mNavigationDrawerFragment = new NavigationDrawerFragment();
        mNavigationDrawerFragment.setUp(this, toolbar,  findViewById(R.id.navigation_drawer_container),
                (DrawerLayout) findViewById(R.id.drawer_layout));

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                //put fragment in navigation drawer
                .add(R.id.navigation_drawer_container, mNavigationDrawerFragment)
                .commit();

                //put DashboardFragment in main content
        fragmentManager.beginTransaction()
                .add(R.id.mainFragmentContainer, new DashboardFragment())
                .commit();

		getOverflowMenu();
		onFirstTime();
        onAppUpgrade();
		showPrompt();
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiveImplicitIntent();
    }

	private void setTheme() {
		try {
			setTheme(R.style.class.getDeclaredField(MetaSettings.getAppTheme(context)).getInt(null));
		}
		catch(NoSuchFieldException nsfe) {
			nsfe.printStackTrace();
		}
		catch(IllegalAccessException iae) {
			iae.printStackTrace();
		}
	}

	//do not support the old backups or old database any longer. If users haven't
	//upgraded by now, then they must not care enough about their verses, and their
	//complaints are not important to the developement of the app.
	private void onFirstTime() {
		boolean firstTime = MetaSettings.getFirstTime(context);
		//If this is the first time opening the app
		if(firstTime) {
			MetaSettings.putFirstTime(context, false);
		}
	}

    public void onAppUpgrade() {
        try {
            int version = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;

			if(version > 27) {
				MetaSettings.putBibleVersion(context, new ABSBible(null, null));
			}

            //we have updated the app
            if(version > MetaSettings.getAppVersion(context)) {
                MetaSettings.putAppVersion(context, version);
                CacheCleaner.setAlarm(context);

				MainSettings.putDisplayMode(context, MainSettings.getDisplayMode(context) - 1);

                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    String path = Environment.getExternalStorageDirectory().getPath() + "/scripturememory";

                    File folder = new File(path);

                    if (!folder.exists()) {
                        folder.mkdirs();
                    }
                    if (folder.exists()) {
                        ArrayList<Source> domSource = new ArrayList<Source>();
                        ArrayList<File> outputStream = new ArrayList<File>();

                        Field[] fields = R.raw.class.getFields();
                        for (Field f : fields) {
                            try {
                                //create objects before adding them to lists.
                                Source source = new StreamSource(getResources().openRawResource(f.getInt(null)));
                                File file = new File(path, f.getName() + ".xml");

                                //after we know both have been made correctly, add to lists
                                domSource.add(source);
                                outputStream.add(file);
                            }
							catch (IllegalArgumentException| IllegalAccessException e) {

                            }
                        }

                        TransformerFactory factory = TransformerFactory.newInstance();
                        Transformer transformer = factory.newTransformer();

                        for (int i = 0; i < outputStream.size(); i++) {
                            transformer.transform(domSource.get(i), new StreamResult(outputStream.get(i)));
                        }
                    }
                }
            }
        }
        catch(PackageManager.NameNotFoundException | TransformerException e) {
            e.printStackTrace();
        }
    }

	private void showPrompt() {
		if(MetaSettings.getPromptOnStart(context) == 3) {
            MetaSettings.putPromptOnStart(context, 4);

            final View view = LayoutInflater.from(context).inflate(R.layout.popup_rate_app, null);
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(view);

            final AlertDialog dialog = builder.create();

            view.findViewById(R.id.share_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String shareMessage = getResources().getString(R.string.share_message);
                    Intent intent = new Intent();
                    intent.setType("text/plain");
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Try Scripture Memory Notifications for Android");
                    intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(intent, "Share To..."));
                    dialog.dismiss();
                }
            });

            view.findViewById(R.id.rate_it_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
					final Intent rateAppIntent = new Intent(Intent.ACTION_VIEW, uri);

					if (getPackageManager().queryIntentActivities(rateAppIntent, 0).size() > 0) {
						startActivity(rateAppIntent);
					}
                    dialog.dismiss();
                }
            });

            view.findViewById(R.id.no_thanks_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "You can always do it again from settings", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            });

            view.findViewById(R.id.remind_me_later_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MetaSettings.putPromptOnStart(context, 0);
                    dialog.dismiss();
                }
            });

            dialog.show();
		}
        else if(MetaSettings.getPromptOnStart(context) < 3){
            MetaSettings.putPromptOnStart(context, MetaSettings.getPromptOnStart(context) + 1);
        }
	}

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private void receiveImplicitIntent() {
        Bundle extras = getIntent().getExtras();
		if(extras != null && extras.containsKey(Intent.EXTRA_TEXT)) {
            DashboardFragment dashboard = new DashboardFragment();
            dashboard.setArguments(extras);
            setFragment(dashboard);
        }
	}

//ActionBar
//------------------------------------------------------------------------------
	//Forces three dot overflow on devices with hardware menu button by reflection
	private void getOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
	}

//Everything to do with new NavDrawerFragment
//------------------------------------------------------------------------------

    public void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.mainFragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

	@Override
	public void setToolBar(String name, int color) {
		ActionBar ab = getSupportActionBar();
		ColorDrawable colorDrawable = new ColorDrawable(color);
		ab.setBackgroundDrawable(colorDrawable);
		ab.setTitle(name);

        expandedToolbar.setBackgroundDrawable(colorDrawable);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			float[] hsv = new float[3];
			Color.colorToHSV(color, hsv);
			hsv[2] *= 0.7f; // value component
			getWindow().setStatusBarColor(Color.HSVToColor(hsv));
		}
	}

    @Override
    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    public void expandToolbarWIthView(View view) {
        expandedToolbar.setVisibility(View.VISIBLE);
        expandedToolbar.addView(view);
    }

    @Override
    public void collapseExpandedToolbar() {
        expandedToolbar.setVisibility(View.GONE);
        expandedToolbar.removeAllViews();
    }

    @Override
    public void toVerseDetail() {

    }

    @Override
    public void toVerseEdit() {
        Intent intent = new Intent(this, DetailActivity.class);
        Bundle extras = new Bundle();
        extras.putInt("FRAGMENT", 0);

        intent.putExtras(extras);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, 0);
    }

    @Override
    public void toVerseList(int listType, int id) {
        Fragment fragment = VerseListFragment.newInstance(listType, id);
        setFragment(fragment);
    }

    @Override
    public void toDashboard() {
        Fragment fragment = new DashboardFragment();
        setFragment(fragment);
    }

    @Override
    public void toTopicalBible() {
        Fragment fragment = TopicalBibleFragment.newInstance();
        setFragment(fragment);
    }

    @Override
    public void toImportVerses() {
        Fragment fragment = ImportVersesFragment.newInstance();
        setFragment(fragment);
    }

    @Override
    public void toSettings() {
        SettingsFragment fragment = new SettingsFragment();
		fragment.setToolbar(toolbar);
        setFragment(fragment);
    }

    @Override
    public void toHelp() {
        Fragment fragment = new HelpFragment();
        setFragment(fragment);
    }

	@Override
	public void toDebugPreferences() {
		Fragment fragment = DebugSharedPreferencesFragment.newInstance();
		setFragment(fragment);
	}

	@Override
	public void toDebugDatabase() {
		Fragment fragment = DebugDatabaseFragment.newInstance();
		setFragment(fragment);
	}

	@Override
	public void toDebugCache() {
		Fragment fragment = DebugCacheFragment.newInstance();
		setFragment(fragment);
	}

    @Override
    public void toBible(int id) {
        Fragment fragment = BibleReaderFragment.newInstance(id);
        setFragment(fragment);
    }
}