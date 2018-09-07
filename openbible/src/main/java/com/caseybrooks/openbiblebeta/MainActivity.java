package com.caseybrooks.openbiblebeta;

import android.os.Bundle;

import com.caseybrooks.androidbibletools.ABT;
import com.caseybrooks.common.features.debug.DebugFeatureConfiguration;
import com.caseybrooks.common.features.discover.topiclist.TopicListConfiguration;
import com.caseybrooks.common.features.discover.topicsearch.TopicSearchConfiguration;
import com.caseybrooks.common.features.help.HelpFeatureConfiguration;
import com.caseybrooks.common.features.settings.SettingsFeatureConfiguration;
import com.caseyjbrooks.zion.app.activity.ActivityBase;
import com.caseyjbrooks.zion.app.activity.FeatureProvider;

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

        ABT.getInstance(this)
                .getMetadata().putString("ABS_ApiKey", getResources().getString(R.string.bibles_org_key));
        ABT.getInstance(this)
                .getMetadata().putString("JoshuaProject_ApiKey", getResources().getString(R.string.joshua_project_key));
    }

    public boolean isDebug() {
        return BuildConfig.DEBUG;
    }
}
