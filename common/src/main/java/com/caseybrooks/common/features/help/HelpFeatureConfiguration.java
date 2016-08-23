package com.caseybrooks.common.features.help;

import android.content.Context;

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.activity.DrawerFeature;
import com.caseybrooks.common.app.activity.FeatureConfiguration;
import com.caseybrooks.common.features.verses.MemorizationStateFeatureConfiguration;

public class HelpFeatureConfiguration extends FeatureConfiguration {

    public HelpFeatureConfiguration(Context context) {
        super(context);
    }

    @Override
    public DrawerFeature getDrawerFeature() {
        DrawerFeature feature = new DrawerFeature(HelpFeatureConfiguration.class, "Help", R.drawable.ic_help);

        return feature;
    }
}
