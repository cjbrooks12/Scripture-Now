package com.caseybrooks.common.features.help;

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

public class HelpFragment extends FragmentBase {
    public static HelpFragment newInstance() {
        HelpFragment fragment = new HelpFragment();
        Bundle data = new Bundle();
        fragment.setArguments(data);
        return fragment;
    }

    public static FeatureConfiguration getConfiguration() {
        return new FeatureConfiguration() {
            @Override
            public Pair<AppFeature, Integer> getFragmentFeature() {
                return new Pair<>(AppFeature.Help, 0);
            }

            @Override
            public Class<? extends ActivityBaseFragment> getFragmentClass() {
                return HelpFragment.class;
            }

            @Override
            public String getTitle() {
                return "Help";
            }
        };
    }

    public FeatureConfiguration getInstanceConfiguration() {
        return getConfiguration();
    }

// Data Members
//--------------------------------------------------------------------------------------------------


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_help, container, false);

        return view;
    }
}
