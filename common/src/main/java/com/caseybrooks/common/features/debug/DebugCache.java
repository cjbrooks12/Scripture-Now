package com.caseybrooks.common.features.debug;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.caseybrooks.common.app.AppFeature;
import com.caseybrooks.common.app.FragmentBase;
import com.caseybrooks.common.R;

public class DebugCache extends FragmentBase {
    public static DebugCache newInstance() {
        DebugCache fragment = new DebugCache();
        Bundle data = new Bundle();
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_debugcache, container, false);

        return view;
    }

    @Override
    public Pair<AppFeature, Integer> getFeatureForFragment() {
        return new Pair<>(AppFeature.DebugCache, 2);
    }
}
