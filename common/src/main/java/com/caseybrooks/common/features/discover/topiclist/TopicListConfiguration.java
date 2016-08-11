package com.caseybrooks.common.features.discover.topiclist;

import android.content.Context;

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.activity.DrawerFeature;
import com.caseybrooks.common.app.activity.FeatureConfiguration;
import com.caseybrooks.common.app.fragment.FragmentConfiguration;

public class TopicListConfiguration extends FeatureConfiguration {

    public TopicListConfiguration(Context context) {
        super(context);
    }

    @Override
    public DrawerFeature getDrawerFeature() {
        return new DrawerFeature(TopicListConfiguration.class, "Topic List", R.drawable.ic_bible);
    }

    @Override
    public Class<? extends FragmentConfiguration> getFragmentConfigurationClass() {
        return TopicListFragment.TopicListFragmentConfiguration.class;
    }

}
