package com.caseybrooks.common.features.debug;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.fragment.ActivityBaseFragment;
import com.caseybrooks.common.app.fragment.AppFeature;
import com.caseybrooks.common.app.fragment.FragmentBase;
import com.caseybrooks.common.app.FeatureConfiguration;

public class DebugCacheFragment extends FragmentBase {
    public static DebugCacheFragment newInstance(Bundle args) {
        DebugCacheFragment fragment = new DebugCacheFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static FeatureConfiguration getConfiguration() {
        return new FeatureConfiguration() {
            @Override
            public Pair<AppFeature, Integer> getFragmentFeature() {
                return new Pair<>(AppFeature.DebugCache, 0);
            }

            @Override
            public Class<? extends ActivityBaseFragment> getFragmentClass() {
                return DebugCacheFragment.class;
            }

            @Override
            public String getNavigationTitle() {
                return getTitle();
            }

            @Override
            public int getNavigationIcon() {
                return R.drawable.ic_file;
            }

            @Override
            public String getTitle() {
                return "Debug Cache";
            }
        };
    }

    public FeatureConfiguration getInstanceConfiguration() {
        return getConfiguration();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_debugcache, container, false);

        return view;
    }
}
