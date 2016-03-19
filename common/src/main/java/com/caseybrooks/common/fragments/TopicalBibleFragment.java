package com.caseybrooks.common.fragments;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.caseybrooks.androidbibletools.data.OnResponseListener;
import com.caseybrooks.androidbibletools.providers.openbible.OpenBiblePassage;
import com.caseybrooks.androidbibletools.providers.openbible.TopicalSearch;
import com.caseybrooks.common.R;
import com.caseybrooks.common.app.ActivityBase;
import com.caseybrooks.common.app.AppFeature;
import com.caseybrooks.common.app.FragmentBase;
import com.caseybrooks.common.app.Util;
import com.caseybrooks.common.widget.CardView;
import com.caseybrooks.common.util.ItemTouchHelperAdapter;
import com.caseybrooks.common.util.ItemTouchHelperViewHolder;
import com.caseybrooks.common.util.SimpleItemTouchHelperCallback;
import com.caseybrooks.common.widget.TintableImageView;

import java.util.Collections;
import java.util.List;

public class TopicalBibleFragment extends FragmentBase implements OnResponseListener {
    RecyclerView recyclerView;
    TopicalSearch topicalSearch;

    ItemTouchHelper mItemTouchHelper;

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
    public void responseFinished(boolean success) {
        ((ActivityBase) getActivity()).setActivityProgress(0);
        Snackbar.make(((ActivityBase) getActivity()).getCoordinatorLayout(), topicalSearch.getPassages().size() + " passages for '" + topicalSearch.getSearchTerm() + "'", Snackbar.LENGTH_LONG).show();

        recyclerView.setAdapter(new OpenBibleAdapter(topicalSearch.getPassages()));
        ((ActivityBase) getContext()).setActivityProgress(0);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback((ItemTouchHelperAdapter) recyclerView.getAdapter());
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public Pair<AppFeature, Integer> getFeatureForFragment() {
        return new Pair<>(AppFeature.Discover, 0);
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

//Adapter
//--------------------------------------------------------------------------------------------------
    private class OpenBibleViewholder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        CardView cardView;
        TextView bible;
        TextView upvotes;
        TextView text;
        View votingLayout;

        TintableImageView voteUp;
        TintableImageView voteDown;

        public OpenBibleViewholder(View itemView) {
            super(itemView);

            cardView = (CardView) itemView;
            bible = (TextView) itemView.findViewById(R.id.bible);
            upvotes = (TextView) itemView.findViewById(R.id.upvotes);
            text = (TextView) itemView.findViewById(R.id.text);
            votingLayout = itemView.findViewById(R.id.voting_layout);
            voteUp = (TintableImageView) itemView.findViewById(R.id.vote_up);
            voteDown = (TintableImageView) itemView.findViewById(R.id.vote_down);
        }

        public void onBind(final OpenBiblePassage passage) {
            passage.getReference().getBible().setAbbreviation("ESV");

            cardView.setTitle(passage.getReference().toString());
            cardView.setMenuResource(R.menu.card_topical_search);

            bible.setText(passage.getReference().getBible().getAbbreviation());
            upvotes.setText(Util.formatString("{0}", passage.getMetadata().getInt("UPVOTES", 0)));
            text.setText(passage.getText());

            voteUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "Upvoting " + passage.getReference().toString(), Toast.LENGTH_SHORT).show();
                    passage.upvote(new OnResponseListener() {
                        @Override
                        public void responseFinished(boolean success) {
                            passage.getMetadata().putInt("UPVOTES", passage.getMetadata().getInt("UPVOTES", 0) + 1);
                            recyclerView.getAdapter().notifyDataSetChanged();
                        }
                    });
                }
            });

            voteDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "Downvoting " + passage.getReference().toString(), Toast.LENGTH_SHORT).show();

                    passage.downvote(new OnResponseListener() {
                        @Override
                        public void responseFinished(boolean success) {
                            passage.getMetadata().putInt("UPVOTES", passage.getMetadata().getInt("UPVOTES", 0) - 1);
                            recyclerView.getAdapter().notifyDataSetChanged();
                        }
                    });
                }
            });
        }

        @Override
        public void onItemSelected() {
        }

        @Override
        public void onItemClear() {
        }
    }

//Adapter
//--------------------------------------------------------------------------------------------------
    private class OpenBibleAdapter extends RecyclerView.Adapter<OpenBibleViewholder> implements ItemTouchHelperAdapter {
        List<OpenBiblePassage> passages;

        public OpenBibleAdapter(List<OpenBiblePassage> passages) {
            this.passages = passages;
        }

        @Override
        public OpenBibleViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
            CardView cardView = new CardView(getContext());
            parent.addView(cardView);

            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_topicalbible, null, false);
            cardView.addView(itemView);
            return new OpenBibleViewholder(cardView);
        }

        @Override
        public void onBindViewHolder(final OpenBibleViewholder holder, int position) {
            holder.onBind(passages.get(position));
        }

        @Override
        public int getItemCount() {
            return passages.size();
        }

        @Override
        public void onItemDismiss(int position) {
            passages.remove(position);
            notifyItemRemoved(position);
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            Collections.swap(passages, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }
    }
}
