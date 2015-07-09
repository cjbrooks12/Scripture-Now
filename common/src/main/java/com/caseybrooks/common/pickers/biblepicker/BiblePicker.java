package com.caseybrooks.common.pickers.biblepicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
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
	TextView bibleCount;

	Bible selectedBible;

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

		bibleCount = (TextView) findViewById(R.id.bible_list_count);

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
			selectedBible = BiblePickerSettings.getSelectedBible(context);


			try {
				bibles = ABSBible.parseAvailableVersions(
						ABSBible.availableVersionsDoc(
								context.getResources().getString(R.string.bibles_org_key), null
						));
			}
			catch(IOException ioe) {
				//assuming we have previously selected a bible, only show that
				//one in the list if we can't connect to the server right now.
				//In the case that we haven't selected a Bible yet, the settings
				//will return a default Bible that we can use for the time being
				bibles = new HashMap<>();
				bibles.put(selectedBible.getAbbreviation(), selectedBible);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			adapter = new BibleListAdapter(context, bibles.values());
			adapter.filterBy(filter.getText().toString());
			bibleList.setAdapter(adapter);
			bibleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					selectedBible = BiblePickerSettings.getSelectedBible(context);

					if(adapter.getItem(position).equals(selectedBible)) {
						Toast.makeText(context, "Bibles are equal", Toast.LENGTH_SHORT).show();
					}
					 else {
						Toast.makeText(context, "Bibles are not equal", Toast.LENGTH_SHORT).show();
					}

//					Bible item = adapter.getItem(position);
//					BiblePickerSettings.setSelectedBible(context, item);
//					adapter.notifyDataSetChanged();
				}
			});

			switcher.getCurrentView().setVisibility(View.GONE);
			switcher.showNext();
		}
	}

	private class BibleListAdapter extends BaseAdapter {
		private Comparator<Bible> bibleComparator = new Comparator<Bible>() {
			@Override
			public int compare(Bible lhs, Bible rhs) {
				if(lhs.equals(selectedBible)) return Integer.MIN_VALUE;
				else if(rhs.equals(selectedBible)) return Integer.MAX_VALUE;
				else return lhs.getAbbreviation().compareTo(rhs.getAbbreviation());
			}
		};

		ArrayList<Bible> allData;
		ArrayList<Bible> filteredData;

		Context context;
		LayoutInflater layoutInflater;
		int[] colors;

		public BibleListAdapter(Context context, Collection<Bible> data) {
			super();
			this.allData = new ArrayList<>();
			this.allData.addAll(data);
			Collections.sort(this.allData, bibleComparator);

			this.filteredData = new ArrayList<>();
			this.filteredData.addAll(allData);

			this.context = context;
			layoutInflater = LayoutInflater.from(context);

			HashMap<String, String> languages = new HashMap<>();

			for(Bible item : allData) {
				languages.put(item.getLanguage(), item.getLanguage());
			}

			String text = "";

			if(allData.size() == 1)
				text += allData.size() + " Bible in ";
			else {
				text += allData.size() + " Bibles in ";
			}

			if(languages.size() == 1)
				text += languages.size() + " language";
			else {
				text += languages.size() + " languages";
			}

			bibleCount.setVisibility(View.VISIBLE);
			bibleCount.setText(text);

			TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{
					R.attr.color_background,
					R.attr.color_background_selected,
					R.attr.color_text,
					R.attr.colorAccent
			});

			colors = new int[] {
					a.getColor(0, context.getResources().getColor(R.color.background_light)),
					a.getColor(1, context.getResources().getColor(R.color.background_selected_light)),
					a.getColor(2, context.getResources().getColor(R.color.text_dark)),
					a.getColor(3, context.getResources().getColor(R.color.accent_material_light))
			};
			a.recycle();
		}

		public void filterBy(String query) {
			filteredData.clear();

			if(query != null && query.length() > 0) {
				for(Bible item : allData) {
					if(	item.getAbbreviation().toLowerCase().contains(query.toLowerCase()) ||
						item.getLanguage().toLowerCase().contains(query.toLowerCase()) ||
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
				convertView = layoutInflater.inflate(R.layout.bible_picker_item, null);

			Bible item = getItem(position);

			TextView name = (TextView) convertView.findViewById(R.id.bible_name);
			name.setText(item.getName());

			TextView language = (TextView) convertView.findViewById(R.id.bible_language);
			language.setText(item.getLanguage());

			TextView abbreviation = (TextView) convertView.findViewById(R.id.bible_abbreviation);
			abbreviation.setText(item.getAbbreviation());

			if(item.equals(selectedBible)) {
//				convertView.setBackgroundColor(colors[1]);
				name.setTextColor(colors[3]);
			}
			else {
//				convertView.setBackgroundColor(colors[0]);
				name.setTextColor(colors[2]);
			}

			return convertView;
		}
	}
}
