package com.caseybrooks.common.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ProgressBar;

import com.caseybrooks.androidbibletools.ABT;
import com.caseybrooks.common.BuildConfig;
import com.caseybrooks.common.R;
import com.caseybrooks.common.fragments.BibleReaderFragment;
import com.caseybrooks.common.fragments.DashboardFragment;
import com.caseybrooks.common.fragments.DebugCache;
import com.caseybrooks.common.fragments.DebugDatabase;
import com.caseybrooks.common.fragments.DebugPreferences;
import com.caseybrooks.common.fragments.EditVerseFragment;
import com.caseybrooks.common.fragments.HelpFragment;
import com.caseybrooks.common.fragments.PrayersFragment;
import com.caseybrooks.common.fragments.SettingsFragment;
import com.caseybrooks.common.fragments.TopicalBibleFragment;
import com.caseybrooks.common.fragments.TopicsListFragment;
import com.caseybrooks.common.fragments.VerseListFragment;
import com.caseybrooks.common.widget.SearchBox;

import java.util.ArrayList;

public class ActivityBase extends AppCompatActivity implements
        ExpandableNavigationView.OnExpandableNavigationItemSelectedListener,
        FragmentManager.OnBackStackChangedListener {
    public String TAG = getClass().getSimpleName();

    private CoordinatorLayout coordinatorLayout;
    private Toolbar toolbar;
    private ProgressBar progressbar;
    private ExpandableNavigationView navView;

    private SearchBox searchbox;

    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private NetworkConnectionReceiver connectionReceiver;

    private boolean wasDisconnected = false;
    private AppFeature selectedFeature;
    private int selectedFeatureId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        progressbar = (ProgressBar) findViewById(R.id.progress);
        navView = (ExpandableNavigationView) findViewById(R.id.expandableNavigationView);
        navView.setExpandableNavigationItemSelectedListener(this);

        searchbox = (SearchBox) findViewById(R.id.searchbox);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        ABT.getInstance(this)
                .getMetadata().putString("ABS_ApiKey", "mDaM8REZFo6itplNpcv1ls8J5PkwEz1wbhJ7p9po");

        if(AppSettings.isFirstInstall(this)) {
            AppSettings.putFirstInstall(this, false);
            onFirstInstall();
        }

        if(BuildConfig.VERSION_CODE > AppSettings.getAppVersion(this)) {
            AppSettings.putAppVersion(this, BuildConfig.VERSION_CODE);
            onAppUpdated();
        }

        getSupportFragmentManager().addOnBackStackChangedListener(this);
        setupFeatures();
        selectDefaultFeature();
    }

    //Method stub that will get called the first time an app is installed
    public void onFirstInstall() {

    }

    //TODO: implement check of the current app version so we can prompt the user to update
    public void isUpdateAvailable() {

    }

    //Method stub that will get called every time the app gets updated
    public void onAppUpdated() {

    }

    public void openSearch(String hint, int menuResourceId, SearchBox.SearchBoxListener listener) {
        searchbox.setHint(hint);
        searchbox.setMenuResource(menuResourceId);
        searchbox.setSearchBoxListener(listener);

        searchbox.reveal(this);
    }

    public void closeSearch() {
        searchbox.hide(this);
    }

    public SearchBox getSearchbox() {
        return searchbox;
    }

    //Setup activity's theme and features
