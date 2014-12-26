package com.caseybrooks.scripturememory.misc;

import com.caseybrooks.scripturememory.fragments.NavigationDrawerFragment;

public interface NavigationCallbacks {
    void onNavigationDrawerItemSelected(NavigationDrawerFragment.NavListItem item);
    void toVerseList(int listType, int id);
    void toVerseDetail(int id);
    void toVerseEdit(int id);
    void toDashboard();
    void toDiscover();
    void toSettings();
    void toHelp();
}
