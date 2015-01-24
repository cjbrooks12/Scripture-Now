package com.caseybrooks.scripturememory.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.fragments.EditVerseFragment;
import com.caseybrooks.scripturememory.fragments.HelpFragment;
import com.caseybrooks.scripturememory.fragments.ImportVersesFragment;
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

        Fragment fragment = ImportVersesFragment.newInstance();

        if(getIntent().getExtras() != null) {
            int id = getIntent().getExtras().getInt("KEY_ID", 0);
            int listType = getIntent().getExtras().getInt("KEY_LIST_TYPE", 0);
            int listId = getIntent().getExtras().getInt("KEY_LIST_ID", 0);

            if (getIntent().getExtras().getInt("FRAGMENT", -1) == 0) { //edit a verse
                fragment = EditVerseFragment.newInstance(id, listType, listId);
            }
        }
        else if (getIntent().getDataString() != null) { //help topic: overview
            int item = Integer.parseInt(getIntent().getDataString());

            if (item == 0) fragment = ImportVersesFragment.newInstance();

            else if (item == 1) fragment = HelpFragment.ViewTopicFragment.newInstance(R.layout.help_overview);
            else if (item == 2) fragment = HelpFragment.ViewTopicFragment.newInstance(R.layout.help_adding_verses);
            else if (item == 3) fragment = HelpFragment.ViewTopicFragment.newInstance(R.layout.help_memorization_state);
            else if (item == 4) fragment = HelpFragment.ViewTopicFragment.newInstance(R.layout.help_tags);

            else if (item == 5) fragment = HelpFragment.ViewTopicFragment.newInstance(R.layout.help_changelog);
            else if (item == 6) fragment = HelpFragment.ViewTopicFragment.newInstance(R.layout.help_licenses);

        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.mainFragmentContainer, fragment)
                .commit();
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
    public void toTopicalBible() {

    }

    @Override
    public void toImportVerses() {

    }

    @Override
    public void toSettings() {

    }

    @Override
    public void toHelp() {

    }
}