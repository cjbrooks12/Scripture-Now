package com.caseybrooks.common.features.feature2;

import android.content.Context;
import android.graphics.Color;

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.activity.DrawerFeature;
import com.caseybrooks.common.app.activity.FeatureConfiguration;
import com.caseybrooks.common.app.fragment.FragmentConfiguration;

import java.util.ArrayList;

public class FeatureTwoConfiguration extends FeatureConfiguration {

    public FeatureTwoConfiguration(Context context) {
        super(context);
    }

    @Override
    public DrawerFeature getDrawerFeature() {
        DrawerFeature feature = new DrawerFeature(FeatureTwoConfiguration.class, "Feature Two", R.drawable.ic_chevron_right);

        ArrayList<DrawerFeature> children = new ArrayList<>();
        children.add(new DrawerFeature(FeatureTwoConfiguration.class, "Child One", R.drawable.ic_chevron_up, 1, Color.BLACK));
        children.add(new DrawerFeature(FeatureTwoConfiguration.class, "Child Two", R.drawable.ic_chevron_right, 2, Color.RED));
        children.add(new DrawerFeature(FeatureTwoConfiguration.class, "Child Three", R.drawable.ic_chevron_down, 3, Color.GREEN));
        children.add(new DrawerFeature(FeatureTwoConfiguration.class, "Child Four", R.drawable.ic_chevron_left, 4, Color.BLUE));

        feature.setChildren(children);

        return feature;
    }

    @Override
    public Class<? extends FragmentConfiguration> getFragmentConfigurationClass() {
        return FeatureTwoFragment.FeatureTwoFragmentConfiguration.class;
    }
}
