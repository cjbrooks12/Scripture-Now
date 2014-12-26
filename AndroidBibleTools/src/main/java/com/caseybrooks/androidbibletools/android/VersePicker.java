package com.caseybrooks.androidbibletools.android;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.caseybrooks.androidbibletools.R;

public class VersePicker extends LinearLayout {
	Context context;
	Spinner versionSpinner, bookSpinner, chapterSpinner, verseSpinner;
	ArrayAdapter<String> versionAdapter, bookAdapter, chapterAdapter, verseAdapter;
	String[] versionItems, bookItems, chapterItems, verseItems;

	public VersePicker(Context context) {
		super(context);
		initialize(context, null, 0);
	}

	public VersePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context, attrs, 0);
	}

	private void initialize(Context context, AttributeSet attrs, int defStyle) {
		this.context = context;

		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.versepicker, this);



	}
}
