package com.caseybrooks.common.features.discover.importverses;

import android.content.Context;

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.activity.DrawerFeature;
import com.caseybrooks.common.app.activity.FeatureConfiguration;
import com.caseybrooks.common.app.fragment.FragmentConfiguration;

public class ImportVersesConfiguration extends FeatureConfiguration {

    public ImportVersesConfiguration(Context context) {
        super(context);
    }

    @Override
    public DrawerFeature getDrawerFeature() {
        return new DrawerFeature(ImportVersesConfiguration.class, "Import Verses", R.drawable.ic_import);
    }

    @Override
    public Class<? extends FragmentConfiguration> getFragmentConfigurationClass() {
        return ImportVersesFragment.ImportVersesFragmentConfiguration.class;
    }

}
