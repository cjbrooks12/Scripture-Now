package com.caseybrooks.scripturememorybeta;

import android.graphics.Color;
import android.os.Bundle;

import com.caseybrooks.common.app.ActivityBase;
import com.caseybrooks.common.app.AppFeature;
import com.caseybrooks.common.app.AppSettings;
import com.caseybrooks.common.app.ExpandableNavigationView;

import java.util.ArrayList;

public class MainActivity extends ActivityBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public ArrayList<AppFeature> getFeatures() {
        ArrayList<AppFeature> featuresList = new ArrayList<>();
        featuresList.add(AppFeature.Dashboard);
        featuresList.add(AppFeature.Read);
        featuresList.add(AppFeature.Discover);
        featuresList.add(AppFeature.MemorizationState);
        featuresList.add(AppFeature.Tags);
        featuresList.add(AppFeature.Help);
        featuresList.add(AppFeature.Settings);

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
        AppSettings.putSelectedFeature(this, AppFeature.Dashboard, 0);
    }
}
