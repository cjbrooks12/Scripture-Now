package com.caseybrooks.common.features.discover.topiclist;

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

public class TopicListFragment extends FragmentBase {

    public static FragmentBase newInstance(Bundle args) {
        TopicListFragment fragment = new TopicListFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView v = new TextView(getContext());
        v.setText("Topic List");

        return v;
    }

    @NonNull
    @Override
    public Class<? extends FragmentConfiguration> getFragmentConfigurationClass() {
        return TopicListFragmentConfiguration.class;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public static class TopicListFragmentConfiguration extends FragmentConfiguration {
        public TopicListFragmentConfiguration(Context context) {
            super(context);
        }

        @NonNull
        @Override
        public Class<? extends FeatureConfiguration> getFeatureConfigurationClass() {
            return TopicListConfiguration.class;
        }

        @NonNull
        @Override
        public Class<? extends FragmentBase> getFragmentClass() {
            return TopicListFragment.class;
        }
    }
}
