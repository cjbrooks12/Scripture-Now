package com.caseybrooks.common.features.verses;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.caseybrooks.common.app.AppFeature;
import com.caseybrooks.common.app.FragmentBase;
import com.caseybrooks.common.R;
import com.caseybrooks.common.util.Util;

public class VerseListFragment extends FragmentBase {
    public static VerseListFragment newInstance(AppFeature type, int id) {
        VerseListFragment fragment = new VerseListFragment();
        Bundle data = new Bundle();
        data.putInt("KEY_LIST_TYPE", type.getId());
        data.putInt("KEY_LIST_ID", id);
        fragment.setArguments(data);
        return fragment;
    }

    AppFeature listFeature;
    int listId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_verselist, container, false);

        TextView tv = (TextView) view.findViewById(R.id.verse_list_tv);

        listFeature = AppFeature.getFeatureForId(getArguments().getInt("KEY_LIST_TYPE"));
        listId = getArguments().getInt("KEY_LIST_ID");

        tv.setText(Util.formatString("Type='{0}' id='{1}'", listFeature.getTitle(), listId));

        return view;
    }

    @Override
    public Pair<AppFeature, Integer> getFeatureForFragment() {
        return new Pair<>(listFeature, listId);
    }
}
