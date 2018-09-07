package com.caseybrooks.common.features.discover.importverses;

import android.content.Context;

import com.caseybrooks.common.R;
import com.caseyjbrooks.zion.app.activity.DrawerFeature;
import com.caseyjbrooks.zion.app.activity.FeatureConfiguration;
import com.caseyjbrooks.zion.app.fragment.FragmentConfiguration;

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
