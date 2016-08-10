package com.caseybrooks.common.app.features.dashboard;

public class DashboardCardConfiguration {

    public String getTitle() {
        return "";
    }

    public Class<? extends DashboardCardBase> getCardClass() {
        return null;
    }

    public int getRelativePosition() {
        return 0;
    }

    public int getViewType() {
        return hashCode();
    }
}
