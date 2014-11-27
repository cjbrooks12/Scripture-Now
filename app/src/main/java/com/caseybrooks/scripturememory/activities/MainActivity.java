package com.caseybrooks.scripturememory.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.fragments.DashboardFragment;
import com.caseybrooks.scripturememory.fragments.DashboardFragment.onDashboardEditListener;
import com.caseybrooks.scripturememory.fragments.NavigationDrawerFragment;
import com.caseybrooks.scripturememory.fragments.VerseListFragment;
import com.caseybrooks.scripturememory.fragments.VerseListFragment.onListEditListener;

import java.lang.reflect.Field;

public class MainActivity extends ActionBarActivity
		implements onListEditListener, onDashboardEditListener,
        NavigationDrawerFragment.NavigationDrawerCallbacks {
//Data members
//------------------------------------------------------------------------------		
    Toolbar tb;
    Context context;

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence title;

//Lifecycle and Initialization
//------------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        context = this;

        int theme = MetaSettings.getAppTheme(context);
		if(theme == 0) setTheme(R.style.Theme_BaseLight);
		else setTheme(R.style.Theme_BaseDark);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        title = getTitle();

        // Set up the drawer.
        tb = (Toolbar) findViewById(R.id.activity_toolbar);
        setSupportActionBar(tb);

        mNavigationDrawerFragment = new NavigationDrawerFragment();

        mNavigationDrawerFragment.setUp(this, tb,  findViewById(R.id.navigation_drawer_container),
                (DrawerLayout) findViewById(R.id.drawer_layout));

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.navigation_drawer_container, mNavigationDrawerFragment)
                .commit();

        VerseListFragment.setOnListEditListener(this);
        DashboardFragment.setOnDashboardEditListener(this);

		getOverflowMenu();
		showFirstTime();
		showPrompt();

        int defaultScreen = MetaSettings.getDefaultScreen(context);

        NavigationDrawerFragment.NavListItem item = new NavigationDrawerFragment.NavListItem();
        item.groupPosition = defaultScreen;
        item.childPosition = 0;

        onNavigationDrawerItemSelected(item);
		receiveImplicitIntent();
    }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	    	startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
	    }
	    return super.onKeyDown(keyCode, event);
	}

    //TODO: Setup app for first time opened. Will need to remove all current "first" time methods and make brand new one
	private void showFirstTime() {
		boolean firstTime = MetaSettings.getFirstTime(context);
		//If this is the first time opening the app, load a set of verses to memorize
		if(firstTime) {
			MetaSettings.putFirstTime(context, false);
		}
	}

	private void showPrompt() {
		if(MetaSettings.getPromptOnStart(context) == 3) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setCancelable(true);
			builder.setTitle("Love Scripture Memory?");
			builder.setMessage("Help support this app by rating it on the Google Play Store, or share it with your friends!");
			builder.setPositiveButton("Rate it", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					final Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
					final Intent rateAppIntent = new Intent(Intent.ACTION_VIEW, uri);

					if (getPackageManager().queryIntentActivities(rateAppIntent, 0).size() > 0) {
						startActivity(rateAppIntent);
					}
				}
			});
			builder.setNegativeButton("No, Thanks", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					Toast.makeText(context, "You can always do it again from settings", Toast.LENGTH_LONG).show();
				}
			});
			builder.setNeutralButton("Share", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {

					String shareMessage = getResources().getString(R.string.share_message);
					Intent intent = new Intent();
					intent.setType("text/plain");
					intent.setAction(Intent.ACTION_SEND);
					intent.putExtra(Intent.EXTRA_SUBJECT, "Try Scripture Memory Notifications for Android");
					intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
					startActivity(Intent.createChooser(intent, "Share To..."));

				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					Toast.makeText(context, "You can always do this later from settings", Toast.LENGTH_LONG).show();
				}
			});

            MetaSettings.putPromptOnStart(context, 4);
		    AlertDialog dialog = builder.create();
			dialog.show();
		}
        else if(MetaSettings.getPromptOnStart(context) < 3){
            MetaSettings.putPromptOnStart(context, MetaSettings.getPromptOnStart(context) + 1);
        }
	}

	private void receiveImplicitIntent() {
		Bundle extras = getIntent().getExtras();
		if(extras != null && extras.containsKey(Intent.EXTRA_TEXT)) {
            DashboardFragment dashboard = new DashboardFragment();
            setFragment(dashboard);
            dashboard.setArguments(extras);
        }
	}

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        receiveImplicitIntent();
    }

    //ActionBar
//------------------------------------------------------------------------------
	//Forces three dot overflow on devices with hardware menu button.
	//Some might consider this a hack...
	private void getOverflowMenu() {
	     try {
	        ViewConfiguration config = ViewConfiguration.get(this);
	        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
	        if(menuKeyField != null) {
	            menuKeyField.setAccessible(true);
	            menuKeyField.setBoolean(config, false);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	@Override
	public void setTitle(CharSequence title) {
	    this.title = title;
		tb.setTitle(title);
	}

//Fragment Communication Interface
//------------------------------------------------------------------------------
    @Override
    public void toEdit(int id) {
        Intent intent = new Intent(getBaseContext(), EditVerse.class);
        intent.putExtra("KEY_ID", id);

        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.none);
    }



//Everything to do with new NavDrawerFragment
//------------------------------------------------------------------------------

    public void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.push_up_in, 0)
                .replace(R.id.mainFragmentContainer, fragment)
                .commit();
    }

    @Override
    public void onNavigationDrawerItemSelected(NavigationDrawerFragment.NavListItem item) {
        Fragment fragment = new DashboardFragment();

        switch(item.groupPosition) {
            case 0:
                fragment = new DashboardFragment();
                break;
            case 1:
                fragment = VerseListFragment.newInstance(VerseListFragment.STATE, item.id);
                setTitle(item.name);
                break;
            case 2:
                fragment = VerseListFragment.newInstance(VerseListFragment.TAGS, item.id);
                setTitle(item.name);
                break;
            case 3:
                Intent settings = new Intent(MainActivity.this, Settings.class);
                setTitle(item.name);
                startActivity(settings);
                finish();
                break;
            default:
        }

        setFragment(fragment);
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 0:
                title = "Dashboard";
                break;
            case 1:
                title = "State";
                break;
            case 2:
                title = "Tags";
                break;
            case 3:
                title = "Settings";
                break;
        }
    }
}