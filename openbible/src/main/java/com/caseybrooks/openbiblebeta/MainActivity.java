package com.caseybrooks.openbiblebeta;

import android.os.Bundle;

import com.caseybrooks.common.app.activity.ActivityBase;
import com.caseybrooks.common.app.activity.FeatureProvider;
import com.caseybrooks.common.app.dashboard.DashboardFeatureConfiguration;
import com.caseybrooks.common.features.debug.DebugFeatureConfiguration;
import com.caseybrooks.common.features.discover.DiscoverFeatureConfiguration;
import com.caseybrooks.common.features.feature1.FeatureOneConfiguration;
import com.caseybrooks.common.features.feature2.FeatureTwoConfiguration;
import com.caseybrooks.common.features.feature3.FeatureThreeConfiguration;

public class MainActivity extends ActivityBase {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initializeFeatures() {
        FeatureProvider provider = FeatureProvider.getInstance(this);

        provider.addFeature(new DashboardFeatureConfiguration(this));
        provider.addFeature(new DiscoverFeatureConfiguration(this));

        provider.addFeature(new FeatureOneConfiguration(this));
        provider.addFeature(new FeatureTwoConfiguration(this));
        provider.addFeature(new FeatureThreeConfiguration(this));

        if(isDebug()) {
            provider.addFeature(new DebugFeatureConfiguration(this));
        }
    }

    @Override
    public boolean isDebug() {
        return BuildConfig.DEBUG;
    }
}
