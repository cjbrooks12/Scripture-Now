package com.caseybrooks.common.features.discover.topicsearch;

import android.content.Context;

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.activity.DrawerFeature;
import com.caseybrooks.common.app.activity.FeatureConfiguration;
import com.caseybrooks.common.app.fragment.FragmentConfiguration;

public class TopicSearchConfiguration extends FeatureConfiguration {

    public TopicSearchConfiguration(Context context) {
        super(context);
    }

    @Override
    public DrawerFeature getDrawerFeature() {
        return new DrawerFeature(TopicSearchConfiguration.class, "Topic Search", R.drawable.ic_bible);
    }

    @Override
    public Class<? extends FragmentConfiguration> getFragmentConfigurationClass() {
        return TopicSearchFragment.TopicSearchFragmentConfiguration.class;
    }

}
