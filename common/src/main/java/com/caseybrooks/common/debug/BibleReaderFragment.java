package com.caseybrooks.common.debug;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.caseybrooks.androidbibletools.basic.Reference;
import com.caseybrooks.androidbibletools.data.Formatter;
import com.caseybrooks.androidbibletools.providers.abs.ABSBible;
import com.caseybrooks.androidbibletools.providers.abs.ABSPassage;
import com.caseybrooks.androidbibletools.widget.ReferencePicker;
import com.caseybrooks.androidbibletools.widget.ReferencePickerListener;
import com.caseybrooks.androidbibletools.widget.VerseView;
import com.caseybrooks.common.R;
import com.caseybrooks.common.features.NavigationCallbacks;

public class BibleReaderFragment extends Fragment {
	Context context;
	NavigationCallbacks mCallbacks;

	ReferencePicker referencePicker;
	VerseView verseView;

	private static final String settings_file = "my_settings";
	private static final String PREFIX = "BIBLE_";

	private static final String PROGRESS = "PROGRESS";

	public static Fragment newInstance() {
		Fragment fragment = new BibleReaderFragment();
		Bundle extras = new Bundle();
		fragment.setArguments(extras);
		return fragment;
	}

//Lifecycle and Initialization
//------------------------------------------------------------------------------

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_bible_reader, container, false);

		this.context = getActivity();
		setHasOptionsMenu(true);

		referencePicker = (ReferencePicker) view.findViewById(R.id.reference_picker);
		referencePicker.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			  @Override
			  public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				  if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					  loadBible();
					  return true;
				  }
				  return false;
			  }
		  });

		verseView = (VerseView) view.findViewById(R.id.verse_view);
		verseView.setDisplayingRawText(false);
		verseView.setDisplayingAsHtml(true);

		return view;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();

		mCallbacks.setToolBar("Bible", Color.parseColor("#855585"));
		context.getSharedPreferences(settings_file, 0).edit().putInt("DRAWER_SELECTED_GROUP", 1).commit();
		context.getSharedPreferences(settings_file, 0).edit().putInt("DRAWER_SELECTED_CHILD", 0).commit();

		restoreProgress();
	}

	public void loadBible() {
		referencePicker.checkReference();
		referencePicker.setListener(new ReferencePickerListener() {
			@Override
			public void onPreParse(String textToParse) {

			}

			@Override
			public void onParseCompleted(Reference parsedReference, boolean wasSuccessful) {
				if(wasSuccessful) {
					saveProgress(parsedReference.toString());

					final ABSPassage passage = new ABSPassage(
							getResources().getString(R.string.bibles_org_key),
							parsedReference
					);
					passage.setFormatter(new Formatter() {
						@Override
						public String onPreFormat(Reference reference) {
							return "";
						}

						@Override
						public String onFormatVerseStart(int i) {
							return "<b><sup>" + i + "</sup></b>";
						}

						@Override
						public String onFormatText(String s) {
							return s;
						}

						@Override
						public String onFormatSpecial(String s) {
							return s;
						}

						@Override
						public String onFormatVerseEnd() {
							return "<br />";
						}

						@Override
						public String onPostFormat() {
							return "<br /><br /><small>" + ((ABSBible) passage.getBible()).getCopyright() + "</small>";
						}
					});
					verseView.loadSelectedBible();
					verseView.setVerse(passage);
					verseView.tryCacheOrDownloadText();
				}
			}
		});
	}

//Menu
//------------------------------------------------------------------------------
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_debug, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.menu_debug_refresh) {
			loadBible();
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}
	}

//Preferences
//------------------------------------------------------------------------------
	public void saveProgress(String progress) {
		context.getSharedPreferences(settings_file, 0).edit().putString(PREFIX + PROGRESS, progress).commit();
	}

	public void restoreProgress() {
		String progress = context.getSharedPreferences(settings_file, 0).getString(PREFIX + PROGRESS, "Matthew 1:1");

		referencePicker.setText(progress);
		loadBible();
	}

//Host Activity Interface
//------------------------------------------------------------------------------
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallbacks = (NavigationCallbacks) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException("Activity must implement NavigationCallbacks.");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = null;
	}
}