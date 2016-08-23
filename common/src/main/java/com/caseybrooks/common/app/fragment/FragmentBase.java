package com.caseybrooks.common.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.caseybrooks.common.app.activity.ActivityBase;

public abstract class FragmentBase extends Fragment {

    protected FragmentConfiguration instanceConfiguration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public ActivityBase getActivityBase() {
        return (ActivityBase) super.getActivity();
    }

    public FragmentConfiguration getInstanceConfiguration() {
        return instanceConfiguration;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(!(context instanceof ActivityBase)) {
            throw new ClassCastException("Parent context must be an instance of ActivityBase");
        }
    }

    public abstract @NonNull Class<? extends FragmentConfiguration> getFragmentConfigurationClass();
}
