package com.caseybrooks.common.app.activity;

import android.support.annotation.NonNull;

import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;

import java.util.ArrayList;
import java.util.List;

public class DrawerFeature implements ParentListItem {
    private final Class< ? extends FeatureConfiguration> featureClass;

    private final String title;
    private final int icon;
    private final int color;
    private final int id;

    private int count;
    private ArrayList<DrawerFeature> children;

    public DrawerFeature(@NonNull Class< ? extends FeatureConfiguration> featureClass, String title, int icon) {
        this(featureClass, title, icon, 0, 0);
    }

    public DrawerFeature(@NonNull Class< ? extends FeatureConfiguration> featureClass, String title, int icon, int id) {
        this(featureClass, title, icon, id, 0);
    }

    public DrawerFeature(@NonNull Class< ? extends FeatureConfiguration> featureClass, String title, int icon, int id, int color) {
        this.featureClass = featureClass;
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

    public Class<? extends FeatureConfiguration> getFeatureClass() {
        return featureClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DrawerFeature that = (DrawerFeature) o;

        if (id != that.id) return false;
        return featureClass.equals(that.featureClass);

    }

    @Override
    public int hashCode() {
        int result = featureClass.hashCode();
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
