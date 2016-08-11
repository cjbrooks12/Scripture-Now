package com.caseybrooks.common.app.dashboard;

import android.support.annotation.NonNull;

import com.caseybrooks.common.app.activity.FeatureConfiguration;

public abstract class DashboardCardConfiguration {
    public abstract @NonNull Class<? extends FeatureConfiguration> getFeatureConfigurationClass();
    public abstract @NonNull Class<? extends DashboardCardBase> getDashboardCardClass();
}
