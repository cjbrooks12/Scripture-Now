package com.caseybrooks.common.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.activity.ActivityBase;

public class FragmentBase extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public ActivityBase getActivityBase() {
        return (ActivityBase) super.getActivity();
    }

    public boolean onBackButtonPressed() {
        return false;
    }

    public boolean onBackArrowPressed() {
        return false;
    }

    public boolean onFABPressed() {
        return false;
    }

    public boolean onNetworkConnected() {
        return false;
    }

    public boolean onNetworkDisconnected() {
        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(!(context instanceof ActivityBase)) {
            throw new ClassCastException("Parent context must be an instance of ActivityBase");
        }
    }

//Make fragments easily searchable
//--------------------------------------------------------------------------------------------------

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(getConfiguration().usesSearchbox()) {
            inflater.inflate(R.menu.menu_searchable, menu);
        }

        if(getConfiguration().getMenuResource() != 0) {
            inflater.inflate(getConfiguration().getMenuResource(), menu);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.search) {
            if(getActivityBase().getSearchbox().isRevealed())
                getActivityBase().closeSearch();
            else
                getActivityBase().openSearch(getConfiguration().getSearchboxHint(), getConfiguration().getSearchboxMenuResource());

            return true;
        }

        return false;
    }

    public boolean onSearchSubmitted(String query) {
        return false;
    }

    public void onQueryChanged(String query) {

    }

    public boolean onSearchMenuItemSelected(MenuItem selectedItem) {
        return false;
    }

    public FragmentConfiguration getConfiguration() {
        return null;
    }
}
