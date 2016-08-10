package com.caseybrooks.common.app.features.dashboard;

import android.content.Context;

import com.caseybrooks.common.widget.CardView;

public class DashboardCardBase extends CardView {

    public DashboardCardBase(Context context) {
        super(context);
    }

    public DashboardCardConfiguration getConfiguration() {
        return null;
    }
}
