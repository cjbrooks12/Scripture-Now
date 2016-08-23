package com.caseybrooks.common.features.settings;

import android.content.Context;

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.activity.DrawerFeature;
import com.caseybrooks.common.app.activity.FeatureConfiguration;

public class SettingsFeatureConfiguration extends FeatureConfiguration {

    public SettingsFeatureConfiguration(Context context) {
        super(context);
    }

    @Override
    public DrawerFeature getDrawerFeature() {
        DrawerFeature feature = new DrawerFeature(SettingsFeatureConfiguration.class, "Settings", R.drawable.ic_settings);

        return feature;
    }
}

