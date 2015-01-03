package com.caseybrooks.scripturememory.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.container.Verses;
import com.caseybrooks.androidbibletools.data.MetaData;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.activities.MainActivity;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.misc.BibleVerseAdapter;
import com.caseybrooks.scripturememory.misc.NavigationCallbacks;

import java.util.Collections;
import java.util.Comparator;

public class VerseListFragment extends ListFragment {
//enums for creating new fragment
//------------------------------------------------------------------------------
    public static final int TAGS = 0;
    public static final int STATE = 1;

    public static Fragment newInstance(int type, int id) {
        Fragment fragment = new VerseListFragment();
        Bundle data = new Bundle();
        data.putInt("KEY_LIST_TYPE", type);
        data.putInt("KEY_LIST_ID", id);
        fragment.setArguments(data);
        return fragment;
    }

//Data members
//------------------------------------------------------------------------------
	Context context;
    ActionBar ab;
    ActionMode mActionMode;
    NavigationCallbacks mCallbacks;

	BibleVerseAdapter bibleVerseAdapter;
	int listType;
    int listId;

    VerseDB db;
	
//Lifecycle and Initialization
//------------------------------------------------------------------------------
    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getListView().setDivider(null);
		getListView().setDividerHeight(0);
		getListView().setSelector(new StateListDrawable());
		getListView().setFastScrollEnabled(true);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		context = getActivity();
		Bundle extras = getArguments();
        listType = extras.getInt("KEY_LIST_TYPE");
        listId = extras.getInt("KEY_LIST_ID");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(context,
							   R.array.sort_methods, android.R.layout.simple_spinner_dropdown_item);

		ab = ((MainActivity) context).getSupportActionBar();
		ab.setHomeButtonEnabled(true);
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setListNavigationCallbacks(spinnerAdapter, navigationListener);
		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ab.setSelectedNavigationItem(MetaSettings.getSortBy(context));

        String title;
        int color;

        db = new VerseDB(context).open();
        if(listType == TAGS) {
            title = db.getTagName(listId);
            color = db.getTagColor(db.getTagName(listId));
        }
        else {
            title = db.getStateName(listId);
            color = db.getStateColor(listId);
        }

        ((MainActivity)context).getSupportActionBar().setTitle(title);

        db.close();

        ColorDrawable colorDrawable = new ColorDrawable(color);
        ab.setBackgroundDrawable(colorDrawable);

		populateBibleVerses();
	}

	@Override
	public void onPause() {
		super.onPause();
		((ActionBarActivity) context).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }

    BibleVerseAdapter.OnMultiSelectListener iconClick = new BibleVerseAdapter.OnMultiSelectListener() {
        @Override
        public void onMultiSelect(View view, int position) {
            if (mActionMode != null) {

            }

            // Start the CAB using the ActionMode.Callback defined above
            mActionMode = ((ActionBarActivity)getActivity()).startSupportActionMode(mActionModeCallback);
        }
    };

    BibleVerseAdapter.OnItemClickListener itemClick = new BibleVerseAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            mCallbacks.toVerseEdit((int)bibleVerseAdapter.getItemId(position));
        }
    };

	private void populateBibleVerses() {
		Verses<Passage> verses;
        MainActivity mainActivity = (MainActivity) getActivity();

		db.open();
        if(listType == TAGS) {
            verses = db.getTaggedVerses(listId);
            mainActivity.setTitle(db.getTagName(listId));
        }
        else if(listType == STATE) {
            if(listId != 0) {
                verses = db.getStateVerses(listId);
                mainActivity.setTitle(db.getStateName(listId));
            }
            else {
                verses = db.getAllCurrentVerses();
                mainActivity.setTitle("All");
            }
        }
        else {
            verses = db.getAllCurrentVerses();
            mainActivity.setTitle("All");
        }
		db.close();

        Comparator comparator;

        switch(MetaSettings.getSortBy(context)) {
            case 0:
                comparator = new MetaData.Comparator("TIME_CREATED");
                break;
            case 1:
                comparator = new MetaData.Comparator(MetaData.Comparator.KEY_REFERENCE);
                break;
            case 2:
                comparator = new MetaData.Comparator(MetaData.Comparator.KEY_REFERENCE_ALPHABETICAL);
                break;
            case 3:
            default:
                comparator = new MetaData.Comparator("ID");
                break;
        }

        Collections.sort(verses.verses, comparator);

		bibleVerseAdapter = new BibleVerseAdapter(context, verses);
        bibleVerseAdapter.setOnMultiSelectListener(iconClick);
        bibleVerseAdapter.setOnItemClickListener(itemClick);
		setListAdapter(bibleVerseAdapter);
	}

//ActionBar Spinner
//------------------------------------------------------------------------------
    ActionBar.OnNavigationListener navigationListener = new ActionBar.OnNavigationListener() {
        @Override
        public boolean onNavigationItemSelected(int position, long itemId) {

            String[] strings = getActivity().getResources().getStringArray(R.array.sort_methods);

			MetaSettings.putSortBy(context, position);
            populateBibleVerses();

            return true;
        }
    };
	
//Host Activity Interface
//------------------------------------------------------------------------------
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

//Contextual ActionMode for multi-selection
//------------------------------------------------------------------------------
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
//            MenuInflater inflater = mode.getMenuInflater();
//            inflater.inflate(R.menu.context_menu, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//            switch (item.getItemId()) {
//                case R.id.menu_share:
//                    shareCurrentItem();
//                    mode.finish(); // Action picked, so close the CAB
//                    return true;
//                default:
//                    return false;
//            }

            return false;
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    };
}