//--------------------------------------------------------------------------------------------------
    public void setTheme() {
        if(AppSettings.getAppTheme(this).equals("Light"))
            setTheme(R.style.ThemeBase_Light_NoActionBar);
        else
            setTheme(R.style.ThemeBase_Dark_NoActionBar);
    }

    private void setupFeatures() {
        ArrayList<AppFeature> features = getFeatures();
        if(features != null && features.size() > 0) {
            if(isDebug())
                features.add(AppFeature.Debug);

            setDrawerEnabled(true);

            ArrayList<ExpandableNavigationView.NavParentItem> parents = new ArrayList<>();
            for(AppFeature feature : features) {
                ExpandableNavigationView.NavParentItem parent = new ExpandableNavigationView.NavParentItem();
                parent.itemText = feature.getTitle();
                parent.itemIconResId = feature.getIconResId();
                parent.appFeature = feature;

                if(feature.hasChildren()) {
                    ArrayList<ExpandableNavigationView.NavChildItem> featureChildren = getChildrenForFeature(feature);
                    parent.setNavChildItemList(featureChildren);
                }
                else {
                    parent.setNavChildItemList(new ArrayList<ExpandableNavigationView.NavChildItem>());
                }
                parents.add(parent);
            }
            navView.setContent(parents);
        }
        else {
            setDrawerEnabled(false);
        }
    }

    private void selectDefaultFeature() {
        Pair<AppFeature, Integer> feature = AppSettings.getDefaultFeature(this);

        if(feature.first == AppFeature.LastVisited)
            feature = AppSettings.getSelectedFeature(this);

        selectAppFeature(feature.first, feature.second);
    }

    public ArrayList<ExpandableNavigationView.NavChildItem> getChildrenForFeature(AppFeature feature) {
        if(feature == AppFeature.Debug) {
            ArrayList<ExpandableNavigationView.NavChildItem> children = new ArrayList<>();

            for(AppFeature debugFeature : new AppFeature[] { AppFeature.DebugDatabase, AppFeature.DebugPreferences, AppFeature.DebugCache }) {
                ExpandableNavigationView.NavChildItem debugItem = new ExpandableNavigationView.NavChildItem();
                debugItem.appFeature = debugFeature;
                debugItem.subitemText = debugFeature.getTitle();
                debugItem.subitemIcon = debugFeature.getIconResId();
                debugItem.subitemIconColor = Color.RED;
                debugItem.subitemCount = 0;
                children.add(debugItem);
            }

            return children;
        }
        else {
            return new ArrayList<>();
        }
    }

//Set look and features of the Toolbar
//--------------------------------------------------------------------------------------------------

    /**
     * Progress values:
     * 0: remove progressbar
     * less than 0: set progressbar to indeterminate
     * greater than 0 && < max: set definite progress to value
     */
    public void setActivityProgress(int progress) {
        if(progress ==0) {
            progressbar.setVisibility(View.INVISIBLE);
        }
        else if(progress < 0) {
            progressbar.setVisibility(View.VISIBLE);
            progressbar.setIndeterminate(true);
        }
        else if(progress >= progressbar.getMax()) {
            progressbar.setVisibility(View.VISIBLE);
            progressbar.setIndeterminate(false);
            progressbar.setProgress(progressbar.getMax());
        }
        else {
            progressbar.setVisibility(View.VISIBLE);
            progressbar.setIndeterminate(false);
            progressbar.setProgress(progress);
        }
    }

    public void setToolBar(String name, int color) {

    }

//Get important components of the activity's UI
//--------------------------------------------------------------------------------------------------

    public ArrayList<AppFeature> getFeatures() {
        return null;
    }

    public CoordinatorLayout getCoordinatorLayout() {
        return coordinatorLayout;
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public boolean isDebug() {
        return BuildConfig.DEBUG;
    }

//Select app features
//--------------------------------------------------------------------------------------------------
    @Override
    public void onParentSelected(AppFeature feature) {
        selectAppFeature(feature);
    }

    @Override
    public void onChildSelected(AppFeature feature, int childId) {
        selectAppFeature(feature, childId);
    }

    public final void selectAppFeature(AppFeature feature) {
        selectAppFeature(feature, 0);
    }

    public final void selectAppFeature(AppFeature feature, int id) {
        drawer.closeDrawer(GravityCompat.START);

        if(feature == selectedFeature && id == selectedFeatureId)
            return;

        Fragment fragment;
        switch(feature) {
        case Dashboard:
            fragment = DashboardFragment.newInstance();
            break;
        case Read:
            fragment = BibleReaderFragment.newInstance();
            break;
        case Discover:
            fragment = TopicalBibleFragment.newInstance();
            break;
        case Search:
            fragment = TopicalBibleFragment.newInstance();
            break;
        case Topics:
            fragment = TopicsListFragment.newInstance();
            break;
        case MemorizationState:
            fragment = VerseListFragment.newInstance(feature, id);
            break;
        case Tags:
            fragment = VerseListFragment.newInstance(feature, id);
            break;
        case Help:
            fragment = HelpFragment.newInstance();
            break;
        case Settings:
            fragment = SettingsFragment.newInstance();
            break;
        case Prayers:
            fragment = PrayersFragment.newInstance();
            break;
        case Edit:
            fragment = EditVerseFragment.newInstance(id);
            break;
        case DebugDatabase:
            fragment = DebugDatabase.newInstance();
            break;
        case DebugPreferences:
            fragment = DebugPreferences.newInstance();
            break;
        case DebugCache:
            fragment = DebugCache.newInstance();
            break;
        default:
            return;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, TAG)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackStackChanged() {
        if(getSupportFragmentManager().getBackStackEntryCount() == 0) {
            finish();
            return;
        }
        else {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG);
            if(fragment instanceof ActivityBaseFragment) {
                ActivityBaseFragment baseFragment = (ActivityBaseFragment) fragment;
                Pair<AppFeature, Integer> feature = baseFragment.getFeatureForFragment();
                if(feature != null) {
                    selectedFeature = feature.first;
                    selectedFeatureId = feature.second;

                    if(baseFragment.shouldAddToBackStack()) {
                        AppSettings.putSelectedFeature(this, feature.first, feature.second);
                    }
                }
            }

            getSupportActionBar().setTitle(selectedFeature.getTitle());
            getToolbar().setSubtitle(null);
            navView.setSelectedFeature(selectedFeature, selectedFeatureId);

            if(selectedFeature == null || selectedFeature.isTopLevel()) {
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.START);
                toggle.setDrawerIndicatorEnabled(true);
            }
            else {
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START);
                toggle.setDrawerIndicatorEnabled(false);
            }
        }

        if(searchbox.isRevealed()) {
            searchbox.hideInstant(this);
        }
    }

    public void setDrawerEnabled(boolean drawerEnabled) {
        if(drawerEnabled) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.START);
            toggle.setDrawerIndicatorEnabled(true);

            DrawerArrowDrawable arrowDrawable = new DrawerArrowDrawable(this);
            arrowDrawable.setProgress(1.0f);
            arrowDrawable.setColor(Color.WHITE);
            toggle.setHomeAsUpIndicator(arrowDrawable);
            toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackArrowPressed();
                }
            });
        }
        else {
            drawer.closeDrawer(GravityCompat.START);
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START);
            toggle.setDrawerIndicatorEnabled(false);
        }
    }

