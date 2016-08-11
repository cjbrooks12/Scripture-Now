package com.caseybrooks.common.features.debug.preferences;

import android.content.Context;

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.activity.DrawerFeature;
import com.caseybrooks.common.app.activity.FeatureConfiguration;
import com.caseybrooks.common.app.fragment.FragmentConfiguration;

public class DebugPreferencesConfiguration extends FeatureConfiguration {

    public DebugPreferencesConfiguration(Context context) {
        super(context);
    }

    @Override
    public DrawerFeature getDrawerFeature() {
        return new DrawerFeature(DebugPreferencesConfiguration.class, "Debug Preferences", R.drawable.ic_preferences);
    }

    @Override
    public Class<? extends FragmentConfiguration> getFragmentConfigurationClass() {
        return DebugPreferencesFragment.DebugPreferencesFragmentConfiguration.class;
    }
}
