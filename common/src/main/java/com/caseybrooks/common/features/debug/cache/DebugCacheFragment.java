package com.caseybrooks.common.features.debug.cache;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.caseyjbrooks.zion.app.activity.FeatureConfiguration;
import com.caseyjbrooks.zion.app.fragment.FragmentBase;
import com.caseyjbrooks.zion.app.fragment.FragmentConfiguration;

public class DebugCacheFragment extends FragmentBase {

    public static FragmentBase newInstance(Bundle args) {
        DebugCacheFragment fragment = new DebugCacheFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView v = new TextView(getContext());
        v.setText("Debug Cache");

        return v;
    }

    @NonNull
    @Override
    public Class<? extends FragmentConfiguration> getFragmentConfigurationClass() {
        return DebugCacheFragmentConfiguration.class;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public static class DebugCacheFragmentConfiguration extends FragmentConfiguration {
        public DebugCacheFragmentConfiguration(Context context) {
            super(context);
        }

        @NonNull
        @Override
        public Class<? extends FeatureConfiguration> getFeatureConfigurationClass() {
            return DebugCacheConfiguration.class;
        }

        @NonNull
        @Override
        public Class<? extends FragmentBase> getFragmentClass() {
            return DebugCacheFragment.class;
        }
    }
}
