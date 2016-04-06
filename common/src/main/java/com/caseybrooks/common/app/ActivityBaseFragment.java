package com.caseybrooks.common.app;

import android.util.Pair;

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
