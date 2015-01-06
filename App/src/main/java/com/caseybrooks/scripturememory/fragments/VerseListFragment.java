package com.caseybrooks.scripturememory.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.data.Metadata;
import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.activities.MainActivity;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.misc.BibleVerseAdapter;
import com.caseybrooks.scripturememory.misc.NavigationCallbacks;
import com.caseybrooks.scripturememory.notifications.MainNotification;

import java.util.ArrayList;
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

        float density = getResources().getDisplayMetrics().density;

        getListView().setDivider(null);
		getListView().setDividerHeight(0);//(int)(2*density));
		getListView().setSelector(new StateListDrawable());
		getListView().setFastScrollEnabled(true);
        getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);
        getListView().setPadding((int)(8*density), 0, (int)(8*density), 0);
		
		context = getActivity();
		Bundle extras = getArguments();
        listType = extras.getInt("KEY_LIST_TYPE");
        listId = extras.getInt("KEY_LIST_ID");
	}
	
	@Override
	public void onResume() {
		super.onResume();

		ab = ((MainActivity) context).getSupportActionBar();
		ab.setHomeButtonEnabled(true);
		ab.setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);

        String title;
        int color;

        db = new VerseDB(context).open();
        if(listType == TAGS) {
            title = db.getTagName(listId);
            color = db.getTagColor(db.getTagName(listId));
            MetaSettings.putDrawerSelection(context, 3, listId);
        }
        else {
            title = db.getStateName(listId);
            color = db.getStateColor(listId);
            MetaSettings.putDrawerSelection(context, 2, listId);
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
        if(mActionMode != null) mActionMode.finish();
    }

    AdapterView.OnItemClickListener iconClick = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(mActionMode == null) {
                mActionMode = ((ActionBarActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                mActionMode.setTitle("1");
            }
            else if(bibleVerseAdapter.getSelectedCount() == 0) {
                mActionMode.finish();
            }
            else {
                mActionMode.setTitle(bibleVerseAdapter.getSelectedCount() + "");
            }
        }
    };

    AdapterView.OnItemClickListener itemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mCallbacks.toVerseEdit((int)id);
        }
    };

    AdapterView.OnItemClickListener overflowClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final BibleVerseAdapter.ViewHolder vh = (BibleVerseAdapter.ViewHolder) view.getTag();
            final ArrayList<Passage> listOfOne = new ArrayList<>();
            listOfOne.add(vh.passage);

            PopupMenu popup = new PopupMenu(context, view);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.context_list_post:
                            MetaSettings.putVerseId(context, vh.passage.getMetadata().getInt(DefaultMetaData.ID));
                            MetaSettings.putNotificationActive(context, true);
                            MainNotification.notify(context).show();
                            Toast.makeText(context, vh.passage.getReference().toString() + " set as notification", Toast.LENGTH_SHORT).show();
                            return true;
                        case R.id.context_list_add_tag:
                            addTag(listOfOne);
                            return true;
                        case R.id.context_list_change_state:
                            changeState(listOfOne);
                            return true;
                        case R.id.context_list_view_in_broswer:
                            String url = vh.passage.getURL();
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            Toast.makeText(context, "Opening browser...", Toast.LENGTH_SHORT).show();
                            context.startActivity(i);
                            return true;
                        case R.id.context_list_share:
                            return true;
                        case R.id.context_list_delete:
                            delete(listOfOne);
                            return true;
                        default:
                            return false;
                    }
                }
            });
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.context_list, popup.getMenu());
            popup.show();
        }
    };

	private void populateBibleVerses() {
        ArrayList<Passage> verses;
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
                comparator = new Metadata.Comparator(DefaultMetaData.TIME_CREATED);
                break;
            case 1:
                comparator = new Metadata.Comparator(Metadata.Comparator.KEY_REFERENCE);
                break;
            case 2:
                comparator = new Metadata.Comparator(Metadata.Comparator.KEY_REFERENCE_ALPHABETICAL);
                break;
            case 3:
                comparator = new Metadata.Comparator(DefaultMetaData.STATE);
                break;
            default:
                comparator = new Metadata.Comparator("ID");
                break;
        }

        Collections.sort(verses, comparator);

		bibleVerseAdapter = new BibleVerseAdapter(context, verses, getListView());
        bibleVerseAdapter.setOnItemClickListener(itemClick);
        bibleVerseAdapter.setOnItemMultiselectListener(iconClick);
        bibleVerseAdapter.setOnItemOverflowClickListener(overflowClick);
		setListAdapter(bibleVerseAdapter);
	}

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

