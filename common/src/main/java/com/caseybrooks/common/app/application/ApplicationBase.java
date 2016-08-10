package com.caseybrooks.common.app.application;

import android.app.Application;

import com.caseybrooks.common.app.activity.ActivityBase;

public class ApplicationBase extends Application {

    public Class<? extends ActivityBase> getActivityClass() {
        return ActivityBase.class;
    }


}
