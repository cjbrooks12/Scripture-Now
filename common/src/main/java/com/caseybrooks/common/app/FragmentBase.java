package com.caseybrooks.common.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


import com.caseybrooks.common.R;
import com.caseybrooks.common.widget.SearchBox;

public class FragmentBase extends Fragment implements ActivityBaseFragment, SearchBox.SearchBoxListener {
    public String TAG = getClass().getSimpleName();

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
    public Pair<AppFeature, Integer> getFeatureForFragment() {
        return null;
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
    public boolean onNetworkConnected() {
        return false;
    }

    @Override
    public boolean onNetworkDisconnected() {
        return false;
    }

    @Override
    public boolean usesSearchBox() {
        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(!(context instanceof ActivityBase)) {
            throw new ClassCastException("Parent context must be an instance of ActivityBase");
        }
    }

    @Override
    public boolean shouldAddToBackStack() {
        return true;
    }

//Make fragments easily searchable
//--------------------------------------------------------------------------------------------------

    public String getSearchHint() {
        return "";
    }

    public int getSearchMenuResourceId() {
        return 0;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(usesSearchBox()) {
            inflater.inflate(R.menu.menu_searchable, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.search) {
            if(getActivityBase().getSearchbox().isRevealed())
                getActivityBase().closeSearch();
            else
                getActivityBase().openSearch(getSearchHint(), getSearchMenuResourceId(), this);

            return true;
        }

        return false;
    }

    @Override
    public boolean onSearch(String query) {
        return false;
    }

    @Override
    public void onQueryChanged(String query) {

    }

    @Override
    public boolean onSearchMenuItemSelected(MenuItem selectedItem) {
        return false;
    }

//Logging
//--------------------------------------------------------------------------------------------------
    public void LogI(String message, Object... params) {
        Log.i(TAG, Util.formatString(message, params));
    }

    public void LogE(String message, Object... params) {
        Log.e(TAG, Util.formatString(message, params));
    }
}
