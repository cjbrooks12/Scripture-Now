package com.caseybrooks.common.fragments;

import android.os.Bundle;
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
import com.caseybrooks.androidbibletools.widget.VerseView;
import com.caseybrooks.common.R;
import com.caseybrooks.common.app.AppFeature;
import com.caseybrooks.common.app.FragmentBase;

//TODO: save and restore reading progress
//TODO: add forward and backward navigation buttons
//TODO: (ABT) add way to get the previous and next verse/chapter/book
//TODO: (ABT) create easy way to serialize and deserialize Verses like I have with Bibles
//TODO: (ABT) set selectionMode on Dialogs after already created, and add option to diable user selecting selectionMode.
//TODO: (ABT) Restyle selectionMode button
//TODO: (ABT) add some getters to the widgets so I can reuse some of their internal data structures
//TODO: (ABT) use flags for defaults within Builder to know whether to automatically paint selections in versepicker
//TODO: (ABT) use flags for defaults within Builder to know how to display a Reference with .toString()
//TODO: (ABT) pass flags from Builder to Reference, or make a Reference have a constant toString() value that is created by the Builder
public class BibleReaderFragment extends FragmentBase {
    public static BibleReaderFragment newInstance() {
        BibleReaderFragment fragment = new BibleReaderFragment();
        Bundle data = new Bundle();
        fragment.setArguments(data);
        return fragment;
    }

    BiblePickerDialog biblePicker;
    VersePickerDialog versePicker;
    VerseView verseView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_biblereader, container, false);

        verseView = (VerseView) view.findViewById(R.id.verse_view);

        biblePicker = new BiblePickerDialog();
        versePicker = new VersePickerDialog();

        biblePicker.setBibleListClass(ABSBibleList.class, null);
        biblePicker.setOnBibleSelectedListener(versePicker.getVersePicker());

        versePicker.setSelectedBibleTag(null);
        versePicker.setAllowSelectionModeChange(false);
        versePicker.setSelectionMode(VersePicker.SELECTION_WHOLE_CHAPTER);
        versePicker.setOnReferenceCreatedListener(new OnReferenceCreatedListener() {
            @Override
            public void onReferenceCreated(Reference.Builder builder) {
                getActivityBase().getToolbar().setSubtitle(builder.create().toString());
                getActivityBase().getSearchbox().setText(builder.create().toString());

                verseView.onReferenceCreated(builder);
            }
        });

        return view;
    }

    @Override
    public Pair<AppFeature, Integer> getFeatureForFragment() {
        return new Pair<>(AppFeature.Read, 0);
    }

//Searchbox interface
//--------------------------------------------------------------------------------------------------

    @Override
    public String getSearchHint() {
        return "Search Chapter";
    }

    @Override
    public int getSearchMenuResourceId() {
        return R.menu.search_lookup_verse;
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

    @Override
    public boolean usesSearchBox() {
        return true;
    }
}
