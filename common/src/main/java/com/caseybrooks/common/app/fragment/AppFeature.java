package com.caseybrooks.common.app.fragment;


import com.caseybrooks.common.app.FeatureConfiguration;
import com.caseybrooks.common.app.dashboard.DashboardFragment;

/**
 * Enums representing the various screens within the apps.
 *
 * There are three main "types" of screens:
 *
 * TOP LEVEL
 * A top-level item is shown as a parent in the navigation drawer, and represents a View that is highly
 * important or is a category of other important items. Top-Level icons each have an icon, but that
 * icon's color cannot be changed. The id of a top-level item goes from 1-100
 *
 * TOP LEVEL SUBITEM
 * Many top-level elements represent a group of dynamically-created subitems (like tags in
 * Scripture Now!), or a small set of highly-related Top-Level views that fit better when grouped
 * than when listed individually as top-level (like all the Debug screens). Subitems may declare a
 * specific icon to show in the sublist, or may just use a default circle icon. Subitems cannot
 * be declaratively shown, and must be created as the parent Top-Level item requests children.
 * Generally speaking, if there is a static set of pages to list together in a subitem list, declare
 * that in ActivityBase, otherwise let the MainActivity of each app pull the necessary data to
 * populate the lists dynamically. Subitems cannot have a list of subitems themselves. The icon
 * color can be set for subitems, unlike the parent icons. The id of a top-level subitem goes from 101-1000.
 *
 * INNER LEVEL
 * Inner-level items are not shown in the Navigation Drawer, do not have an associated icon, and
 * represent a deeper level to the app's navigation hierarchy. Any top-level screen will still have
 * the naviagation drawer useable, but inner-level screens disable the use of the drawer and replace
 * the "hamburger" icon with a back arrow, indicating the deeper level within the app. The id of an
 * inner item goes from 1001-10000
 *
 * Final note: id 0 is reserved to be used as a marker for convenience in saving the preference of
 * which screen to show by default when first opening the app. Since a valid id opens that screen,
 * an id of 0 will tell the app to open the last top-level screen that was viewed.
 */
// TODO: make this only define features with their fragment class and ID
public enum AppFeature {

    //Top-level features
    Dashboard(          1,      DashboardFragment.getConfiguration()),
    Read(               2,      DashboardFragment.getConfiguration()),
    Discover(           4,      DashboardFragment.getConfiguration()),
    MemorizationState(  7,      DashboardFragment.getConfiguration()),
    Tags(               8,      DashboardFragment.getConfiguration()),
    Help(               9,      DashboardFragment.getConfiguration()),
    Settings(           10,     DashboardFragment.getConfiguration()),
    Prayers(            11,     DashboardFragment.getConfiguration()),
    Debug(              12,     DashboardFragment.getConfiguration()),

    //Some features are top-level but as subitems of another. Functionally the same as top-level, but can't have children
    DebugDatabase(      101,    DashboardFragment.getConfiguration()),
    DebugPreferences(   102,    DashboardFragment.getConfiguration()),
    DebugCache(         103,    DashboardFragment.getConfiguration()),
    TopicalBible(       104,    DashboardFragment.getConfiguration()),
    TopicsList(         105,    DashboardFragment.getConfiguration()),
    ImportVerses(       106,    DashboardFragment.getConfiguration()),

    //inner features that are managed in the same way but should never show up in the main drawer
    Edit(               1001,   DashboardFragment.getConfiguration()),
    Practice(           1002,   DashboardFragment.getConfiguration()),
    Flashcards(         1003,   DashboardFragment.getConfiguration()),

    //Not actually a true "feature" but a convenience marker for settings
    LastVisited(        0,      null);

    private final int id;
    private final FeatureConfiguration configuration;

    AppFeature(int id, FeatureConfiguration configuration) {
        this.id = id;
        this.configuration = configuration;
    }

    public int getId() {
        return id;
    }

    public FeatureConfiguration getConfiguration() {
        return configuration;
    }

    public static AppFeature getFeatureForId(int id) {
        for(AppFeature feature : AppFeature.values()) {
            if(id == feature.getId())
                return feature;
        }
        return null;
    }
}
