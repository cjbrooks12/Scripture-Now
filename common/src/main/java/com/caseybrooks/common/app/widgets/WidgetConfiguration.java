package com.caseybrooks.common.app.widgets;

import android.content.Context;
import android.support.annotation.NonNull;

import com.caseybrooks.common.app.activity.FeatureConfiguration;

public abstract class WidgetConfiguration {
    Context context;

    public WidgetConfiguration(Context context) {
        this.context = context.getApplicationContext();
    }

    public abstract @NonNull Class<? extends FeatureConfiguration> getFeatureConfigurationClass();
    public abstract @NonNull Class<? extends WidgetBase> getWidgetClass();
}
