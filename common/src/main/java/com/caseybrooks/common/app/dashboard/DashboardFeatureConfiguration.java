package com.caseybrooks.common.app.dashboard;

import android.content.Context;

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.activity.DrawerFeature;
import com.caseybrooks.common.app.activity.FeatureConfiguration;
import com.caseybrooks.common.app.fragment.FragmentConfiguration;

public class DashboardFeatureConfiguration extends FeatureConfiguration {
    public DashboardFeatureConfiguration(Context context) {
        super(context);
    }

    @Override
    public DrawerFeature getDrawerFeature() {
        return new DrawerFeature(DashboardFeatureConfiguration.class, "Dashboard", R.drawable.ic_home);
    }

    @Override
    public Class<? extends FragmentConfiguration> getFragmentConfigurationClass() {
        return DashboardFragment.DashboardFragmentConfiguration.class;
    }
}
