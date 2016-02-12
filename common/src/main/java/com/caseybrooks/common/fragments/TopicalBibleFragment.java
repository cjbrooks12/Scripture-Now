package com.caseybrooks.common.fragments;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.caseybrooks.androidbibletools.data.OnResponseListener;
import com.caseybrooks.androidbibletools.providers.openbible.OpenBiblePassage;
import com.caseybrooks.androidbibletools.providers.openbible.TopicalSearch;
import com.caseybrooks.common.app.ActivityBase;
import com.caseybrooks.common.app.AppFeature;
import com.caseybrooks.common.app.FragmentBase;
import com.caseybrooks.common.R;

import java.util.List;

public class TopicalBibleFragment extends FragmentBase implements OnResponseListener {
    RecyclerView recyclerView;
    TopicalSearch topicalSearch;

    public static TopicalBibleFragment newInstance() {
        TopicalBibleFragment fragment = new TopicalBibleFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_topicalbible, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        return view;
    }

    @Override
    public void responseFinished() {
        ((ActivityBase) getActivity()).setActivityProgress(0);
        Snackbar.make(((ActivityBase) getActivity()).getCoordinatorLayout(), topicalSearch.getPassages().size() + " passages for '" + topicalSearch.getSearchTerm() + "'", Snackbar.LENGTH_LONG).show();

        recyclerView.setAdapter(new OpenBibleAdapter(topicalSearch.getPassages()));
        ((ActivityBase) getContext()).setActivityProgress(0);
    }

//Searchbox interface
//--------------------------------------------------------------------------------------------------

    @Override
    public String getSearchHint() {
        return "Search OpenBible.info";
    }

    @Override
    public int getSearchMenuResourceId() {
        return 0;
    }

    @Override
    public boolean usesSearchBox() {
        return true;
    }

    @Override
    public boolean onSearch(String query) {
        ((ActivityBase) getContext()).setActivityProgress(-1);

        topicalSearch = new TopicalSearch();
        topicalSearch.setSearchTerm(query);
        topicalSearch.download(this);

        return true;
    }

//Recyclerview stuff
//--------------------------------------------------------------------------------------------------
    private class OpenBibleViewholder extends RecyclerView.ViewHolder {
        View root;
        TextView reference;
        TextView bible;
        TextView upvotes;
        TextView text;

        public OpenBibleViewholder(View itemView) {
            super(itemView);

            root = itemView;
            reference = (TextView) itemView.findViewById(R.id.reference);
            bible = (TextView) itemView.findViewById(R.id.bible);
            upvotes = (TextView) itemView.findViewById(R.id.upvotes);
            text = (TextView) itemView.findViewById(R.id.text);
        }

        public void onBind(OpenBiblePassage passage) {
            reference.setText(passage.getReference().toString());
            bible.setText(passage.getReference().getBible().getAbbreviation());
            upvotes.setText((passage.getMetadata().getInt("UPVOTES", 0) + " helpful votes"));
            text.setText(passage.getText());
        }
    }

    private class OpenBibleAdapter extends RecyclerView.Adapter<OpenBibleViewholder> {
        List<OpenBiblePassage> passages;

        public OpenBibleAdapter(List<OpenBiblePassage> passages) {
            this.passages = passages;
        }

        @Override
        public OpenBibleViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_topicalbible, parent, false);
            return new OpenBibleViewholder(itemView);
        }

        @Override
        public void onBindViewHolder(OpenBibleViewholder holder, int position) {
            holder.onBind(passages.get(position));
        }

        @Override
        public int getItemCount() {
            return passages.size();
        }
    }

    @Override
    public Pair<AppFeature, Integer> getFeatureForFragment() {
        return new Pair<>(AppFeature.Discover, 0);
    }
}
