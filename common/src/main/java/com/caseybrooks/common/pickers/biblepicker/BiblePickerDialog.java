package com.caseybrooks.common.pickers.biblepicker;

import android.content.Context;
import android.support.v7.app.AlertDialog;

/**
 * Created by Casey on 7/7/2015.
 */
public class BiblePickerDialog extends AlertDialog {

	public static BiblePickerDialog create(Context context) {
		return new BiblePickerDialog(context);
	}

	protected BiblePickerDialog(Context context) {
		super(context);

		setView(new BiblePicker(context));
	}
}
