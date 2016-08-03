package com.caseybrooks.common.app;

import com.caseybrooks.common.app.fragment.ActivityBaseFragment;
import com.caseybrooks.common.app.fragment.AppFeature;
import com.caseybrooks.common.app.notifications.NotificationBase;
import com.caseybrooks.common.app.widgets.WidgetBase;

/**
 * A feature configuration describes an entire app feature. It defines which components a particular
 * feature supports, such as a fragment, a dashboard card, a widget, or a notification, and provides
 * an interface to the configurations of those components. It also describes the metadata of a feature,
 * which is independant of any instance of the feature, such as its title, its decor color, the
 * identifying icon, etc.
 */
public class FeatureConfiguration {
    public AppFeature getAppFeature()   { return null; }

    public Class<? extends ActivityBaseFragment> getFragmentClass() { return null; }
    public Class<? extends WidgetBase> getWidgetClass() { return null; }
    public Class<? extends NotificationBase> getNotificationClass() { return null; }
    public Class<? extends NotificationBase> getDashboardCardClass() { return null; }

    public

    public long getFeatureInstanceId()          { return 0; }

    public String getTitle()                    { return ""; }
    public int getDecorColor()                  { return 0; }
    public boolean shouldAddToBackStack()       { return true; }
    public int getMenuResource()                { return 0; }
    public int getContextualMenuResource()      { return 0; }
    public boolean isTopLevel()                 { return true; }

    public boolean usesSearchbox()              { return false; }
    public int getSearchboxMenuResource()       { return 0; }
    public String getSearchboxHint()            { return ""; }

    public boolean usesFAB()                    { return false; }
    public int getFABColor()                    { return 0; }
    public int getFABIcon()                     { return 0; }
    public int getFABIconColor()                { return 0; }

    public String[] getRequiredPermissions()    { return new String[] {}; }
}
