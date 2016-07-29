package com.caseybrooks.common.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.caseybrooks.androidbibletools.basic.Reference;
import com.caseybrooks.androidbibletools.providers.abs.ABSBibleList;
import com.caseybrooks.androidbibletools.widget.BiblePickerDialog;
import com.caseybrooks.androidbibletools.widget.OnReferenceCreatedListener;
import com.caseybrooks.androidbibletools.widget.VersePicker;
import com.caseybrooks.androidbibletools.widget.VersePickerDialog;
import com.caseybrooks.common.R;
import com.caseybrooks.common.features.prayers.AddPrayerCard;
import com.caseybrooks.common.features.verses.SearchResultCard;
import com.caseybrooks.common.util.ItemTouchHelperAdapter;
import com.caseybrooks.common.util.SimpleItemTouchHelperCallback;
import com.caseybrooks.common.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DashboardFragment extends FragmentBase {
    public static DashboardFragment newInstance() {
        DashboardFragment fragment = new DashboardFragment();
        Bundle data = new Bundle();
        fragment.setArguments(data);
        return fragment;
    }

//Data members
//--------------------------------------------------------------------------------------------------
    RecyclerView recyclerView;
    ItemTouchHelper mItemTouchHelper;
    ArrayList<DashboardCardBase> features;

    BiblePickerDialog biblePicker;
    VersePickerDialog versePicker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        createFeatureViews();

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        recyclerView.setAdapter(new DashboardAdapter());

        SimpleItemTouchHelperCallback callback = new SimpleItemTouchHelperCallback((ItemTouchHelperAdapter) recyclerView.getAdapter());
        callback.setIsLongPressDragEnabled(true);
        callback.setIsItemViewSwipeEnabled(true);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        biblePicker = new BiblePickerDialog();
        versePicker = new VersePickerDialog();

        biblePicker.setBibleListClass(ABSBibleList.class, null);
        biblePicker.setOnBibleSelectedListener(versePicker.getVersePicker());

        versePicker.setSelectedBibleTag(null);
        versePicker.setAllowSelectionModeChange(true);
        versePicker.setSelectionMode(VersePicker.SELECTION_MANY_VERSES);
        versePicker.setOnReferenceCreatedListener(new OnReferenceCreatedListener() {
            @Override
            public void onReferenceCreated(Reference.Builder builder) {
                String ref = builder.create().toString();
                getActivityBase().getSearchbox().setText(ref);
                onSearch(ref);
            }
        });

        return view;
    }

    private void createFeatureViews() {
        features = new ArrayList<>();
        for(DashboardFeature feature : getActivityBase().getDashboardFeatures()) {
            try {
                DashboardCardBase card = feature.getViewClass().getConstructor(Context.class).newInstance(getContext());
                features.add(card);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        Collections.sort(features, new Comparator<DashboardCardBase>() {
            @Override
            public int compare(DashboardCardBase lhs, DashboardCardBase rhs) {
                return lhs.getFeatureForView().getPosition() - rhs.getFeatureForView().getPosition();
            }
        });
    }

    private void addCard(DashboardCardBase newCard) {
        int position = 0;
        for(int i = features.size() - 1; i >= 0; i--) {
            int newCardPosition = newCard.getFeatureForView().getPosition();
            int featurePosition = features.get(i).getFeatureForView().getPosition();
            Log.i("DashboardFragment", Util.formatString("Inseting card: newCardPosition={0}, featurePosition={1}, i={2}", newCardPosition, featurePosition, i));
            if(features.get(i).getFeatureForView().getPosition() <= i) {
                position = i;
                break;
            }
        }

        features.add(position, newCard);
        recyclerView.getAdapter().notifyItemInserted(position);
    }

    @Override
    public Pair<AppFeature, Integer> getFeatureForFragment() {
        return new Pair<>(AppFeature.Dashboard, 0);
    }

//Screen Decor
//--------------------------------------------------------------------------------------------------
    @Override
    public boolean onFABPressed() {
        AddPrayerCard card = new AddPrayerCard(getContext());
        addCard(card);

        return true;
    }

    @Override
    public int getFABIcon() {
        return R.drawable.ic_plus;
    }


//Searchbox interface
//--------------------------------------------------------------------------------------------------
    @Override
    public String getSearchHint() {
        return "Enter Reference";
    }

    @Override
    public int getSearchMenuResourceId() {
        return R.menu.search_lookup_verse;
    }

    @Override
    public boolean usesSearchBox() {
        return true;
    }

    @Override
    public boolean onSearch(String query) {
        SearchResultCard card = new SearchResultCard(getContext());
        card.setReference(query);
        addCard(card);

        return true;
    }

    @Override
    public boolean onSearchMenuItemSelected(MenuItem selectedItem) {
        if(selectedItem.getItemId() == R.id.bible_picker) {
            biblePicker.show(getActivityBase().getSupportFragmentManager(), "");
            getActivityBase().getSearchbox().hideInstant(getActivityBase());
            return true;
        }
        else if(selectedItem.getItemId() == R.id.verse_picker) {
            versePicker.show(getActivityBase().getSupportFragmentManager(), "");
            getActivityBase().getSearchbox().hideInstant(getActivityBase());
            return true;
        }
        else {
            return false;
        }
    }

//Dashboard cards adapter
//--------------------------------------------------------------------------------------------------
    private class DashboardViewHolder extends RecyclerView.ViewHolder  {
        public DashboardViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class DashboardAdapter extends RecyclerView.Adapter<DashboardViewHolder> implements ItemTouchHelperAdapter {

        @Override
        public DashboardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            for (DashboardCardBase card : features) {
                if(card.getFeatureForView().getId() == viewType) {
                    return new DashboardViewHolder(card);
                }
            }

            return null;
        }

        @Override
        public int getItemViewType(int position) {
            return features.get(position).getFeatureForView().getId(); //forces every item in the list to be its own view
        }

        @Override
        public void onBindViewHolder(final DashboardViewHolder holder, int position) {
            //since every item is handled as its own view, we do not need to bind anything here
        }

        @Override
        public int getItemCount() {
            return features.size();
        }

        @Override
        public void onItemDismiss(int position) {
            features.remove(position);
            notifyItemRemoved(position);
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            Collections.swap(features, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }
    }

}
