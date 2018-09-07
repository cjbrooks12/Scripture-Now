package com.caseybrooks.common.features.prayers;

import android.content.Context;

import com.caseybrooks.common.R;
import com.caseyjbrooks.zion.app.activity.DrawerFeature;
import com.caseyjbrooks.zion.app.activity.FeatureConfiguration;

public class PrayersFeatureConfiguration extends FeatureConfiguration {

    public PrayersFeatureConfiguration(Context context) {
        super(context);
    }

    @Override
    public DrawerFeature getDrawerFeature() {
        DrawerFeature feature = new DrawerFeature(PrayersFeatureConfiguration.class, "Prayers", R.drawable.ic_prayers);

        return feature;
    }
}

