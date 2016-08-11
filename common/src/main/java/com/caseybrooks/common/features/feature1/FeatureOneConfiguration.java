package com.caseybrooks.common.features.feature1;

import android.content.Context;

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.activity.DrawerFeature;
import com.caseybrooks.common.app.activity.FeatureConfiguration;
import com.caseybrooks.common.app.fragment.FragmentConfiguration;

public class FeatureOneConfiguration extends FeatureConfiguration {

    public FeatureOneConfiguration(Context context) {
        super(context);
    }

    @Override
    public DrawerFeature getDrawerFeature() {
        return new DrawerFeature(FeatureOneConfiguration.class, "Feature One", R.drawable.ic_chevron_up);
    }

    @Override
    public Class<? extends FragmentConfiguration> getFragmentConfigurationClass() {
        return FeatureOneFragment.FeatureOneFragmentConfiguration.class;
    }
}
