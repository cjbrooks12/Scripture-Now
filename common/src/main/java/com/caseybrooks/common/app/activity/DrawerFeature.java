package com.caseybrooks.common.app.activity;

import android.support.annotation.NonNull;

import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.caseybrooks.common.app.FeatureConfiguration;

import java.util.ArrayList;
import java.util.List;

public class DrawerFeature implements ParentListItem {
    private final FeatureConfiguration feature;

    private final String title;
    private final int icon;
    private final int color;
    private final int id;

    private int count;
    private ArrayList<DrawerFeature> children;

    public DrawerFeature(@NonNull FeatureConfiguration feature, String title, int icon) {
        this(feature, title, icon, 0, 0);
    }

    public DrawerFeature(@NonNull FeatureConfiguration feature, String title, int icon, int id) {
        this(feature, title, icon, id, 0);
    }

    public DrawerFeature(@NonNull FeatureConfiguration feature, String title, int icon, int id, int color) {
        this.feature = feature;
        this.title = title;
        this.icon = icon;
        this.id = id;
        this.color = color;
    }

    public String getTitle() {
        return title;
    }

    public int getIcon() {
        return icon;
    }

    public int getColor() {
        return color;
    }

    public int getId() {
        return id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setChildren(ArrayList<DrawerFeature> children) {
        this.children = children;
    }

    public FeatureConfiguration getFeatureConfiguration() {
        return feature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DrawerFeature that = (DrawerFeature) o;

        if (id != that.id) return false;
        return feature.equals(that.feature);

    }

    @Override
    public int hashCode() {
        int result = feature.hashCode();
        result = 31 * result + id;
        return result;
    }

    @Override
    public List<DrawerFeature> getChildItemList() {
        return children;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
