package com.caseybrooks.common.features.feature3;

import android.content.Context;

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.activity.DrawerFeature;
import com.caseybrooks.common.app.activity.FeatureConfiguration;
import com.caseybrooks.common.app.fragment.FragmentConfiguration;

public class FeatureThreeConfiguration extends FeatureConfiguration {

    public FeatureThreeConfiguration(Context context) {
        super(context);
    }

    @Override
    public DrawerFeature getDrawerFeature() {
        return new DrawerFeature(FeatureThreeConfiguration.class, "Feature Three", R.drawable.ic_chevron_down);
    }

    @Override
    public Class<? extends FragmentConfiguration> getFragmentConfigurationClass() {
        return FeatureThreeFragment.FeatureThreeFragmentConfiguration.class;
    }
}
