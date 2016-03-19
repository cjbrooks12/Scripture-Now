package com.caseybrooks.openbiblebeta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.caseybrooks.common.app.ActivityBase;
import com.caseybrooks.common.app.AppFeature;
import com.caseybrooks.common.app.AppSettings;
import com.caseybrooks.openbiblebeta.gcm.RegistrationIntentService;
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
        super.onFirstInstall();
        AppSettings.putSelectedFeature(this, AppFeature.Search, 0);
    }
}
