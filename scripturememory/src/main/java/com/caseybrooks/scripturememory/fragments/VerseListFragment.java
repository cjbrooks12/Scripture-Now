package com.caseybrooks.scripturememory.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.container.Verses;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.VersesDatabase;
import com.caseybrooks.scripturememory.data.MetaSettings;

public class VerseListFragment extends ListFragment {
//Data members
//------------------------------------------------------------------------------
	Context context;

	BibleVerseAdapter bibleVerseAdapter;
	String list;//, sort;
	
//Lifecycle and Initialization
//------------------------------------------------------------------------------
    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		getListView().setDivider(null);
		getListView().setDividerHeight(0);
		getListView().setSelector(new StateListDrawable());
		getListView().setFastScrollEnabled(true);
		
		context = getActivity();
		Bundle extras = getArguments();
		if(extras.containsKey("KEY_LIST")) {
			list = extras.getString("KEY_LIST");
		}
		else {
			list = "current";
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(context,
							   R.array.sort_methods, android.R.layout.simple_spinner_dropdown_item);

		ActionBar ab = ((ActionBarActivity) context).getSupportActionBar();
		ab.setHomeButtonEnabled(true);
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setListNavigationCallbacks(spinnerAdapter, navigationListener);
		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ab.setSelectedNavigationItem(MetaSettings.getSortBy(context));

		populateBibleVerses();
	}

	@Override
	public void onPause() {
		super.onPause();
		((ActionBarActivity) context).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	}

	//Custom list Adapter
//------------------------------------------------------------------------------
	public class BibleVerseAdapter extends ArrayAdapter<Passage> {
	    Context context;
	    Verses<Passage> verses;
		
		public BibleVerseAdapter(Context context, Verses<Passage> verses) {
            super(context, R.layout.list_bible_verse, verses.toArray(new Passage[verses.size()]));
			
			this.context = context;
			this.verses = verses;
		}
				
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View itemView = convertView;
			if(itemView == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				itemView = inflater.inflate(R.layout.list_bible_verse, parent, false);
			}
			
			Passage currentVerse = verses.get(position);
			
			TextView reference = (TextView) itemView.findViewById(R.id.item_reference);
			TextView verse = (TextView) itemView.findViewById(R.id.item_verse);
			reference.setText(currentVerse.getReference());
			verse.setText(currentVerse.getText());
			
			return itemView;
		}
		
		@Override
	    public void add(Passage bv) {
	        verses.add(bv);
	        notifyDataSetChanged();
	    }
		
		@Override
	    public void remove(Passage bv) {
	        verses.remove(bv);
	        notifyDataSetChanged();
	    }
		
		public Passage get(int position) {
			return verses.get(position);
		}
	}
	
	public void onListItemClick(ListView lv, View v, int position, long id) {
        Passage currentVerse = bibleVerseAdapter.get(position);
	
		switchToEditFragment(currentVerse.getId());
	}
	
	private void populateBibleVerses() {
		Verses<Passage> verses;
		VersesDatabase db = new VersesDatabase(context);
		
		db.open();
		verses = db.getVerseList(list);
		db.close();
		
		bibleVerseAdapter = new BibleVerseAdapter(context, verses);
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
	private static onListEditListener listener;
	
	public void switchToEditFragment(int id) {
	    if(listener != null) {
	        listener.toEdit(id);
	    }
	}

	public interface onListEditListener {
	    void toEdit(int id);
    }

	public static void setOnListEditListener(onListEditListener listener) {
	    VerseListFragment.listener = listener;
	}
}
