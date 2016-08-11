package com.caseybrooks.common.app.dashboard;

import android.content.Context;
import android.support.annotation.NonNull;

import com.caseybrooks.common.widget.CardView;

public abstract class DashboardCardBase extends CardView {
    public DashboardCardBase(Context context) {
        super(context);
    }

    public abstract @NonNull Class<? extends DashboardCardConfiguration> getDashboardCardConfigurationClass();
}
