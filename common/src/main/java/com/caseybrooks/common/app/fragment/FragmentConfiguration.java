package com.caseybrooks.common.app.fragment;

import android.content.Context;
import android.support.annotation.NonNull;

import com.caseybrooks.common.app.activity.FeatureConfiguration;

public abstract class FragmentConfiguration {
    private Context context;

    private int decorColor;
    private long id;
    private int icon;
    private String title;
    private String subtitle;

    int menuResourceId;

    public FragmentConfiguration(Context context) {
        this.context = context.getApplicationContext();
    }

    public abstract @NonNull Class<? extends FeatureConfiguration> getFeatureConfigurationClass();
    public abstract @NonNull Class<? extends FragmentBase> getFragmentClass();

    public int getDecorColor() {
        return decorColor;
    }

    public void setDecorColor(int decorColor) {
        this.decorColor = decorColor;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public int getMenuResourceId() {
        return menuResourceId;
    }

    public void setMenuResourceId(int menuResourceId) {
        this.menuResourceId = menuResourceId;
    }
}
