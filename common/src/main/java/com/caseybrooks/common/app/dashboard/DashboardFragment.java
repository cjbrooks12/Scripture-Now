package com.caseybrooks.common.app.dashboard;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.caseybrooks.common.app.activity.FeatureConfiguration;
import com.caseybrooks.common.app.fragment.FragmentBase;
import com.caseybrooks.common.app.fragment.FragmentConfiguration;

public class DashboardFragment extends FragmentBase {
    public static DashboardFragment newInstance(Bundle args) {
        DashboardFragment fragment = new DashboardFragment();
        fragment.setArguments(args);
        return fragment;
    }

//Data members
//--------------------------------------------------------------------------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        TextView view = new TextView(getContext());
        view.setText("Dashboard");

        return view;
    }

    @NonNull
    @Override
    public Class<? extends FragmentConfiguration> getFragmentConfigurationClass() {
        return DashboardFragmentConfiguration.class;
    }

    public static class DashboardFragmentConfiguration extends FragmentConfiguration {
        public DashboardFragmentConfiguration(Context context) {
            super(context);
        }

        @NonNull
        @Override
        public Class<? extends FeatureConfiguration> getFeatureConfigurationClass() {
            return DashboardFeatureConfiguration.class;
        }

        @NonNull
        @Override
        public Class<? extends FragmentBase> getFragmentClass() {
            return DashboardFragment.class;
        }
    }
}
