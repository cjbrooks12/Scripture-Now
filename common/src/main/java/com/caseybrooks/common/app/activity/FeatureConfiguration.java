package com.caseybrooks.common.app.activity;

import android.content.Context;

import com.caseybrooks.common.app.dashboard.DashboardCardConfiguration;
import com.caseybrooks.common.app.fragment.FragmentConfiguration;
import com.caseybrooks.common.app.notifications.NotificationConfiguration;
import com.caseybrooks.common.app.widgets.WidgetConfiguration;

/**
 * A feature is a combined set of the following components:
 *      - A fragment.
 *      - A drawer feature, with optional list of children drawer features (i.e. 'Tags' as main features, with individual tags as children).
 *      - A dashboard card.
 *      - A homescreen/lockscreen widget.
 *      - A notification.
 *
 * A feature configuration is essentially a manifest for an app feature. It contains the most basic
 * information necessary to bootstrap that feature. By providing only the class of the respective
 * configuration objects, each component can be loaded lazily.
 *
 * Creating a new feature involves creating a new FeatureConfiguration and adding it to the FeatureProvider
 * manifest. This manifest is a global manifest that takes care of instantiating new components when
 * necessary, as well as discovering new features and plugs them into the
 * existing framework. The framework will take care of adding the feature to the Navigation Drawer,
 * navigating to the feature fragment when selected, updating homescreen widgets, scheduling notifications,
 * dispatching broadcasts, and much more.
 */
public abstract class FeatureConfiguration {
    Context context;

    public FeatureConfiguration(Context context) {
        this.context = context.getApplicationContext();
    }

    public String[] getRequiredPermissions()    { return new String[] {}; }

    public Class<? extends FragmentConfiguration> getFragmentConfigurationClass() { return null; }
    public Class<? extends DashboardCardConfiguration> getDashboardCardConfigurationClass() { return null; }
    public Class<? extends NotificationConfiguration> getNotificationConfigurationClass() { return null; }
    public Class<? extends WidgetConfiguration> getWidgetConfigurationClass() { return null; }

    public DrawerFeature getDrawerFeature() { return null; }

    protected Context getContext() {
        return context;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
