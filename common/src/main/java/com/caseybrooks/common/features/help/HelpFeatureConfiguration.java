package com.caseybrooks.common.features.help;

import android.content.Context;

import com.caseybrooks.common.R;
import com.caseyjbrooks.zion.app.activity.DrawerFeature;
import com.caseyjbrooks.zion.app.activity.FeatureConfiguration;

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
