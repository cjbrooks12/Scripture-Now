package com.caseybrooks.common.features.discover.topiclist;

import android.content.Context;

import com.caseybrooks.common.R;
import com.caseyjbrooks.zion.app.activity.DrawerFeature;
import com.caseyjbrooks.zion.app.activity.FeatureConfiguration;
import com.caseyjbrooks.zion.app.fragment.FragmentConfiguration;

public class TopicListConfiguration extends FeatureConfiguration {

    public TopicListConfiguration(Context context) {
        super(context);
    }

    @Override
    public DrawerFeature getDrawerFeature() {
        return new DrawerFeature(TopicListConfiguration.class, "Topic List", R.drawable.ic_topic_list);
    }

    @Override
    public Class<? extends FragmentConfiguration> getFragmentConfigurationClass() {
        return TopicListFragment.TopicListFragmentConfiguration.class;
    }
}
