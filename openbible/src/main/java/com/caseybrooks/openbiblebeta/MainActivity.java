package com.caseybrooks.openbiblebeta;

import android.os.Bundle;

import com.caseybrooks.common.app.activity.ActivityBase;
import com.caseybrooks.common.app.activity.FeatureProvider;
import com.caseybrooks.common.features.debug.DebugFeatureConfiguration;
import com.caseybrooks.common.features.discover.topiclist.TopicListConfiguration;
import com.caseybrooks.common.features.discover.topicsearch.TopicSearchConfiguration;
import com.caseybrooks.common.features.help.HelpFeatureConfiguration;
import com.caseybrooks.common.features.settings.SettingsFeatureConfiguration;

public class MainActivity extends ActivityBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initializeFeatures() {
        FeatureProvider provider = FeatureProvider.getInstance(this);

        provider.addFeature(new TopicSearchConfiguration(this));
        provider.addFeature(new TopicListConfiguration(this));

        provider.addFeature(new HelpFeatureConfiguration(this));
        provider.addFeature(new SettingsFeatureConfiguration(this));

        if(isDebug()) {
            provider.addFeature(new DebugFeatureConfiguration(this));
        }
    }

    @Override
    public boolean isDebug() {
        return BuildConfig.DEBUG;
    }
}
