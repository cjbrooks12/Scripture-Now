package com.caseybrooks.common.app.activity;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.ProgressBar;

import com.caseybrooks.androidbibletools.ABT;
import com.caseybrooks.common.BuildConfig;
import com.caseybrooks.common.R;
import com.caseybrooks.common.app.AppSettings;
import com.caseybrooks.common.app.ExpandableNavigationView;
import com.caseybrooks.common.app.fragment.FragmentBase;
import com.caseybrooks.common.app.fragment.FragmentConfiguration;
import com.caseybrooks.common.util.Util;
import com.caseybrooks.common.util.clog.Clog;

import java.lang.reflect.Method;
import java.util.Calendar;

public abstract class ActivityBase extends AppCompatActivity implements
        ExpandableNavigationView.OnExpandableNavigationItemSelectedListener,
        FragmentManager.OnBackStackChangedListener {
    public String TAG = getClass().getSimpleName();

    private CoordinatorLayout coordinatorLayout;
    private Toolbar toolbar;
    private ExpandableNavigationView navView;

    AppBarLayout appBarLayout;

    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;

    private FragmentConfiguration selectedFragment;

    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        navView = (ExpandableNavigationView) findViewById(R.id.expandableNavigationView);
        navView.setExpandableNavigationItemSelectedListener(this);

        appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        progressBar = (ProgressBar) findViewById(R.id.progress);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    int statusBarColor = getWindow().getStatusBarColor();
                    int translucentstatusBarColor = Color.argb(
                            (int) (255 - (slideOffset*255)),
                            Color.red(statusBarColor),
                            Color.green(statusBarColor),
                            Color.blue(statusBarColor));

                    getWindow().setStatusBarColor(translucentstatusBarColor);
                }
            }
        };
        drawer.addDrawerListener(toggle);

        toggle.syncState();

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        Calendar time1 = Calendar.getInstance();

        ABT.getInstance(this)
                .getMetadata().putString("ABS_ApiKey", getResources().getString(R.string.bibles_org_key));
        ABT.getInstance(this)
                .getMetadata().putString("JoshuaProject_ApiKey", getResources().getString(R.string.joshua_project_key));
        Calendar time2 = Calendar.getInstance();

        FeatureProvider.getInstance(this).clearFeatures();
        initializeFeatures();
        Calendar time3 = Calendar.getInstance();

        setupNavigationView();
        Calendar time4 = Calendar.getInstance();

        selectDefaultFeature();
        Calendar time5 = Calendar.getInstance();


        long diff_1_2 = time2.getTimeInMillis() - time1.getTimeInMillis();
        long diff_2_3 = time3.getTimeInMillis() - time2.getTimeInMillis();
        long diff_3_4 = time4.getTimeInMillis() - time3.getTimeInMillis();
        long diff_4_5 = time5.getTimeInMillis() - time4.getTimeInMillis();

        Clog.i("setup activitybase", "Setup ABT [{{ $1 }}ms], initialize features [{{ $2 }}ms], setup navigation view [{{ $3 }}ms], select default feature [{{ $4 }}ms] ", diff_1_2, diff_2_3, diff_3_4, diff_4_5);
    }


//Setup activity's theme and features
//--------------------------------------------------------------------------------------------------
    public void setTheme() {
        if(AppSettings.getAppTheme(this).equals("Light"))
            setTheme(R.style.ThemeBase_Light_NoActionBar);
        else
            setTheme(R.style.ThemeBase_Dark_NoActionBar);
    }

    protected abstract void initializeFeatures();

    private void setupNavigationView() {
        navView.setDrawerFeatures(FeatureProvider.getInstance(this).getDrawerFeatures());
    }

    private void selectDefaultFeature() {
        selectFragment(FeatureProvider.getInstance(this).getDefaultFeature());
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
    public void selectFragment(DrawerFeature feature) {
        FeatureConfiguration targetFeature = FeatureProvider.getInstance(this).findFeatureConfiguration(feature.getFeatureClass());
        if(targetFeature == null) {
            Clog.i("Target FeatureConfiguration is null");
            return;
        }

        FragmentConfiguration targetFragment = FeatureProvider.getInstance(this).findFragmentConfiguration(targetFeature.getFragmentConfigurationClass());
        if(targetFragment == null) {
            Clog.i("Target FragmentConfiguration is null");
            return;
        }

        Bundle args = new Bundle();
        args.putLong("id", feature.getId());
        args.putInt("color", feature.getColor());

        selectFragment(targetFragment, args);
    }

    public void selectFragment(FeatureConfiguration feature) {
        selectFragment(feature, new Bundle());
    }

    public void selectFragment(FeatureConfiguration feature, Bundle args) {
        if(feature != null && feature.getFragmentConfigurationClass() != null) {
            selectFragment(FeatureProvider.getInstance(this).findFragmentConfiguration(feature.getFragmentConfigurationClass()), args);
        }
    }

    public final void selectFragment(FragmentConfiguration feature) {
        selectFragment(feature, new Bundle());
    }

    public final void selectFragment(FragmentConfiguration feature, Bundle args) {
        drawer.closeDrawer(GravityCompat.START);
        Clog.i("activitybase", "Instantiating Fragment[{{$1}}]", feature.getFragmentClass());

        if(feature.equals(selectedFragment)) {
            Clog.i("activitybase", "Feature is already selected");
            return;
        }
        else {
            Clog.i("activitybase", "Feature is not already selected");
            selectedFragment = feature;
        }

        Fragment fragment;

        Class<? extends FragmentBase> fragmentClass = feature.getFragmentClass();
        try {
            Method method = fragmentClass.getMethod("newInstance", Bundle.class);
            fragment = (Fragment) method.invoke(null, args);
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if(fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment, TAG)
                    .addToBackStack(null)
                    .commit();
        }

    }

    @Override
    public void onBackStackChanged() {
        setupDecor();
    }

    public void setupDecor() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG);
        if(fragment instanceof FragmentBase) {
            FragmentConfiguration fragmentConfiguration = ((FragmentBase) fragment).getInstanceConfiguration();

            if(fragmentConfiguration == null) {
                fragmentConfiguration = selectedFragment;
            }

            int themeColor = fragmentConfiguration.getDecorColor();

            if(themeColor == 0) {
                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = getTheme();
                theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
                themeColor = typedValue.data;
            }

            final int color = themeColor;

            //setup toolbar
            getToolbar().setBackgroundColor(color);
            getToolbar().setTitle(fragmentConfiguration.getTitle());
            getToolbar().setSubtitle(fragmentConfiguration.getSubtitle());

            //setup statusbar
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int statusBarColor = Util.lighten(color, 0.7f);
                getWindow().setStatusBarColor(statusBarColor);
            }

            //color progressbar
            progressBar.getIndeterminateDrawable().setColorFilter(new PorterDuffColorFilter(themeColor, PorterDuff.Mode.SRC_IN));
            progressBar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(themeColor, PorterDuff.Mode.SRC_IN));
        }
    }









    public void setActivityProgress(int progress) {
        if(progress == 0) {
            progressBar.setVisibility(View.GONE);
        }
        else {
            progressBar.setVisibility(View.VISIBLE);
        }

        if(progress < 0) {
            progressBar.setIndeterminate(true);
        }
        else {
            progressBar.setIndeterminate(false);
        }

        progressBar.setProgress(Math.min(progress, 100));
    }

    public int getActivityProgress() {
        return progressBar.getProgress();
    }
}
