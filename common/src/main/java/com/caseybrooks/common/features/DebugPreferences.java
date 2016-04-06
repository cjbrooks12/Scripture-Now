package com.caseybrooks.common.features;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.caseybrooks.common.app.AppFeature;
import com.caseybrooks.common.app.FragmentBase;
import com.caseybrooks.common.R;

import java.util.ArrayList;
import java.util.List;

public class DebugPreferences extends FragmentBase {
    public static DebugPreferences newInstance() {
        DebugPreferences fragment = new DebugPreferences();
        Bundle data = new Bundle();
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public Pair<AppFeature, Integer> getFeatureForFragment() {
        return new Pair<>(AppFeature.DebugPreferences, 1);
    }

    RecyclerView recyclerview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        recyclerview = new RecyclerView(getContext());

        ArrayList<PreferencesItem> items = new ArrayList<>();
        items.add(new PreferencesItem("KEY 1", "FILE 1", "VALUE 1"));
        items.add(new PreferencesItem("KEY 2", "FILE 1", "VALUE 2"));
        items.add(new PreferencesItem("KEY 3", "FILE 1", "VALUE 3"));
        items.add(new PreferencesItem("KEY 4", "FILE 2", "VALUE 4"));
        items.add(new PreferencesItem("KEY 5", "FILE 2", "VALUE 5"));

        recyclerview.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerview.setAdapter(new PreferencesAdapter(items));

        return recyclerview;
    }

    private class PreferencesItem {
        public String key;
        public String file;
        public String value;

        public PreferencesItem(String key, String file, String value) {
            this.key = key;
            this.file = file;
            this.value = value;
        }
    }

    private class PreferencesViewHolder extends RecyclerView.ViewHolder {
        View root;
        TextView key;
        TextView file;
        TextView value;

        public PreferencesViewHolder(View itemView) {
            super(itemView);

            root = itemView;
            key = (TextView) itemView.findViewById(R.id.key);
            file = (TextView) itemView.findViewById(R.id.file);
            value = (TextView) itemView.findViewById(R.id.value);
        }

        public void onBind(PreferencesItem item) {
            key.setText(item.key);
            file.setText(item.file);
            value.setText(item.value);
        }
    }

    private class PreferencesAdapter extends RecyclerView.Adapter<PreferencesViewHolder> {
        List<PreferencesItem> passages;

        public PreferencesAdapter(List<PreferencesItem> passages) {
            this.passages = passages;
        }

        @Override
        public PreferencesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_debugpreferences, parent, false);
            return new PreferencesViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(PreferencesViewHolder holder, int position) {
            holder.onBind(passages.get(position));
        }

        @Override
        public int getItemCount() {
            return passages.size();
        }
    }
}