//ActionBar items
//------------------------------------------------------------------------------
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater = ((ActionBarActivity) context).getMenuInflater();
        inflater.inflate(R.menu.menu_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_list_sort_date:
                MetaSettings.putSortBy(context, 0);
                populateBibleVerses();
                return true;
            case R.id.menu_list_sort_canonical:
                MetaSettings.putSortBy(context, 1);
                populateBibleVerses();
                return true;
            case R.id.menu_list_sort_alphabetically:
                MetaSettings.putSortBy(context, 2);
                populateBibleVerses();
                return true;
            case R.id.menu_list_sort_mem_state:
                MetaSettings.putSortBy(context, 3);
                populateBibleVerses();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//Contextual ActionMode for multi-selection
//------------------------------------------------------------------------------
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.contextual_list, menu);
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
            switch (item.getItemId()) {
                //clicked back button to close the CAB
                case android.R.id.home:
                    mode.finish();
                    return true;
                //select all verses in the list
                case R.id.contextual_list_select_all:
                    ArrayList<Passage> items = bibleVerseAdapter.getItems();

                    int firstPosition = getListView().getFirstVisiblePosition();
                    int lastPosition = firstPosition + getListView().getChildCount() - 1;

                    for(Passage passage : items) {
                        int position = passage.getMetadata().getInt("LIST_POSITION");

                        if(!passage.getMetadata().getBoolean(DefaultMetaData.IS_CHECKED)) {
                            if (position >= firstPosition && position <= lastPosition) {
                                View view = getListView().getChildAt(position - firstPosition);
                                if (view != null) {
                                    BibleVerseAdapter.ViewHolder vh = (BibleVerseAdapter.ViewHolder) view.getTag();
                                    vh.multiSelect();
                                }
                            }
                            else {
                                passage.getMetadata().putBoolean(DefaultMetaData.IS_CHECKED, true);
                            }
                        }
                    }

                    //update count in toolbar
                    mActionMode.setTitle(bibleVerseAdapter.getSelectedCount() + " Selected");

                    //just to ensure that all verses correctly reflect their selected state in case of issues
                    bibleVerseAdapter.notifyDataSetChanged();

                    return true;

                //delete all selected verses
                case R.id.contextual_list_delete:
                    delete(bibleVerseAdapter.getSelectedItems());
                    return true;

                //export selected verses to XML file and save to SD card
                case R.id.contextual_list_export:
                    export(bibleVerseAdapter.getSelectedItems());
                    return true;

                //add a tag to all selected verses
                case R.id.contextual_list_add_tag:
                    addTag(bibleVerseAdapter.getSelectedItems());
                    return true;

                //change the memorization state of all selected verses
                case R.id.contextual_list_change_state:
                    changeState(bibleVerseAdapter.getSelectedItems());
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;

            //deselect all items
            ArrayList<Passage> selectedItems = bibleVerseAdapter.getSelectedItems();

            int firstPosition = getListView().getFirstVisiblePosition();
            int lastPosition = firstPosition + getListView().getChildCount() - 1;

            for(Passage passage : selectedItems) {
                int position = passage.getMetadata().getInt("LIST_POSITION");
                if( position >= firstPosition && position <= lastPosition) {

                    View view = getListView().getChildAt(position - firstPosition);
                    if(view != null) {
                        BibleVerseAdapter.ViewHolder vh = (BibleVerseAdapter.ViewHolder) view.getTag();
                        vh.multiSelect();
                    }
                }
                else {
                    passage.getMetadata().putBoolean(DefaultMetaData.IS_CHECKED, false);
                }
            }
            //just to ensure that all verses correctly reflect their selected state in case of issues
            bibleVerseAdapter.notifyDataSetChanged();
        }
    };

//Actions to perform on verses within the listview. Size-generic, so one call for both single and multiples
//------------------------------------------------------------------------------

    private void delete(final ArrayList<Passage> items) {
        final View view = LayoutInflater.from(context).inflate(R.layout.popup_delete_verse, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        final AlertDialog dialog = builder.create();

        TextView verseList = (TextView) view.findViewById(R.id.verse_list);
        String message = "";
        for(Passage passage : items) {
            message += passage.getReference().toString() + "\n";
        }
        verseList.setText(message.trim());

        TextView cancelButton = (TextView) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        TextView deleteButton = (TextView) view.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VerseDB db = new VerseDB(context).open();
                for(Passage passage : items) {
                    db.deleteVerse(passage);
                    bibleVerseAdapter.removeItem(passage);
                }
                db.close();
                dialog.cancel();
                if(mActionMode != null) mActionMode.finish();
                bibleVerseAdapter.notifyDataSetChanged();

                String toastMessage = "";
                if(items.size() > 1) toastMessage += items.size() + " verses deleted";
                else toastMessage += items.get(0).getReference().toString() + " deleted";

                Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    //TODO: implement exporting of verse lists as XML
    private void export(ArrayList<Passage> items) {

    }

    //TODO: implement change of state through alert dialog
    private void changeState(ArrayList<Passage> items) {

    }

    private void addTag(final ArrayList<Passage> items) {
        final View view = LayoutInflater.from(context).inflate(R.layout.popup_new_tag, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        final AlertDialog dialog = builder.create();

        final AutoCompleteTextView edit = (AutoCompleteTextView) view.findViewById(R.id.edit_text);
        VerseDB db = new VerseDB(context).open();

        String[] tagSuggestions = db.getAllTagNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                context,
                android.R.layout.simple_list_item_1,
                tagSuggestions
        );
        edit.setAdapter(adapter);

        TextView cancelButton = (TextView) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        TextView addButton = (TextView) view.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = edit.getText().toString().trim();
                if(text.length() > 0) {
                    VerseDB db = new VerseDB(context).open();
                    for(Passage passage : items) {
                        passage.addTag(text);
                        db.updateVerse(passage);
                    }
                    db.close();
                    dialog.cancel();
                    if(mActionMode != null) mActionMode.finish();
                    bibleVerseAdapter.notifyDataSetChanged();

                    String toastMessage = "Tag '" + text + "' added to ";
                    if(items.size() > 1) toastMessage += items.size() + " verses";
                    else toastMessage += items.get(0).getReference().toString();

                    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();
    }
}