package com.caseybrooks.common.app.features;

import android.content.Context;

import com.caseybrooks.common.app.AppSettings;
import com.caseybrooks.common.app.FeatureConfiguration;
import com.caseybrooks.common.util.Util;

public class LastVisitedFeatureConfiguration extends FeatureConfiguration {

    public static FeatureConfiguration getInstance(Context context) {
        Class<? extends FeatureConfiguration> featureClass = AppSettings.getLastVisitedFeature(context);

        if (featureClass != null) {
            return Util.findFeatureConfiguration(context, featureClass);
        }
        else {
            return null;
        }
    }
}
