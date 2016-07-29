package com.caseybrooks.common.app;

import com.caseybrooks.common.features.prayers.AddPrayerCard;
import com.caseybrooks.common.features.verses.AddVerseCard;
import com.caseybrooks.common.features.help.ChangelogCard;
import com.caseybrooks.common.features.joshuaproject.JoshuaProjectCard;
import com.caseybrooks.common.features.memoryverse.MemoryVerseCard;
import com.caseybrooks.common.features.prayers.PrayerOfTheDayCard;
import com.caseybrooks.common.features.verses.SearchResultCard;
import com.caseybrooks.common.features.help.UpdateAvailableCard;
import com.caseybrooks.common.features.votd.VerseOfTheDayCard;

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
    VerseOfTheDay(      "Verse Of The Day",     VerseOfTheDayCard.class,      2,        1),
    PrayerOfTheDay(     "Prayer Of The Day",    PrayerOfTheDayCard.class,     2,        2),
    JoshuaProjectPrayer("Joshua Project",       JoshuaProjectCard.class,      1,        3),

    VerseSearchResult(  "Search Result",        SearchResultCard.class,       1,        4),
    AddVerse(           "Add Verse",            AddVerseCard.class,           1,        5),
    AddPrayer(          "Add Prayer",           AddPrayerCard.class,          1,        6),

    NotificationVerse(  "Notification Verse",   MemoryVerseCard.class,  0,        7),

    UpdateAvailable(    "Update Available",     UpdateAvailableCard.class,    1,        8),
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
