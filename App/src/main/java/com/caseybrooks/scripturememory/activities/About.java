package com.caseybrooks.scripturememory.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.LinearLayout;

import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;

public class About extends ActionBarActivity {
    Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        context = this;

        int theme = MetaSettings.getAppTheme(context);
        if(theme == 0) setTheme(R.style.Theme_BaseLight);
        else setTheme(R.style.Theme_BaseDark);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
	}
}
