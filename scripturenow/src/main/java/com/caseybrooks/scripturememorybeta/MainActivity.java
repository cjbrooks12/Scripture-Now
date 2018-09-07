package com.caseybrooks.scripturememorybeta;

import android.os.Bundle;

import com.caseybrooks.androidbibletools.ABT;
import com.caseybrooks.common.features.biblereader.BibleReaderConfiguration;
import com.caseybrooks.common.features.debug.DebugFeatureConfiguration;
import com.caseybrooks.common.features.discover.DiscoverFeatureConfiguration;
import com.caseybrooks.common.features.help.HelpFeatureConfiguration;
import com.caseybrooks.common.features.prayers.PrayersFeatureConfiguration;
import com.caseybrooks.common.features.settings.SettingsFeatureConfiguration;
import com.caseybrooks.common.features.verses.MemorizationStateFeatureConfiguration;
import com.caseybrooks.common.features.verses.TagsFeatureConfiguration;
import com.caseyjbrooks.zion.app.activity.ActivityBase;
import com.caseyjbrooks.zion.app.activity.FeatureProvider;
import com.caseyjbrooks.zion.app.dashboard.DashboardFeatureConfiguration;

public class MainActivity extends ActivityBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initializeFeatures() {
        FeatureProvider provider = FeatureProvider.getInstance(this);

        provider.addFeature(new DashboardFeatureConfiguration(this));
        provider.addFeature(new BibleReaderConfiguration(this));
        provider.addFeature(new DiscoverFeatureConfiguration(this));

        provider.addFeature(new MemorizationStateFeatureConfiguration(this));
        provider.addFeature(new TagsFeatureConfiguration(this));

        provider.addFeature(new PrayersFeatureConfiguration(this));

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
