package com.caseybrooks.common.pickers.biblepicker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

import com.caseybrooks.androidbibletools.basic.Bible;

public class BiblePickerPreference extends DialogPreference implements OnBibleSelectedListener{
	OnBibleSelectedListener listener;
	BiblePicker picker;

	public BiblePickerPreference(Context context, AttributeSet attrs) {
		//"hack" to ensure custom Preferences all look the same
		this(context, attrs, Resources.getSystem().getIdentifier("dialogPreferenceStyle", "attr", "android"));
	}

	public BiblePickerPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		Bible bible = BiblePickerSettings.getSelectedBible(context);

		setTitle("Preferred Bible");
		setSummary(bible.getAbbreviation());
	}

	@Override
	protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
		super.onPrepareDialogBuilder(builder);
		builder.setNegativeButton(null, null);
		builder.setNeutralButton(null, null);
	}

	@Override
	protected View onCreateDialogView() {
		picker = new BiblePicker(getContext());
		picker.setListener(this);
		return picker;
	}

	@Override
	public void onBibleSelected(Bible bible) {
		if(listener != null)
			listener.onBibleSelected(bible);

		setSummary(bible.getName());
	}

	public OnBibleSelectedListener getListener() {
		return listener;
	}

	public void setListener(OnBibleSelectedListener listener) {
		this.listener = listener;
	}
}
