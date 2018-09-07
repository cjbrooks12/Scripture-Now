package com.caseybrooks.common.features.debug;

import android.content.Context;

import com.caseybrooks.common.R;
import com.caseybrooks.common.features.debug.cache.DebugCacheConfiguration;
import com.caseybrooks.common.features.debug.database.DebugDatabaseConfiguration;
import com.caseybrooks.common.features.debug.preferences.DebugPreferencesConfiguration;
import com.caseyjbrooks.zion.app.activity.DrawerFeature;
import com.caseyjbrooks.zion.app.activity.FeatureConfiguration;
import com.caseyjbrooks.zion.app.activity.FeatureProvider;

import java.util.ArrayList;

public class DebugFeatureConfiguration extends FeatureConfiguration {

    public DebugFeatureConfiguration(Context context) {
        super(context);
    }

    @Override
    public DrawerFeature getDrawerFeature() {
        DrawerFeature feature = new DrawerFeature(DebugFeatureConfiguration.class, "Debug", R.drawable.ic_debug);

        ArrayList<DrawerFeature> children = new ArrayList<>();
        children.add(FeatureProvider.getInstance(getContext()).findFeatureConfiguration(DebugCacheConfiguration.class).getDrawerFeature());
        children.add(FeatureProvider.getInstance(getContext()).findFeatureConfiguration(DebugDatabaseConfiguration.class).getDrawerFeature());
        children.add(FeatureProvider.getInstance(getContext()).findFeatureConfiguration(DebugPreferencesConfiguration.class).getDrawerFeature());

        feature.setChildren(children);

        return feature;
    }
}
