package com.caseybrooks.common.features.dashboard;

import com.caseybrooks.common.features.dashboard.cards.AddPrayerCard;
import com.caseybrooks.common.features.dashboard.cards.AddVerseCard;
import com.caseybrooks.common.features.dashboard.cards.ChangelogCard;
import com.caseybrooks.common.features.dashboard.cards.JoshuaProjectCard;
import com.caseybrooks.common.features.dashboard.cards.NotificationVerseCard;
import com.caseybrooks.common.features.dashboard.cards.PrayerOfTheDayCard;
import com.caseybrooks.common.features.dashboard.cards.SearchResultCard;
import com.caseybrooks.common.features.dashboard.cards.UpdateAvailableCard;
import com.caseybrooks.common.features.dashboard.cards.VerseOfTheDayCard;

/**
 * Enums representing the various cards available on the app's dashboard.
 *
 * Cards on the dashboard are contextual, representing the current state of the app and the upcoming
 * actions for the user to take notice of, as well as a quick way to add new content of many
 * kinds in one common place, removing the requirement for a user to navigate to a certain screen
 * just to add a new item of that kind.
 */
public enum DashboardFeature {
    //Enum              Name                    card class position, menu resource,     id
    VerseOfTheDay(      "Verse Of The Day",     VerseOfTheDayCard.class,      1000,     1),
    PrayerOfTheDay(     "Prayer Of The Day",    PrayerOfTheDayCard.class,     1000,     2),
    JoshuaProjectPrayer("Joshua Project",       JoshuaProjectCard.class,      1000,     3),

    VerseSearchResult(  "Search Result",        SearchResultCard.class,       1,        4),
    AddVerse(           "Add Verse",            AddVerseCard.class,           1000,     5),
    AddPrayer(          "Add Prayer",           AddPrayerCard.class,          1000,     6),

    NotificationVerse(  "Notification Verse",   NotificationVerseCard.class,  0,        7),

    UpdateAvailable(    "Update Available",     UpdateAvailableCard.class,    1000,     8),
    Changelog(          "Changelog",            ChangelogCard.class,          1,        9);

    private final String title;
    private final Class<? extends DashboardCardBase> viewClass;
    private final int position;
    private final int id;

    DashboardFeature(String title, Class<? extends DashboardCardBase> viewClass, int position, int id) {
        this.title = title;
        this.viewClass = viewClass;
        this.position = position;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public Class<? extends DashboardCardBase> getViewClass() {
        return viewClass;
    }

    public int getPosition() {
        return position;
    }

    public int getId() {
        return id;
    }

    public static DashboardFeature getFeatureForId(int id) {
        for(DashboardFeature feature : DashboardFeature.values()) {
            if(id == feature.getId())
                return feature;
        }
        return null;
    }
}
