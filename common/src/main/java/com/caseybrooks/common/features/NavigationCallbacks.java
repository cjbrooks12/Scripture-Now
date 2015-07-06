package com.caseybrooks.common.features;

public interface NavigationCallbacks {
	void setToolBar(String name, int color);
	void toVerseList(int listType, int id);
	void toVerseDetail();

	void toVerseEdit();
	void toDashboard();
	void toTopicalBible();
	void toImportVerses();
	void toSettings();
	void toHelp();

	void toDebugPreferences();
	void toDebugDatabase();
	void toDebugCache();
}