//Monitor network
//--------------------------------------------------------------------------------------------------
    @Override
    public void onResume() {
        super.onResume();
        connectionReceiver = new NetworkConnectionReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectionReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(connectionReceiver);
    }

    public class NetworkConnectionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("ActivityBase");

            if(fragment != null && fragment instanceof FragmentBase) {
                if(isNetworkAvailable()) {
                    ((FragmentBase) fragment).onNetworkConnected();
                }
                else {
                    ((FragmentBase) fragment).onNetworkDisconnected();
                }
            }

            if(isNetworkAvailable()) {
                if(wasDisconnected) {
                    onNetworkConnected();
                    wasDisconnected = false;
                }
            }
            else {
                onNetworkDisconnected();
                wasDisconnected = true;
            }
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    void onNetworkConnected() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG);
        if(fragment instanceof ActivityBaseFragment) {
            boolean isFinished = ((ActivityBaseFragment) fragment).onNetworkConnected();
            if(isFinished)
                return;
        }

        Snackbar.make(coordinatorLayout, "Network connectivity restored", Snackbar.LENGTH_LONG).show();
    }

    void onNetworkDisconnected() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG);
        if(fragment instanceof ActivityBaseFragment) {
            boolean isFinished = ((ActivityBaseFragment) fragment).onNetworkDisconnected();
            if(isFinished)
                return;
        }

        final Snackbar snackbar = Snackbar.make(coordinatorLayout, "No Network Connection", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Connect", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
        snackbar.show();
    }

    @Override
    public void onBackPressed() {
        if(searchbox.isRevealed()) {
            if(searchbox.isOpen()) {
                searchbox.setIsOpen(false);
            }
            else {
                closeSearch();
            }
            return;
        }

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG);
        if(fragment instanceof ActivityBaseFragment) {
            boolean isFinished = ((ActivityBaseFragment) fragment).onBackButtonPressed();
            if(isFinished)
                return;
        }

        super.onBackPressed();
    }

    void onBackArrowPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG);
        if(fragment instanceof ActivityBaseFragment) {
            boolean isFinished = ((ActivityBaseFragment) fragment).onBackArrowPressed();
            if(isFinished)
                return;
        }

        getSupportFragmentManager().popBackStack();
    }

//Logging
//--------------------------------------------------------------------------------------------------
    public void LogI(String message, Object... params) {
        Log.i(TAG, Util.formatString(message, params));
    }

    public void LogE(String message, Object... params) {
        Log.e(TAG, Util.formatString(message, params));
    }
}
