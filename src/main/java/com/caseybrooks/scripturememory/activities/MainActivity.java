package com.caseybrooks.scripturememory.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.VersesDatabase;
import com.caseybrooks.scripturememory.fragments.DashboardFragment;
import com.caseybrooks.scripturememory.fragments.DashboardFragment.onDashboardEditListener;
import com.caseybrooks.scripturememory.fragments.VerseListFragment;
import com.caseybrooks.scripturememory.fragments.VerseListFragment.onListEditListener;

import java.lang.reflect.Field;

public class MainActivity extends ActionBarActivity
		implements onListEditListener, onDashboardEditListener {
//Data members
//------------------------------------------------------------------------------		
    Toolbar tb;
    Context context;

	private String[] drawerItems;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private CharSequence drawerTitle;
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
		getOverflowMenu();
		setupActionBar();
		showFirstTime();
		showPrompt();
		Fragment fragment = new DashboardFragment();
		Bundle data = new Bundle();

        int defaultScreen = MetaSettings.getDefaultScreen(context);

        if(defaultScreen == 0) {
        	fragment = new DashboardFragment();
        	this.title = "Dashboard";
    		tb.setTitle(title);
        }
        else if(defaultScreen == 1) {
        	fragment = new VerseListFragment();
	    	data.putString("KEY_LIST", "current");
	    	fragment.setArguments(data);
	    	this.title = "Current Verses";
			tb.setTitle(title);
        }
        else if(defaultScreen == 2) {
        	fragment = new VerseListFragment();
	    	data.putString("KEY_LIST", "memorized");
	    	fragment.setArguments(data);
	    	this.title = "Memorized Verses";
			tb.setTitle(title);
        }
		setFragment(fragment);

		VerseListFragment.setOnListEditListener(this);
		DashboardFragment.setOnDashboardEditListener(this);

		setupNavDrawer();
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
			MetaSettings.putVerseId(context, 1);

            String[] refs = getResources().getStringArray(R.array.init_references);
            String[] verses = getResources().getStringArray(R.array.init_verses);

			VersesDatabase entry = new VersesDatabase(this);
			entry.open();

            for(int i = 0; i < refs.length; i++) {
                //entry.createEntry(refs[i], verses[i], "current");
            }
			entry.close();
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

	private void setupActionBar() {
        tb = (Toolbar) findViewById(R.id.activity_toolbar);
        setSupportActionBar(tb);

        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
    	ab.setDisplayHomeAsUpEnabled(true);
	}

	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_dashboard_add).setVisible(false);
        menu.findItem(R.id.menu_dashboard_votd).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu_dashboard, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case android.R.id.home:
	    	if(drawerLayout.isDrawerOpen(drawerList)) {
	    		drawerLayout.closeDrawer(drawerList);
		    }
	    	else {
	    		drawerLayout.openDrawer(drawerList);
	    	}
	    	return true;
        default:
            return super.onOptionsItemSelected(item);
	    }
	}

//Navigation Drawer
//------------------------------------------------------------------------------
	private void setupNavDrawer() {
		drawerItems = getResources().getStringArray(R.array.drawer_items);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);

        drawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, drawerItems));
        drawerList.setOnItemClickListener(new MainActivity.DrawerItemClickListener());

        title = drawerTitle = getTitle();
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(
        		this,
        		drawerLayout,
                tb,
                R.string.drawer_open,
                R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                tb.setTitle(title);
                supportInvalidateOptionsMenu();
            }

			public void onDrawerOpened(View drawerView) {
                tb.setTitle(drawerTitle);
                supportInvalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

    }

	private class DrawerItemClickListener implements ListView.OnItemClickListener {

	    @Override
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	        selectItem(position);
	    }
	}

	private void selectItem(int position) {
	    Fragment fragment = new DashboardFragment();
	    Bundle data = new Bundle();

	    switch(position) {
	    case 0:
	    	fragment = new DashboardFragment();
	    	break;
//        case 1:
//            fragment = new SearchFragment();
//            break;
	    case 1:
	    	fragment = new VerseListFragment();
	    	data.putString("KEY_LIST", "current");
	    	break;
	    case 2:
	    	fragment = new VerseListFragment();
	    	data.putString("KEY_LIST", "memorized");
//			fragment = new SearchFragment();
	    	break;
	    case 3:
	    	Intent settings = new Intent(MainActivity.this, Settings.class);
            startActivity(settings);
            finish();
            break;
	    default:
	    }

	    fragment.setArguments(data);
	    setFragment(fragment);
	    drawerList.setItemChecked(position, true);
	    setTitle(drawerItems[position]);
	    drawerLayout.closeDrawer(drawerList);
	}

	public void setFragment(Fragment fragment) {
		FragmentManager fragmentManager = getSupportFragmentManager();
	    fragmentManager.beginTransaction()
	    			   .setCustomAnimations(R.anim.push_up_in, 0)
	                   .replace(R.id.mainFragmentContainer, fragment)
	                   .commit();
	}

	@Override
	public void setTitle(CharSequence title) {
	    this.title = title;
		tb.setTitle(title);
	}

	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
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
}