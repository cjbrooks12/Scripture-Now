package com.caseybrooks.scripturememory.misc;

public interface NavigationCallbacks {
	void setToolBar(String name, int color);
	void toVerseList(int listType, int id);
    void toVerseDetail(int id);

    void toVerseEdit(int id);
    void toDashboard();
    void toTopicalBible();
    void toImportVerses();
    void toSettings();
    void toHelp();
}
