package com.caseybrooks.common.pickers.biblepicker;

import android.content.Context;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.caseybrooks.androidbibletools.basic.Bible;
import com.caseybrooks.androidbibletools.providers.abs.ABSBible;
import com.caseybrooks.common.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class BiblePicker extends LinearLayout {
	Context context;
	ViewSwitcher switcher;

	ListView bibleList;
	BibleListAdapter adapter;

	EditText filter;

	public BiblePicker(Context context) {
		super(context);
		this.context = context;

		initialize();
	}

	public BiblePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

		initialize();
	}

	public void initialize() {
		LayoutInflater.from(context).inflate(R.layout.bible_picker, this);

		switcher = (ViewSwitcher) findViewById(R.id.picker_content_switcher);

		bibleList = (ListView) findViewById(R.id.bible_list);

		filter = (EditText) findViewById(R.id.bible_list_filter);
		filter.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(adapter != null)	adapter.filterBy(filter.getText().toString());
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		new LoadBiblesThread().execute();
	}

	private class LoadBiblesThread extends AsyncTask<Void, Void, Void> {
		HashMap<String, Bible> bibles;

		@Override
		protected Void doInBackground(Void... params) {
			try {
				bibles = ABSBible.parseAvailableVersions(
						ABSBible.availableVersionsDoc(
								context.getResources().getString(R.string.bibles_org_key), null
						));
			}
			catch(IOException ioe) {

			}

			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			if(bibles != null) {
				adapter = new BibleListAdapter(context, bibles.values());
				adapter.filterBy(filter.getText().toString());
				bibleList.setAdapter(adapter);
				switcher.showNext();
			}
		}
	}

	private class BibleListAdapter extends BaseAdapter {
		private Comparator<Bible> bibleComparator = new Comparator<Bible>() {
			@Override
			public int compare(Bible lhs, Bible rhs) {
				return lhs.getAbbr().compareTo(rhs.getAbbr());
			}
		};

		ArrayList<Bible> allData;
		ArrayList<Bible> filteredData;

		Context context;
		LayoutInflater layoutInflater;

		public BibleListAdapter(Context context, Collection<Bible> data) {
			super();
			this.allData = new ArrayList<>();
			this.allData.addAll(data);
			Collections.sort(this.allData, bibleComparator);

			this.filteredData = new ArrayList<>();
			this.filteredData.addAll(allData);

			this.context = context;
			layoutInflater = LayoutInflater.from(context);
		}

		public void filterBy(String query) {
			filteredData.clear();

			if(query != null && query.length() > 0) {
				for(Bible item : allData) {
					if(	item.getAbbr().toLowerCase().contains(query.toLowerCase()) ||
						item.getName().toLowerCase().contains(query.toLowerCase()))
					{
						filteredData.add(item);
					}
				}
			}
			else {
				filteredData.addAll(allData);
			}

			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return filteredData.size();
		}

		@Override
		public Bible getItem(int position) {
			return filteredData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null)
				convertView= layoutInflater.inflate(R.layout.bible_picker_item, null);

			TextView name = (TextView) convertView.findViewById(R.id.bible_name);
			name.setText(getItem(position).getName());

			TextView abbr = (TextView) convertView.findViewById(R.id.bible_abbr);
			abbr.setText(getItem(position).getAbbr());

			return convertView;
		}
	}
}
