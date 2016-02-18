package com.caseybrooks.common.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.util.Pair;

import java.util.Stack;

public class PreferenceFragmentBase extends PreferenceFragmentCompat implements ActivityBaseFragment {
    public String TAG = getClass().getSimpleName();

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
    public Pair<AppFeature, Integer> getFeatureForFragment() {
        return null;
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
        return false;
    }

    @Override
    public boolean usesSearchBox() {
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
