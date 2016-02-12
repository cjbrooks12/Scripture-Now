package com.caseybrooks.common.fragments;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.caseybrooks.common.app.AppFeature;
import com.caseybrooks.common.app.FragmentBase;
import com.caseybrooks.common.R;

public class EditVerseFragment extends FragmentBase {
    public static EditVerseFragment newInstance(int id) {
        EditVerseFragment fragment = new EditVerseFragment();
        Bundle data = new Bundle();
        data.putInt("KEY_VERSE_ID", id);
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editverse, container, false);

        return view;
    }

    @Override
    public Pair<AppFeature, Integer> getFeatureForFragment() {
        return new Pair<>(AppFeature.Edit, 0);
    }
}
