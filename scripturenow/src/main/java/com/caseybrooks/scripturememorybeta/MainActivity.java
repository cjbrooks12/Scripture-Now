package com.caseybrooks.scripturememorybeta;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.caseybrooks.common.app.ActivityBase;
import com.caseybrooks.common.app.AppFeature;
import com.caseybrooks.common.app.AppSettings;
import com.caseybrooks.common.app.DashboardFeature;
import com.caseybrooks.common.app.ExpandableNavigationView;
import com.caseybrooks.scripturememorybeta.gcm.RegistrationIntentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;

public class MainActivity extends ActivityBase {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(checkPlayServices()) {
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS) {
            if(apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            }
            else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public ArrayList<AppFeature> getAppFeatures() {
        ArrayList<AppFeature> featuresList = super.getAppFeatures();
        featuresList.add(AppFeature.Dashboard);
        featuresList.add(AppFeature.Read);
        featuresList.add(AppFeature.Discover);
        featuresList.add(AppFeature.MemorizationState);
        featuresList.add(AppFeature.Tags);
        featuresList.add(AppFeature.Prayers);
        featuresList.add(AppFeature.Help);
        featuresList.add(AppFeature.Settings);

        return featuresList;
    }

    @Override
    public ArrayList<DashboardFeature> getDashboardFeatures() {
        ArrayList<DashboardFeature> featuresList = super.getDashboardFeatures();
        featuresList.add(DashboardFeature.NotificationVerse);
        featuresList.add(DashboardFeature.VerseOfTheDay);
        featuresList.add(DashboardFeature.JoshuaProjectPrayer);
//        featuresList.add(DashboardFeature.AddVerse);
//        featuresList.add(DashboardFeature.AddPrayer);
//        featuresList.add(DashboardFeature.Changelog);

        return featuresList;
    }

    @Override
    public ArrayList<ExpandableNavigationView.NavChildItem> getChildrenForFeature(AppFeature feature) {
        if(feature == AppFeature.MemorizationState) {
            ArrayList<ExpandableNavigationView.NavChildItem> children = new ArrayList<>();
            for(int i = 0; i < 5; i++) {
                ExpandableNavigationView.NavChildItem item = new ExpandableNavigationView.NavChildItem();
                item.subitemText = "Memorization State Child " + i;
                item.subitemCount = i;
                item.subitemIconColor = Color.BLUE;
                item.appFeature = feature;
                item.appFeatureId = i;

                children.add(item);
            }
            return children;
        }
        else if(feature == AppFeature.Tags){
            ArrayList<ExpandableNavigationView.NavChildItem> children = new ArrayList<>();
            for(int i = 0; i < 5; i++) {
                ExpandableNavigationView.NavChildItem item = new ExpandableNavigationView.NavChildItem();
                item.subitemText = "Tag Child " + i;
                item.subitemCount = i;
                item.subitemIconColor = Color.WHITE;
                item.appFeature = feature;
                item.appFeatureId = i;

                children.add(item);
            }
            return children;
        }
        else if(feature == AppFeature.Read) {
            ArrayList<ExpandableNavigationView.NavChildItem> children = new ArrayList<>();
            for(int i = 0; i < 5; i++) {
                ExpandableNavigationView.NavChildItem item = new ExpandableNavigationView.NavChildItem();
                item.subitemText = "Bookmark Child " + i;
                item.subitemCount = 0;
                item.subitemIconColor = Color.GREEN;
                item.appFeature = AppFeature.Read;
                item.subitemIcon = R.drawable.ic_bookmark;
                item.appFeatureId = i;

                children.add(item);
            }
            return children;
        }

        return super.getChildrenForFeature(feature);
    }

    @Override
    public boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    @Override
    public void onFirstInstall() {
        super.onFirstInstall();
        AppSettings.putSelectedFeature(this, AppFeature.Dashboard, 0);
    }
}