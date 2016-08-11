package com.caseybrooks.common.features.debug.cache;

import android.content.Context;

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.activity.DrawerFeature;
import com.caseybrooks.common.app.activity.FeatureConfiguration;
import com.caseybrooks.common.app.fragment.FragmentConfiguration;

public class DebugCacheConfiguration extends FeatureConfiguration {

    public DebugCacheConfiguration(Context context) {
        super(context);
    }

    @Override
    public DrawerFeature getDrawerFeature() {
        return new DrawerFeature(DebugCacheConfiguration.class, "Debug Cache", R.drawable.ic_cache);
    }

    @Override
    public Class<? extends FragmentConfiguration> getFragmentConfigurationClass() {
        return DebugCacheFragment.DebugCacheFragmentConfiguration.class;
    }

}
