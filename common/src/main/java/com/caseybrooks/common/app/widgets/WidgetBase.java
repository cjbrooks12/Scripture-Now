package com.caseybrooks.common.app.widgets;

import android.support.annotation.NonNull;

public abstract class WidgetBase {
    public abstract @NonNull Class<? extends WidgetConfiguration> getWidgetConfigurationClass();

}
