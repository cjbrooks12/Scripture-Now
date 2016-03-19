package com.caseybrooks.openbiblebeta;

import android.os.Bundle;

import com.caseybrooks.common.app.ActivityBase;
import com.caseybrooks.common.app.AppFeature;
import com.caseybrooks.common.app.AppSettings;

import java.util.ArrayList;

public class MainActivity extends ActivityBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public ArrayList<AppFeature> getFeatures() {
        ArrayList<AppFeature> featuresList = new ArrayList<>();
        featuresList.add(AppFeature.Search);
        featuresList.add(AppFeature.Topics);
        featuresList.add(AppFeature.Help);
        featuresList.add(AppFeature.Settings);

        return featuresList;
    }

    @Override
    public boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    @Override
    public void onFirstInstall() {
        AppSettings.putSelectedFeature(this, AppFeature.Search, 0);
    }
}
