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
import com.caseybrooks.scripturememory.data.FlowLayout;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.notifications.MainNotification;
import com.caseybrooks.scripturememory.views.TagChip;

public class EditVerseFragment extends Fragment {
//Data Members
//------------------------------------------------------------------------------
	Context context;
	View view;
	VerseDB db;
    Passage passage;
	
	ActionBar ab;
	EditText editRef, editVer;

    FlowLayout tagChipsLayout;

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
        db = new VerseDB(context);
        initialize();
      
        return view;
	}

	private void initialize() {
		long id = getActivity().getIntent().getIntExtra("KEY_ID", 1);

		db.open();
		passage = db.getVerse(id);

        if(passage != null) {
            editRef = (EditText) view.findViewById(R.id.updateReference);
            editRef.setText(passage.getReference());
            editVer = (EditText) view.findViewById(R.id.updateVerse);
            editVer.setText(passage.getText());

            tagChipsLayout = (FlowLayout) view.findViewById(R.id.tagChipLayout);

            String[] tags = passage.getTags();

            for(String tag : tags) {
                TagChip tagChip = new TagChip(context);
                int tagId = (int)db.getTagID(tag);
                tagChip.setMode(0);
                tagChip.setTag(tagId);

                tagChipsLayout.addView(tagChip);
            }

            TagChip tagChip = new TagChip(context);
            tagChip.setMode(2);

            tagChipsLayout.addView(tagChip);
        }

        db.close();


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

        if(passage != null) {
            if (MetaSettings.getVerseId(context) == passage.getId()) {
                menu.removeItem(R.id.menu_edit_delete);
                menu.removeItem(R.id.menu_edit_change_list);
            }
            if (passage.getState() == 4) {
                menu.removeItem(R.id.menu_edit_set_notification);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater = ((ActionBarActivity) context).getMenuInflater();
	    inflater.inflate(R.menu.menu_edit_verse, menu);
        if(passage != null) {
            if (passage.getState() != 5) {
                menu.findItem(R.id.menu_edit_change_list).setTitle("Move to Memorized");
            } else {
                menu.findItem(R.id.menu_edit_change_list).setTitle("Move to Current");
            }
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
            if(passage != null) {
                MetaSettings.putVerseId(context, (int)passage.getId());
                MainNotification.notify(context).show();
                Toast.makeText(context, "Notification Set", Toast.LENGTH_SHORT).show();
            }
	    	returnToDashboard();
	    	return true;
	    case R.id.menu_edit_save_changes:
            if(passage != null) {
                db.open();
                db.updateVerse(passage);
                db.close();
                Toast.makeText(context, "Verse Updated", Toast.LENGTH_SHORT).show();
            }
			returnToDashboard();
	    	return true;
	    case R.id.menu_edit_delete:
            if(passage != null) {
                db.open();
                passage.setState(7);
                db.updateVerse(passage);
                db.close();
            }
			returnToDashboard();
	    	return true;
	    case R.id.menu_edit_change_list:
            if(passage != null) {
                db.open();
                if (passage.getState() != 5) {
                    passage.setState(5);
                    db.updateVerse(passage);
                }
                else {
                    passage.setState(1+(int)(Math.random()*4));
                    db.updateVerse(passage);
                }
                db.close();
                Toast.makeText(context, "Verse Updated", Toast.LENGTH_SHORT).show();
            }
			returnToDashboard();
	    	return true;
	    case R.id.menu_edit_share:
            if(passage != null) {
                String shareMessage = passage.getReference() + " - " + passage.getText();
                Intent intent = new Intent();
                intent.setType("text/plain");
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_SUBJECT, passage.getReference());
                intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(intent, "Share To..."));
            }
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
