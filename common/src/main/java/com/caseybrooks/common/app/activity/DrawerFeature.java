package com.caseybrooks.common.app.activity;

import java.util.ArrayList;

public class DrawerFeature {
    private String title;
    private int icon;
    private int color;
    private ArrayList<DrawerFeature> children;
    private int id;

    public String getTitle() {
        return title;
    }

    public int getIcon() {
        return icon;
    }

    public int getColor() {
        return color;
    }

    public ArrayList<DrawerFeature> getChildren() {
        return children;
    }
}
