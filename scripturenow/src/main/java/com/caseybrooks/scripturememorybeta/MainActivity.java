package com.caseybrooks.scripturememorybeta;

import android.os.Bundle;

import com.caseybrooks.common.app.activity.ActivityBase;
import com.caseybrooks.common.app.activity.FeatureProvider;
import com.caseybrooks.common.app.dashboard.DashboardFeatureConfiguration;
import com.caseybrooks.common.features.biblereader.BibleReaderConfiguration;
import com.caseybrooks.common.features.debug.DebugFeatureConfiguration;
import com.caseybrooks.common.features.discover.DiscoverFeatureConfiguration;
import com.caseybrooks.common.features.help.HelpFeatureConfiguration;
import com.caseybrooks.common.features.prayers.PrayersFeatureConfiguration;
import com.caseybrooks.common.features.settings.SettingsFeatureConfiguration;
import com.caseybrooks.common.features.verses.MemorizationStateFeatureConfiguration;
import com.caseybrooks.common.features.verses.TagsFeatureConfiguration;

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
    }

    @Override
    public boolean isDebug() {
        return BuildConfig.DEBUG;
    }
}
