package com.caseybrooks.common.features.biblereader;

import android.content.Context;
import android.graphics.Color;

import com.caseybrooks.common.R;
import com.caseyjbrooks.zion.app.activity.DrawerFeature;
import com.caseyjbrooks.zion.app.activity.FeatureConfiguration;
import com.caseyjbrooks.zion.app.fragment.FragmentConfiguration;

import java.util.ArrayList;

public class BibleReaderConfiguration extends FeatureConfiguration {

    public BibleReaderConfiguration(Context context) {
        super(context);
    }

    @Override
    public DrawerFeature getDrawerFeature() {
        DrawerFeature feature = new DrawerFeature(BibleReaderConfiguration.class, "Read", R.drawable.ic_book_open);

        ArrayList<DrawerFeature> children = new ArrayList<>();
        children.add(new DrawerFeature(BibleReaderConfiguration.class, "New Testament", R.drawable.ic_bookmark, 1, Color.parseColor("#532F63")));
        children.add(new DrawerFeature(BibleReaderConfiguration.class, "Old Testament", R.drawable.ic_bookmark, 2, Color.parseColor("#8F1518")));
        children.add(new DrawerFeature(BibleReaderConfiguration.class, "Church", R.drawable.ic_bookmark, 3, Color.parseColor("#4F56A3")));

        feature.setChildren(children);

        return feature;
    }

    @Override
    public Class<? extends FragmentConfiguration> getFragmentConfigurationClass() {
        return BibleReaderFragment.BibleReaderFragmentConfiguration.class;
    }
}
