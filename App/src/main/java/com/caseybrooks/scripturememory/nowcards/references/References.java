package com.caseybrooks.scripturememory.nowcards.references;

import android.content.Context;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.basic.Reference;

public class References {
	Passage correctVerse;
	Reference[] choices;
	Context context;

	public References(Context context) {
		this.context = context;
	}
}
