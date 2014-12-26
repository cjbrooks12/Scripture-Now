package com.caseybrooks.scripturememory.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.fragments.EditVerseFragment;
import com.caseybrooks.scripturememory.fragments.NavigationDrawerFragment;
import com.caseybrooks.scripturememory.misc.NavigationCallbacks;

public class DetailActivity extends ActionBarActivity implements NavigationCallbacks {
    Context context;
    Toolbar tb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;

        int theme = MetaSettings.getAppTheme(context);
        if(theme == 0) setTheme(R.style.Theme_BaseLight);
        else setTheme(R.style.Theme_BaseDark);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Set up the drawer.
        tb = (Toolbar) findViewById(R.id.activity_toolbar);
        setSupportActionBar(tb);

        int id = getIntent().getExtras().getInt("KEY_ID", 0);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.mainFragmentContainer, EditVerseFragment.newInstance(id))
                .commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        if(id == android.R.id.home) {
//            finish();
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNavigationDrawerItemSelected(NavigationDrawerFragment.NavListItem item) {

    }

    @Override
    public void toVerseList(int listType, int id) {

    }

    @Override
    public void toVerseDetail(int id) {

    }

    @Override
    public void toVerseEdit(int id) {

    }

    @Override
    public void toDashboard() {

    }

    @Override
    public void toDiscover() {

    }

    @Override
    public void toSettings() {

    }

    @Override
    public void toHelp() {

    }
}
