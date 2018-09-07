package com.caseybrooks.common.features.debug.cache;

import android.content.Context;

import com.caseybrooks.common.R;
import com.caseyjbrooks.zion.app.activity.DrawerFeature;
import com.caseyjbrooks.zion.app.activity.FeatureConfiguration;
import com.caseyjbrooks.zion.app.fragment.FragmentConfiguration;

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
