package com.caseybrooks.common.features.discover;

import android.content.Context;

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.activity.DrawerFeature;
import com.caseybrooks.common.app.activity.FeatureConfiguration;
import com.caseybrooks.common.app.activity.FeatureProvider;
import com.caseybrooks.common.features.discover.importverses.ImportVersesConfiguration;
import com.caseybrooks.common.features.discover.topiclist.TopicListConfiguration;
import com.caseybrooks.common.features.discover.topicsearch.TopicSearchConfiguration;
import com.caseybrooks.common.features.feature2.FeatureTwoConfiguration;

import java.util.ArrayList;

public class DiscoverFeatureConfiguration extends FeatureConfiguration {

    public DiscoverFeatureConfiguration(Context context) {
        super(context);
    }

    @Override
    public DrawerFeature getDrawerFeature() {
        DrawerFeature feature = new DrawerFeature(FeatureTwoConfiguration.class, "Discover", R.drawable.ic_flashcards);

        ArrayList<DrawerFeature> children = new ArrayList<>();
        children.add(FeatureProvider.getInstance(getContext()).findFeatureConfiguration(TopicSearchConfiguration.class).getDrawerFeature());
        children.add(FeatureProvider.getInstance(getContext()).findFeatureConfiguration(TopicListConfiguration.class).getDrawerFeature());
        children.add(FeatureProvider.getInstance(getContext()).findFeatureConfiguration(ImportVersesConfiguration.class).getDrawerFeature());

        feature.setChildren(children);

        return feature;
    }
}
