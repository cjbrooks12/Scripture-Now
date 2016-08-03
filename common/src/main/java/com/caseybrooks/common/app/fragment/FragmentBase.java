package com.caseybrooks.common.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.activity.ActivityBase;

public abstract class FragmentBase extends Fragment implements ActivityBaseFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public ActivityBase getActivityBase() {
        return (ActivityBase) super.getActivity();
    }

    @Override
    public boolean onBackButtonPressed() {
        return false;
    }

    @Override
    public boolean onBackArrowPressed() {
        return false;
    }

    @Override
    public boolean onFABPressed() {
        return false;
    }

    @Override
    public boolean onNetworkConnected() {
        return false;
    }

    @Override
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
        if(getInstanceConfiguration().usesSearchbox()) {
            inflater.inflate(R.menu.menu_searchable, menu);
        }

        inflater.inflate(getInstanceConfiguration().getMenuResource(), menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.search) {
            if(getActivityBase().getSearchbox().isRevealed())
                getActivityBase().closeSearch();
            else
                getActivityBase().openSearch(getInstanceConfiguration().getSearchboxHint(), getInstanceConfiguration().getSearchboxMenuResource());

            return true;
        }

        return false;
    }

    @Override
    public boolean onSearchSubmitted(String query) {
        return false;
    }

    @Override
    public void onQueryChanged(String query) {

    }

    @Override
    public boolean onSearchMenuItemSelected(MenuItem selectedItem) {
        return false;
    }
}
