package com.caseybrooks.common.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.view.MenuItem;

import com.caseybrooks.common.app.activity.ActivityBase;

import java.util.Stack;

public abstract class PreferenceFragmentBase extends PreferenceFragmentCompat implements ActivityBaseFragment {
    protected Stack<PreferenceScreen> settingsScreens;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        settingsScreens = new Stack<>();
    }

    @Override
    public void onNavigateToScreen(PreferenceScreen preferenceScreen) {
        setPreferenceScreen(preferenceScreen);
        settingsScreens.push(preferenceScreen);

        getActivityBase().setDrawerEnabled(settingsScreens.size() < 2);
    }

    private boolean popPreferenceScreen() {
        if(settingsScreens.size() < 2) {
            getActivityBase().setDrawerEnabled(true);
            return false;
        }
        else {
            getActivityBase().setDrawerEnabled(!(settingsScreens.size() > 2));

            settingsScreens.pop();
            setPreferenceScreen(settingsScreens.peek());
            return true;
        }
    }

//ActivityBaseFragment Implementation
//--------------------------------------------------------------------------------------------------
    @Override
    public ActivityBase getActivityBase() {
        return (ActivityBase) super.getActivity();
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
    public boolean onBackButtonPressed() {
        return popPreferenceScreen();
    }

    @Override
    public boolean onBackArrowPressed() {
        return popPreferenceScreen();
    }

    @Override
    public boolean onFABPressed() {
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(!(context instanceof ActivityBase)) {
            throw new ClassCastException("Parent context must be an instance of ActivityBase");
        }
    }
}
