package com.caseybrooks.common.app.fragment;

import com.caseybrooks.common.app.FeatureConfiguration;
import com.caseybrooks.common.app.activity.DrawerFeature;

public class FragmentConfiguration {

    public Class<? extends FeatureConfiguration> getFeatureConfigurationClass() { return null; }

    public Class<? extends FragmentBase> getFragmentClass() {
        return null;
    }

    public boolean shouldAddToBackstack() {
        return true;
    }

    public boolean isTopLevel() {
        return true;
    }

    public DrawerFeature getDrawerFeature() {
        return null;
    }

    public int getDecorColor() {
        return 0;
    }

    public String getTitle() {
        return "";
    }

    public boolean usesFAB() {
        return false;
    }

    public int getFABIcon() {
        return 0;
    }

    public boolean usesSearchbox() {
        return false;
    }

    public int getMenuResource() {
        return 0;
    }

    public int getSearchboxMenuResource() {
        return 0;
    }

    public String getSearchboxHint() {
        return "";
    }

}
