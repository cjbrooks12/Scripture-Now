package com.caseybrooks.common.app;

import android.util.Pair;
// TODO: Make this a configuration object and this interface just gives back that object. This will make adding changes to the activity features easily propagate throughout the app
public interface ActivityBaseFragment<T> {
    boolean shouldAddToBackStack();
    ActivityBase getActivityBase();
    Pair<AppFeature, Integer> getFeatureForFragment();
    int getDecorColor();

    boolean onNetworkConnected();
    boolean onNetworkDisconnected();

    boolean onBackButtonPressed();
    boolean onBackArrowPressed();

    boolean onFABPressed();
    int getFABIcon();

    boolean usesSearchBox();
}
