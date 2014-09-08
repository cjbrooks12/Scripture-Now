package com.caseybrooks.scripturememory.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;

import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.fragments.EditVerseFragment;
import com.caseybrooks.scripturememory.fragments.EditVerseFragment.onReturnHomeListener;
import com.caseybrooks.scripturememory.data.MetaSettings;

public class EditVerse extends ActionBarActivity implements onReturnHomeListener {	
//Data Members
//------------------------------------------------------------------------------
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
		setContentView(R.layout.activity_edit_verse);
		Fragment edit = new EditVerseFragment();
		FragmentManager fragmentManager = getSupportFragmentManager();
	    fragmentManager.beginTransaction()
	                   .add(R.id.editFragmentContainer, edit)
	                   .commit();		
	    EditVerseFragment.setOnReturnHomeListener(this);
	}
	
	@Override
	public void toParent() {
		finish();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	    	finish();
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	    }
	    return super.onKeyDown(keyCode, event);
	}
}