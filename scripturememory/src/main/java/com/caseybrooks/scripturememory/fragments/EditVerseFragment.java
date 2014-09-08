package com.caseybrooks.scripturememory.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.VersesDatabase;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.notifications.MainNotification;

public class EditVerseFragment extends Fragment {
//Data Members
//------------------------------------------------------------------------------
	Context context;
	View view;
	VersesDatabase db;
	
	ActionBar ab;
	EditText reference, verse;
	
	int id;
	String list;
	
//Lifecycle and Initialization
//------------------------------------------------------------------------------
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
//		int theme = Integer.parseInt(preferences.getString("PREF_SELECTED_THEME", "0"));
//		if(theme == 0) new ContextThemeWrapper(getActivity(), R.style.Theme_ScriptureMemory_Light);
//		else new ContextThemeWrapper(getActivity(), R.style.Theme_ScriptureMemory_Light);
		
		view = inflater.inflate(R.layout.fragment_edit_verse, container, false); 
        context = getActivity();	
        db = new VersesDatabase(context);
        initialize();
      
        return view;
	}

	private void initialize() {
		id = getActivity().getIntent().getIntExtra("KEY_ID", 1);
		db.open();
		Passage verse = db.getEntryAt(id);
		db.close();
		
		reference = (EditText) view.findViewById(R.id.updateReference);
		reference.setText(verse.getReference());
		this.verse = (EditText) view.findViewById(R.id.updateVerse);
		this.verse.setText(verse.getText());
		
		list = verse.getTags()[0];
		
		setHasOptionsMenu(true);
        setupActionBar();
	}
	
//ActionBar
//------------------------------------------------------------------------------
	private void setupActionBar() {
		ab = ((ActionBarActivity) context).getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
	}

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if(MetaSettings.getVerseId(context) == id) {
            menu.removeItem(R.id.menu_edit_delete);
            menu.removeItem(R.id.menu_edit_change_list);
        }
        if(list.equals("memorized")) {
			menu.removeItem(R.id.menu_edit_set_notification);
		}
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater = ((ActionBarActivity) context).getMenuInflater();
	    inflater.inflate(R.menu.menu_edit_verse, menu);
	    if(list.equals("memorized")) {
	    	menu.findItem(R.id.menu_edit_change_list).setTitle("Move to Current");
	    }
	    else {
	    	menu.findItem(R.id.menu_edit_change_list).setTitle("Move to Memorized");
	    }
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case android.R.id.home:
	    	returnToDashboard();
	    	((ActionBarActivity) context).overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	    	return true;
	    case R.id.menu_edit_set_notification:
			MetaSettings.putVerseId(context, id);
	    	MainNotification.notify(context).show();
	    	Toast.makeText(context, "Notification Set", Toast.LENGTH_SHORT).show();
	    	returnToDashboard();
	    	return true;
	    case R.id.menu_edit_save_changes:
			db.open();
			db.updateEntry(id, reference.getText().toString(), verse.getText().toString(), null);
			db.close();
			Toast.makeText(context, "Verse Updated", Toast.LENGTH_SHORT).show();
			returnToDashboard();
	    	return true;
	    case R.id.menu_edit_delete:
			db.open();
			db.deleteEntry(id);
			db.close();
			returnToDashboard();
	    	return true;
	    case R.id.menu_edit_change_list:
			db.open();
			if(list.equals("memorized")) {
				db.updateEntry(id, null, null, "current");
			}
			else {
				db.updateEntry(id, null, null, "memorized");
			}
			db.close();
			Toast.makeText(context, "Verse Updated", Toast.LENGTH_SHORT).show();
			returnToDashboard();
	    	return true;
	    case R.id.menu_edit_share:
	    	String shareMessage = reference.getText() + " - " + verse.getText();
	    	Intent intent = new Intent();
	    	intent.setType("text/plain");
	    	intent.setAction(Intent.ACTION_SEND);
	    	intent.putExtra(Intent.EXTRA_SUBJECT, reference.getText().toString());
	    	intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
	    	startActivity(Intent.createChooser(intent, "Share To..."));
	    	returnToDashboard();
	    	return true;
        default:
            return super.onOptionsItemSelected(item);
	    }
	}
	
//Host Activity Interface
//------------------------------------------------------------------------------
	private static onReturnHomeListener listener;
	
	public void returnToDashboard() {
	    if(listener != null){
	        listener.toParent();
	    }
	}

	public interface onReturnHomeListener {
	    void toParent();
    }

	public static void setOnReturnHomeListener(onReturnHomeListener listener) {
	    EditVerseFragment.listener = listener;
	}
}
