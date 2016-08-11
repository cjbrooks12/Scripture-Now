package com.caseybrooks.common.app.fragment;

import android.content.Context;
import android.support.annotation.NonNull;

import com.caseybrooks.common.app.activity.FeatureConfiguration;

public abstract class FragmentConfiguration {
    Context context;

    public FragmentConfiguration(Context context) {
        this.context = context.getApplicationContext();
    }

    public abstract @NonNull Class<? extends FeatureConfiguration> getFeatureConfigurationClass();
    public abstract @NonNull Class<? extends FragmentBase> getFragmentClass();
}
