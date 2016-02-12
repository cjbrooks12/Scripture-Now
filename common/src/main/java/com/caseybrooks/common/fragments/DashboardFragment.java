package com.caseybrooks.common.fragments;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.caseybrooks.androidbibletools.basic.Reference;
import com.caseybrooks.androidbibletools.providers.abs.ABSBibleList;
import com.caseybrooks.androidbibletools.widget.BiblePickerDialog;
import com.caseybrooks.androidbibletools.widget.OnReferenceCreatedListener;
import com.caseybrooks.androidbibletools.widget.VersePicker;
import com.caseybrooks.androidbibletools.widget.VersePickerDialog;
import com.caseybrooks.common.R;
import com.caseybrooks.common.app.AppFeature;
import com.caseybrooks.common.app.FragmentBase;
import com.caseybrooks.common.dashboard.SearchResultCard;

public class DashboardFragment extends FragmentBase {
    public static DashboardFragment newInstance() {
        DashboardFragment fragment = new DashboardFragment();
        Bundle data = new Bundle();
        fragment.setArguments(data);
        return fragment;
    }

    LinearLayout cardParent;
    SearchResultCard searchResultCard;
    BiblePickerDialog biblePicker;
    VersePickerDialog versePicker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        cardParent = (LinearLayout) view.findViewById(R.id.cardParent);

        searchResultCard = (SearchResultCard) view.findViewById(R.id.searchResultCard);
        cardParent.removeView(searchResultCard);

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

    @Override
    public Pair<AppFeature, Integer> getFeatureForFragment() {
        return new Pair<>(AppFeature.Dashboard, 0);
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
        searchResultCard.setReference(query);
        cardParent.removeView(searchResultCard);
        cardParent.addView(searchResultCard);

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
}
