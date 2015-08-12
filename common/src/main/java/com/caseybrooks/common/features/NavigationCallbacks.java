package com.caseybrooks.common.features;

import android.support.v7.widget.Toolbar;
import android.view.View;

public interface NavigationCallbacks {
	void setToolBar(String name, int color);
	Toolbar getToolbar();
	void expandToolbarWIthView(View view);
	void collapseExpandedToolbar();



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

	void toBible(int id);
}
