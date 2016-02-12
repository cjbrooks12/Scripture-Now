package com.caseybrooks.common.app;

import android.util.Pair;

public interface ActivityBaseFragment {
    boolean shouldAddToBackStack();
    ActivityBase getActivityBase();
    Pair<AppFeature, Integer> getFeatureForFragment();

    boolean onNetworkConnected();
    boolean onNetworkDisconnected();

    boolean onBackButtonPressed();
    boolean onBackArrowPressed();

    boolean usesSearchBox();
}
