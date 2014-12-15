package com.caseybrooks.scripturememory.fragments;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.misc.NavigationCallbacks;
import com.nirhart.parallaxscroll.views.ParallaxExpandableListView;

public class SpotifyTestFragment extends Fragment {
    NavigationCallbacks mCallbacks;
    Context context;

    public static Fragment newInstance() {
        Fragment fragment = new SpotifyTestFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_spotifytest, container, false);

        this.context = getActivity();

        ParallaxExpandableListView listView = (ParallaxExpandableListView) view.findViewById(R.id.list_view);

        TextView v = new TextView(context);
        v.setText("PARALLAXED");
        v.setGravity(Gravity.CENTER);
        v.setTextSize(40);
        v.setHeight(200);
        v.setBackgroundColor(Color.parseColor("#66FF0000"));

        listView.addParallaxedHeaderView(v);
        CustomExpandableListAdapter adapter = new CustomExpandableListAdapter(LayoutInflater.from(context));
        listView.setAdapter(adapter);

        return view;
    }

    public class CustomExpandableListAdapter extends BaseExpandableListAdapter {

        private LayoutInflater inflater;

        public CustomExpandableListAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
        }

        @Override
        public String getChild(int groupPosition, int childPosition) {
            return "Group " + groupPosition + ", child " + childPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            TextView textView = (TextView) convertView;
            if (textView == null)
                textView = new TextView(context);
            textView.setText(getChild(groupPosition, childPosition));
            return textView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return groupPosition*2+1;
        }

        @Override
        public String getGroup(int groupPosition) {
            return "Group " + groupPosition;
        }

        @Override
        public int getGroupCount() {
            return 20;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            TextView textView = (TextView) convertView;
            if (textView == null)
                textView = new TextView(context);
            textView.setText(getGroup(groupPosition));
            return textView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }
}
