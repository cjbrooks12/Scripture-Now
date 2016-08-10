package com.caseybrooks.common.app.features.dashboard;

import android.content.Context;

import com.caseybrooks.common.app.FeatureConfiguration;
import com.caseybrooks.common.app.fragment.FragmentConfiguration;

public class DashboardFeatureConfiguration extends FeatureConfiguration {

    private static DashboardFeatureConfiguration instance;

    public static DashboardFeatureConfiguration getInstance(Context context) {
        if(instance == null) {
            instance = new DashboardFeatureConfiguration();
        }

        return instance;
    }

    @Override
    public FragmentConfiguration getFragmentConfiguration(Context context) {
        return DashboardFragment.configuration;
    }
}
