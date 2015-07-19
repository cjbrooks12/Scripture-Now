package com.caseybrooks.scripturememory.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.caseybrooks.common.features.NavigationCallbacks;
import com.caseybrooks.scripturememory.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class DebugSharedPreferencesFragment extends ListFragment {
	Context context;
	SPAdapter adapter;
	NavigationCallbacks mCallbacks;
	EditText editText;

	public SharedPreferences settings[];
	public String settings_names[];

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
		setHasOptionsMenu(true);

		settings = new SharedPreferences[] {
				PreferenceManager.getDefaultSharedPreferences(context),
				context.getSharedPreferences("my_settings", 0)
		};

		settings_names = new String[] {
				"Preferences",
				"Settings"
		};

		editText = new EditText(context);
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
		getListView().addHeaderView(editText);

		initialize();
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

	public int getCount(Context context) {
		int count = 0;
		settings = new SharedPreferences[] {
				PreferenceManager.getDefaultSharedPreferences(context),
				context.getSharedPreferences("my_settings", 0)
		};

		for(int i = 0; i < settings.length; i++) {
			count += settings[i].getAll().size();
		}

		return count;
	}

	public static int getPrefsCount(Context context) {
		return ((DebugSharedPreferencesFragment)newInstance()).getCount(context);
	}

	private void clearAll() {
		new AlertDialog.Builder(context)
				.setPositiveButton("Delete All", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						for(SharedPreferences prefs : settings) {
							prefs.edit().clear().commit();
						}
						initialize();
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				})
				.setTitle("Clear All Preferences")
				.setMessage("Are you sure you want to delete all user preferences? This cannot be undone.")
				.create()
				.show();
	}

	private void initialize() {
		ArrayList<ListItem> displayedItems = new ArrayList<>();

		for(int i = 0; i < settings.length; i++) {
			Map<String, ?> pairs = settings[i].getAll();

			for(String key : pairs.keySet()) {
				ListItem item = new ListItem();

				item.file = settings_names[i];
				item.key = key;

				Object pref = pairs.get(key);
				if(pref instanceof Boolean) {
					item.type = "Boolean";
					item.value = ((Boolean) pref).toString();
				}
				else if(pref instanceof Float) {
					item.type = "Float";
					item.value = ((Float) pref).toString();
				}
				else if(pref instanceof Integer) {
					item.type = "Integer";
					item.value = ((Integer) pref).toString();
				}
				else if(pref instanceof Long) {
					item.type = "Long";
					item.value = ((Long) pref).toString();
				}
				else if(pref instanceof String) {
					item.type = "String";
					item.value = ((String) pref).toString();
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
		}

		adapter = new SPAdapter(context, displayedItems);
		adapter.filterBy(editText.getText().toString());

		setListAdapter(adapter);
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

//Menu
//------------------------------------------------------------------------------
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_debug, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.menu_debug_refresh) {
			initialize();
			return true;
		}
		else if(item.getItemId() == R.id.menu_debug_clear) {
			clearAll();
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
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
