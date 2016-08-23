package com.caseybrooks.common.features.verses;

import android.content.Context;

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.activity.DrawerFeature;
import com.caseybrooks.common.app.activity.FeatureConfiguration;

public class TagsFeatureConfiguration extends FeatureConfiguration {

    public TagsFeatureConfiguration(Context context) {
        super(context);
    }

    @Override
    public DrawerFeature getDrawerFeature() {
        DrawerFeature feature = new DrawerFeature(TagsFeatureConfiguration.class, "Tags", R.drawable.ic_tag);

        return feature;
    }
}
