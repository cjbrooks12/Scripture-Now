package com.caseybrooks.common.pickers.biblepicker;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import com.caseybrooks.androidbibletools.basic.Bible;

public class BiblePickerDialog extends AlertDialog implements OnBibleSelectedListener {
	OnBibleSelectedListener listener;
	BiblePicker picker;

	public static BiblePickerDialog create(Context context) {
		return new BiblePickerDialog(context);
	}

	protected BiblePickerDialog(Context context) {
		super(context);

		picker = new BiblePicker(context);
		picker.setListener(this);
		setView(picker);
	}

	@Override
	public void onBibleSelected(Bible bible) {
		if(listener != null)
			listener.onBibleSelected(bible);
	}

	public OnBibleSelectedListener getListener() {
		return listener;
	}

	public void setListener(OnBibleSelectedListener listener) {
		this.listener = listener;
	}
}
