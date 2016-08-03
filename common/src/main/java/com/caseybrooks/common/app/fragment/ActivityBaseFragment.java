package com.caseybrooks.common.app.fragment;

import android.view.MenuItem;

import com.caseybrooks.common.app.FeatureConfiguration;
import com.caseybrooks.common.app.activity.ActivityBase;

// TODO: Make this a configuration object and this interface just gives back that object. This will make adding changes to the activity features easily propagate throughout the app
public interface ActivityBaseFragment {
    ActivityBase getActivityBase();

    boolean onNetworkConnected();
    boolean onNetworkDisconnected();
    boolean onBackButtonPressed();
    boolean onBackArrowPressed();
    boolean onFABPressed();

    boolean onSearchSubmitted(String query);
    void onQueryChanged(String query);
    boolean onSearchMenuItemSelected(MenuItem selectedItem);

    FeatureConfiguration getInstanceConfiguration();
}
