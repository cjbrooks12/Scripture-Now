package com.caseybrooks.scripturememory.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;

import java.util.ArrayList;

public class Changelog extends ActionBarActivity {
//Data Members
//------------------------------------------------------------------------------
	ListView listview;
    Context context;

//Lifecycle and Initialization
//------------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		context = this;
		int theme = MetaSettings.getAppTheme(context);
		if(theme == 0) setTheme(R.style.Theme_ScriptureMemory_Light);
		else setTheme(R.style.Theme_ScriptureMemory_Dark);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_changelog);
		
		listview = (ListView) findViewById(R.id.changelogListview);
		
		listview.setDivider(null);
		listview.setDividerHeight(0);
		listview.setSelector(new StateListDrawable());
		listview.setFastScrollEnabled(true);
		listview.setPadding(32, 16, 32, 16);
		
		populateBibleVerses();
	}
	
	private class ChangelogEntry {
		public String title, detail;
	}
	
	private class ChangelogAdapter extends ArrayAdapter<ChangelogEntry> {
	    Context context;
	    ArrayList<ChangelogEntry> entries;
		
		public ChangelogAdapter(Context context, ArrayList<ChangelogEntry> entries) {
			super(context, R.layout.list_changelog, entries);
			
			this.context = context;
			this.entries = entries;
		}
				
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View itemView = convertView;
			if(itemView == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				itemView = inflater.inflate(R.layout.list_changelog, parent, false);
			}
			
			ChangelogEntry currentEntry = entries.get(position);
			
			TextView time = (TextView) itemView.findViewById(R.id.item_title);
			TextView detail = (TextView) itemView.findViewById(R.id.item_detail);
			time.setText(currentEntry.title);
			detail.setText(currentEntry.detail);
			
			return itemView;
		}
	}
	
	ChangelogAdapter changelogAdapter;
	
	private void populateBibleVerses() {
		ArrayList<ChangelogEntry> entries = new ArrayList<ChangelogEntry>();
		
		String[] changelogTitle = getResources().getStringArray(R.array.changelog_title);
		String[] changelogDetail = getResources().getStringArray(R.array.changelog_detail);	
		
		for(int i = changelogTitle.length - 1; i >= 0; i = i - 1) {
			ChangelogEntry entry = new ChangelogEntry();
			entry.title = changelogTitle[i];
			entry.detail = changelogDetail[i];
			entries.add(entry);
		}
		
		changelogAdapter = new ChangelogAdapter(this, entries);
		listview.setAdapter(changelogAdapter);
	}
}
