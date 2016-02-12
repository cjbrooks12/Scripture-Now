package com.caseybrooks.common.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.caseybrooks.common.app.AppFeature;
import com.caseybrooks.common.app.FragmentBase;
import com.caseybrooks.common.R;

public class ImportVersesFragment extends FragmentBase {
    public static ImportVersesFragment newInstance(AppFeature type, int id) {
        ImportVersesFragment fragment = new ImportVersesFragment();
        Bundle data = new Bundle();
        data.putInt("KEY_LIST_TYPE", type.getId());
        data.putInt("KEY_LIST_ID", id);
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_importverses, container, false);

        return view;
    }
}
