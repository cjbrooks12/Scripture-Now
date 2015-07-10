package com.caseybrooks.common.pickers.biblepicker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.caseybrooks.androidbibletools.basic.Bible;

public class BiblePickerFragment extends Fragment implements OnBibleSelectedListener {
	OnBibleSelectedListener listener;
	BiblePicker picker;

	public static Fragment newInstance() {
		return new BiblePickerFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		setHasOptionsMenu(true);

		picker = new BiblePicker(getActivity());
		picker.setListener(this);

		return picker;
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

