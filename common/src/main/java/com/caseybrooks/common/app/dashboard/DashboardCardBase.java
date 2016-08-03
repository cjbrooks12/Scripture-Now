package com.caseybrooks.common.app.dashboard;

import android.content.Context;

import com.caseybrooks.common.widget.CardView;

public class DashboardCardBase extends CardView implements DashboardBaseView {

    public DashboardCardBase(Context context) {
        super(context);
    }

    @Override
    public DashboardFeature getFeatureForView() {
        return null;
    }
}
