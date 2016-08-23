package com.caseybrooks.common.features.verses;

import android.content.Context;

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.activity.DrawerFeature;
import com.caseybrooks.common.app.activity.FeatureConfiguration;

public class MemorizationStateFeatureConfiguration extends FeatureConfiguration {

    public MemorizationStateFeatureConfiguration(Context context) {
        super(context);
    }

    @Override
    public DrawerFeature getDrawerFeature() {
        DrawerFeature feature = new DrawerFeature(MemorizationStateFeatureConfiguration.class, "Memorization State", R.drawable.ic_memorization_state);

        return feature;
    }
}
