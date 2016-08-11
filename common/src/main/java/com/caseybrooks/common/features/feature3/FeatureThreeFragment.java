package com.caseybrooks.common.features.feature3;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.activity.FeatureConfiguration;
import com.caseybrooks.common.app.fragment.FragmentBase;
import com.caseybrooks.common.app.fragment.FragmentConfiguration;

public class FeatureThreeFragment extends FragmentBase {

    public static FragmentBase newInstance(Bundle args) {
        FeatureThreeFragment fragment = new FeatureThreeFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_three, container, false);

        return v;
    }

    @NonNull
    @Override
    public Class<? extends FragmentConfiguration> getFragmentConfigurationClass() {
        return FeatureThreeFragmentConfiguration.class;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static class FeatureThreeFragmentConfiguration extends FragmentConfiguration {
        public FeatureThreeFragmentConfiguration(Context context) {
            super(context);
        }

        @NonNull
        @Override
        public Class<? extends FeatureConfiguration> getFeatureConfigurationClass() {
            return FeatureThreeConfiguration.class;
        }

        @NonNull
        @Override
        public Class<? extends FragmentBase> getFragmentClass() {
            return FeatureThreeFragment.class;
        }
    }
}
