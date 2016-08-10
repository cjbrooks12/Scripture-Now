package com.caseybrooks.common.app;

import android.content.Context;

import com.caseybrooks.common.app.features.dashboard.DashboardCardConfiguration;
import com.caseybrooks.common.app.fragment.FragmentConfiguration;
import com.caseybrooks.common.app.notifications.NotificationConfiguration;
import com.caseybrooks.common.app.widgets.WidgetConfiguration;

/**
 * A feature configuration describes an entire app feature. It defines which components a particular
 * feature supports, such as a fragment, a dashboard card, a widget, or a notification, and provides
 * an interface to the configurations of those components. It also describes the metadata of a feature,
 * which is independant of any instance of the feature, such as its title, its decor color, the
 * identifying icon, etc.
 *
 * A feature is a combined set of the following components:
 *      - A fragment.
 *      - A drawer feature, with optional list of children drawer features (i.e. 'Tags' as main features, with individual tags as children).
 *      - A dashboard card.
 *      - A homescreen/lockscreen widget.
 *      - A notification.
 *
 * Creating a new feature involves creating a new FeatureConfiguration and adding it to the AppFeature
 * manifest. This manifest is how the app framework discovers new features and plugs them into the
 * existing framework. The framework will take care of adding the feature to the Navigation Drawer,
 * navigating to the feature fragment when selected, updating homescreen widgets, scheduling notifications,
 * dispatching broadcasts, and much more.
 */
public abstract class FeatureConfiguration {
    public FragmentConfiguration getFragmentConfiguration(Context context) { return null; }
    public WidgetConfiguration getWidgetConfiguration(Context context) { return null; }
    public NotificationConfiguration getNotificationConfiguration(Context context) { return null; }
    public DashboardCardConfiguration getDashboardCardConfiguration(Context context) { return null; }

    public String[] getRequiredPermissions()    { return new String[] {}; }

}
