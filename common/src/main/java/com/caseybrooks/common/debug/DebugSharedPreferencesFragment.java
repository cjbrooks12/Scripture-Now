package com.caseybrooks.common.debug;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.caseybrooks.common.R;
import com.caseybrooks.common.features.NavigationCallbacks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class DebugSharedPreferencesFragment extends ListFragment {
	Context context;
	SPAdapter adapter;
	NavigationCallbacks mCallbacks;

	public static final String settings_file = "my_settings";

	public static Fragment newInstance() {
		Fragment fragment = new DebugSharedPreferencesFragment();
		Bundle extras = new Bundle();
		fragment.setArguments(extras);
		return fragment;
	}

	private static class ListItem implements Comparable<ListItem> {
		String file;
		String type;
		String key;
		String value;

		@Override
		public int compareTo(ListItem another) {
			return key.compareTo(another.key);
		}
	}

//Lifecycle and Initialization
//------------------------------------------------------------------------------

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		context = getActivity();

		adapter = new SPAdapter(context, initialize());

		final EditText editText = new EditText(context);
		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				adapter.filterBy(editText.getText().toString());
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		setListAdapter(adapter);
		getListView().addHeaderView(editText);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		context = getActivity();

		return view;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();

		mCallbacks.setToolBar("All Preferences", Color.parseColor("#FFC107"));
	}

	public static int getPrefsCount(Context context) {
		Map<String, ?> settings = PreferenceManager.getDefaultSharedPreferences(context).getAll();
		Map<String, ?> prefs = context.getSharedPreferences(settings_file, 0).getAll();

		return settings.size() + prefs.size();
	}

	private ArrayList<ListItem> initialize() {
		Map<String, ?> settings = PreferenceManager.getDefaultSharedPreferences(context).getAll();
		Map<String, ?> prefs = context.getSharedPreferences(settings_file, 0).getAll();

		ArrayList<ListItem> displayedItems = new ArrayList<>();

		for (String key : settings.keySet()) {
			ListItem item = new ListItem();

			item.file = "Settings";
			item.key = key;

			Log.e("KEY", key);

			Object pref = settings.get(key);
			if(pref instanceof Boolean) {
				item.type = "Boolean";
				item.value = ((Boolean)pref).toString();
			}
			else if(pref instanceof Float) {
				item.type = "Float";
				item.value = ((Float)pref).toString();
			}
			else if(pref instanceof Integer) {
				item.type = "Integer";
				item.value = ((Integer)pref).toString();
			}
			else if(pref instanceof Long) {
				item.type = "Long";
				item.value = ((Long)pref).toString();
			}
			else if(pref instanceof String) {
				item.type = "String";
				item.value = ((String)pref).toString();
			}
			else {
				if(pref != null) {
					item.type = pref.getClass().getSimpleName();
					item.value = "(generic)" + pref.toString();
				}
				else {
					item.type = "null";
					item.value = "null";
				}
			}

			displayedItems.add(item);
		}

		for (String key : prefs.keySet()) {
			ListItem item = new ListItem();

			item.file = "Preferences";
			item.key = key;

			Log.e("KEY", key);

			Object pref = prefs.get(key);
			if(pref instanceof Boolean) {
				item.type = "Boolean";
				item.value = ((Boolean)pref).toString();
			}
			else if(pref instanceof Float) {
				item.type = "Float";
				item.value = ((Float)pref).toString();
			}
			else if(pref instanceof Integer) {
				item.type = "Integer";
				item.value = ((Integer)pref).toString();
			}
			else if(pref instanceof Long) {
				item.type = "Long";
				item.value = ((Long)pref).toString();
			}
			else if(pref instanceof String) {
				item.type = "String";
				item.value = ((String)pref).toString();
			}
			else {
				if(pref != null) {
					item.type = pref.getClass().getSimpleName();
					item.value = "(generic)" + pref.toString();
				}
				else {
					item.type = "null";
					item.value = "null";
				}
			}

			displayedItems.add(item);
		}

		return displayedItems;
	}

	private static class SPAdapter extends BaseAdapter {
		ArrayList<ListItem> allData;
		ArrayList<ListItem> filteredData;


		Context context;
		LayoutInflater layoutInflater;

		public SPAdapter(Context context, ArrayList<ListItem> data) {
			super();
			this.allData = data;
			Collections.sort(this.allData);

			this.filteredData = new ArrayList<>();
			this.filteredData.addAll(allData);

			this.context = context;
			layoutInflater = LayoutInflater.from(context);
		}

		public void filterBy(String query) {
			filteredData.clear();

			if(query != null && query.length() > 0) {
				for(ListItem item : allData) {
					if(
							item.key.toLowerCase().contains(query.toLowerCase()) ||
							item.type.toLowerCase().contains(query.toLowerCase()) ||
							item.file.toLowerCase().contains(query.toLowerCase()) ||
							item.value.toLowerCase().contains(query.toLowerCase())) {
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
		public ListItem getItem(int position) {
			return filteredData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null)
				convertView= layoutInflater.inflate(R.layout.debug_shared_preferences_list_item, null);

			TextView key = (TextView) convertView.findViewById(R.id.sp_key);
			key.setText(getItem(position).key);

			TextView file = (TextView) convertView.findViewById(R.id.sp_file);
			file.setText(getItem(position).file + " (" + getItem(position).type + ")");

			TextView value = (TextView) convertView.findViewById(R.id.sp_value);
			value.setText(getItem(position).value);

			return convertView;
		}

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
}
