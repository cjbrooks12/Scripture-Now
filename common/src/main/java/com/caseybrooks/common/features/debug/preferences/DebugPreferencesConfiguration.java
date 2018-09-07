package com.caseybrooks.common.features.debug.preferences;

import android.content.Context;

import com.caseybrooks.common.R;
import com.caseyjbrooks.zion.app.activity.DrawerFeature;
import com.caseyjbrooks.zion.app.activity.FeatureConfiguration;
import com.caseyjbrooks.zion.app.fragment.FragmentConfiguration;

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
