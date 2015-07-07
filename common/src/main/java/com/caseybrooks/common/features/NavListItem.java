package com.caseybrooks.common.features;

public class NavListItem {
	public enum Type {
		Dashboard,
		TopicalBible,
		VerseListState,
		VerseListTags,
		Settings,
		Help,
		DebugPreferences,
		DebugDatabase,
		DebugCache,
	}

	public int groupPosition;
	public int childPosition;
	public String name;
	public int id;
	public int count;
	public int color;
}
