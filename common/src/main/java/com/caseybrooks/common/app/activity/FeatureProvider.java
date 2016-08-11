package com.caseybrooks.common.app.activity;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.caseybrooks.common.app.dashboard.DashboardCardConfiguration;
import com.caseybrooks.common.app.fragment.FragmentConfiguration;
import com.caseybrooks.common.app.notifications.NotificationConfiguration;
import com.caseybrooks.common.app.widgets.WidgetConfiguration;
import com.caseybrooks.common.util.clog.Clog;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class FeatureProvider {
    private static FeatureProvider instance;

    public static FeatureProvider getInstance(@NonNull Context context) {
        if(instance == null) {
            instance = new FeatureProvider(context);
        }

        return instance;
    }

    private FeatureProvider(@NonNull Context context) {
        applicationContext = context.getApplicationContext();

        appFeatures = new ArrayList<>();
        fragments = new ArrayList<>();
        drawerFeatures = new ArrayList<>();
        dashboardCards = new ArrayList<>();
    }



// Data Members
//--------------------------------------------------------------------------------------------------
    private final Context applicationContext;

    private ArrayList<FeatureConfiguration> appFeatures;
    private ArrayList<FragmentConfiguration> fragments;
    private ArrayList<DashboardCardConfiguration> dashboardCards;
    private ArrayList<NotificationConfiguration> notifications;
    private ArrayList<WidgetConfiguration> widgets;

    private ArrayList<DrawerFeature> drawerFeatures;

    private FeatureConfiguration defaultFeature;

// Set up application features through the top-level FeatureConfiguration objects. When requested,
//    they will serve up the class of the appropriate configuration object, which we will create if
//    necessary, then cache and return.

    public void addFeature(@NonNull FeatureConfiguration newFeature) {
        for(int i = 0; i < appFeatures.size(); i++) {
            if(appFeatures.get(i).equals(newFeature)) {
                //we already have this feature in our manifest.
                return;
            }
        }

        //we didn't find this feature already, add it now, along with an optional drawer feature
        appFeatures.add(newFeature);
        if(newFeature.getDrawerFeature() != null) {
            drawerFeatures.add(newFeature.getDrawerFeature());

            if(defaultFeature == null) {
                defaultFeature = newFeature;
            }
        }
    }

    public ArrayList<FeatureConfiguration> getAppFeatures() {
        return appFeatures;
    }

    public ArrayList<FragmentConfiguration> getFragments() {
        return fragments;
    }

    public ArrayList<DashboardCardConfiguration> getDashboardCards() {
        return dashboardCards;
    }

    public ArrayList<NotificationConfiguration> getNotifications() {
        return notifications;
    }

    public ArrayList<WidgetConfiguration> getWidgets() {
        return widgets;
    }

    public ArrayList<DrawerFeature> getDrawerFeatures() {
        return drawerFeatures;
    }

    public FeatureConfiguration getDefaultFeature() {
        return defaultFeature;
    }

    public void setDefaultFeature(FeatureConfiguration defaultFeature) {
        this.defaultFeature = defaultFeature;
    }

    // Use reflection to find the concrete object given a class
//--------------------------------------------------------------------------------------------------
    public @Nullable FeatureConfiguration findFeatureConfiguration(@NonNull Class<? extends FeatureConfiguration> c) {
        FeatureConfiguration featureConfiguration = null;

        // check cache for already-created feature
        for(FeatureConfiguration feature : appFeatures) {
            if(feature.getClass().equals(c)) {
                featureConfiguration = feature;
                break;
            }
        }

        try {
            Constructor getInstanceMethod = c.getConstructor(Context.class);
            FeatureConfiguration appFeature = (FeatureConfiguration) getInstanceMethod.newInstance(applicationContext);

            appFeatures.add(appFeature);
            featureConfiguration = appFeature;
        }
        catch(NoSuchMethodException e) {
            Clog.e("findFragmentConfiguration", "feature does not contain a constructor with Context as its only parameter");
            e.printStackTrace();
        }
        catch(InvocationTargetException e) {
            Clog.e("findFragmentConfiguration", "An exception was thrown while calling this class's static 'getInstance(Context context)' method");
            e.printStackTrace();
        }
        catch(IllegalAccessException e) {
            Clog.e("findFragmentConfiguration", "A static method called 'getInstance(Context context)' is not accessible");
            e.printStackTrace();
        }
        catch(InstantiationException e) {
            Clog.e("findFragmentConfiguration", "Something went wrong instantiating fragment configuration");
            e.printStackTrace();
        }

        return featureConfiguration;
    }

    public @Nullable FragmentConfiguration findFragmentConfiguration(@NonNull Class<? extends FragmentConfiguration> c) {
        FragmentConfiguration fragmentConfiguration = null;

        // check cache for already-created feature
        for(FragmentConfiguration fragment : fragments) {
            if(fragment.getClass().equals(c)) {
                fragmentConfiguration = fragment;
                break;
            }
        }

        try {
            Constructor getInstanceMethod = c.getConstructor(Context.class);

            FragmentConfiguration fragment = (FragmentConfiguration) getInstanceMethod.newInstance(applicationContext);

            fragments.add(fragment);
            fragmentConfiguration = fragment;
        }
        catch(NoSuchMethodException e) {
            Clog.e("findFragmentConfiguration", "feature does not contain a constructor with Context as its only parameter");
            e.printStackTrace();
        }
        catch(InvocationTargetException e) {
            Clog.e("findFragmentConfiguration", "An exception was thrown while calling this class's static 'getInstance(Context context)' method");
            e.printStackTrace();
        }
        catch(IllegalAccessException e) {
            Clog.e("findFragmentConfiguration", "A static method called 'getInstance(Context context)' is not accessible");
            e.printStackTrace();
        }
        catch(InstantiationException e) {
            Clog.e("findFragmentConfiguration", "Something went wrong instantiating fragment configuration");
            e.printStackTrace();
        }

        return fragmentConfiguration;
    }

    public @Nullable
    DashboardCardConfiguration findDashboardCardConfiguration(@NonNull Class<? extends DashboardCardConfiguration> c) {
        DashboardCardConfiguration dashboardCardConfiguration = null;

        // check cache for already-created feature
        for(DashboardCardConfiguration feature : dashboardCards) {
            if(feature.getClass().equals(c)) {
                dashboardCardConfiguration = feature;
                break;
            }
        }

        try {
            Method getInstanceMethod = c.getDeclaredMethod("getInstance", Context.class);

            if(Modifier.isStatic(getInstanceMethod.getModifiers())) {
                if(getInstanceMethod.getReturnType().equals(DashboardCardConfiguration.class)) {
                    DashboardCardConfiguration feature = (DashboardCardConfiguration) getInstanceMethod.invoke(null, applicationContext);

                    dashboardCards.add(feature);
                    dashboardCardConfiguration = feature;
                }
                else {
                    Clog.e("find feature", "Static method 'getInstance(Context context)' must return DashboardFeatureConfiguration");
                }
            }
            else {
                Clog.e("find feature", "Method 'getInstance(Context context)' must be declared as static");
            }
        }
        catch(NoSuchMethodException e) {
            Clog.e("find feature", "feature does not contain a static method called 'getInstance(Context context)'");
            e.printStackTrace();
        }
        catch(InvocationTargetException e) {
            Clog.e("find feature", "An exception was thrown while calling this class's static 'getInstance(Context context)' method");
            e.printStackTrace();
        }
        catch(IllegalAccessException e) {
            Clog.e("find feature", "A static method called 'getInstance(Context context)' is not accessible");
            e.printStackTrace();
        }

        return dashboardCardConfiguration;
    }
}
