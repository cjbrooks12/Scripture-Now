package com.caseybrooks.common.features.debug.database;

import android.content.Context;

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.activity.DrawerFeature;
import com.caseybrooks.common.app.activity.FeatureConfiguration;
import com.caseybrooks.common.app.fragment.FragmentConfiguration;

public class DebugDatabaseConfiguration extends FeatureConfiguration {

    public DebugDatabaseConfiguration(Context context) {
        super(context);
    }

    @Override
    public DrawerFeature getDrawerFeature() {
        return new DrawerFeature(DebugDatabaseConfiguration.class, "Debug Database", R.drawable.ic_database);
    }

    @Override
    public Class<? extends FragmentConfiguration> getFragmentConfigurationClass() {
        return DebugDatabaseFragment.DebugDatabaseFragmentConfiguration.class;
    }
}
