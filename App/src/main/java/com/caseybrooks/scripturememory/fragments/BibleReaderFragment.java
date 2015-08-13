package com.caseybrooks.scripturememory.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

import com.caseybrooks.androidbibletools.basic.AbstractVerse;
import com.caseybrooks.androidbibletools.basic.Bible;
import com.caseybrooks.androidbibletools.basic.Reference;
import com.caseybrooks.androidbibletools.data.Formatter;
import com.caseybrooks.androidbibletools.providers.abs.ABSBible;
import com.caseybrooks.androidbibletools.providers.abs.ABSPassage;
import com.caseybrooks.androidbibletools.widget.IReferencePickerListener;
import com.caseybrooks.androidbibletools.widget.IVerseViewListener;
import com.caseybrooks.androidbibletools.widget.LoadState;
import com.caseybrooks.androidbibletools.widget.ReferencePicker;
import com.caseybrooks.androidbibletools.widget.VerseView;
import com.caseybrooks.common.features.NavigationCallbacks;
import com.caseybrooks.scripturememory.R;

public class BibleReaderFragment extends Fragment implements IReferencePickerListener, IVerseViewListener {
	Context context;
	NavigationCallbacks mCallbacks;

	ReferencePicker referencePicker;
	ImageView clearButton;
	VerseView verseView;
	Toolbar toolbar;
	View headerView;
	String title;
	String key;
	int id;

	private static final String settings_file = "my_settings";
	private static final String PREFIX = "BIBLE_";

	private static final String PROGRESS = "_PROGRESS";
	private static final String TRACK = "_TRACK";

	public static Fragment newInstance(int id) {
		Fragment fragment = new BibleReaderFragment();
		Bundle extras = new Bundle();
		extras.putInt("EXTRA_ID", id);
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

		Bundle args = getArguments();
		id = args.getInt("EXTRA_ID", 0);
		key = (id >= 0) ? getResources().getStringArray(R.array.ribbon_keys)[id] : "";
		title = (id >= 0) ? getResources().getStringArray(R.array.ribbon_names)[id] : "Bible";

		toolbar = mCallbacks.getToolbar();

		headerView = inflater.inflate(R.layout.header_bible_reader, container, false);

		clearButton = (ImageView) headerView.findViewById(R.id.button_clear);
		clearButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				referencePicker.setText("");
			}
		});

		referencePicker = (ReferencePicker) headerView.findViewById(R.id.reference_picker);
		referencePicker.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_SEARCH) {
					String progress = referencePicker.getText().toString();
					String[] pieces = progress.split(":");
					if(pieces.length >= 1) {
						progress = pieces[0];
					}
					referencePicker.setText(progress);
					referencePicker.checkReference();
					return true;
				}
				return false;
			}
		});
		referencePicker.setListener(this);

		verseView = (VerseView) view.findViewById(R.id.verse_view);
		verseView.setDisplayingRawText(false);
		verseView.setDisplayingAsHtml(true);
		verseView.setListener(this);

		return view;
	}

	@Override
	public void onPause() {
		super.onPause();
		mCallbacks.collapseExpandedToolbar();
	}

	@Override
	public void onResume() {
		super.onResume();

		int color;
		switch(id) {
		case 0:
			color = getResources().getColor(R.color.ribbon_nt);
			break;
		case 1:
			color = getResources().getColor(R.color.ribbon_ot);
			break;
		case 2:
			color = getResources().getColor(R.color.ribbon_wis);
			break;
		case 3:
			color = getResources().getColor(R.color.ribbon_other);
			break;
		default:
			color = getResources().getColor(R.color.ribbon_nt);
		}

		mCallbacks.setToolBar(title, color);
		mCallbacks.expandToolbarWIthView(headerView);
		context.getSharedPreferences(settings_file, 0).edit().putInt("DRAWER_SELECTED_GROUP", 1).commit();
		context.getSharedPreferences(settings_file, 0).edit().putInt("DRAWER_SELECTED_CHILD", id).commit();

		restoreProgress();
	}

//Menu
//------------------------------------------------------------------------------
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_bible_reader, menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

//Preferences
//------------------------------------------------------------------------------
	public void saveProgress(String progress) {
		context.getSharedPreferences(settings_file, 0).edit().putString(PREFIX + key +  PROGRESS, progress).commit();
	}

	public void restoreProgress() {
		String progress = context.getSharedPreferences(settings_file, 0).getString(PREFIX + key + PROGRESS, "Matthew 1:1");

		String[] pieces = progress.split(":");
		if(pieces.length >= 1) {
			progress = pieces[0];
		}

		verseView.loadSelectedBible();
		referencePicker.loadSelectedBible();
		referencePicker.setText(progress);
		referencePicker.checkReference();
	}

//Load Data
//------------------------------------------------------------------------------
	@Override
	public boolean onBibleLoaded(Bible bible, LoadState state) {

		return false;
	}

	@Override
	public boolean onVerseLoaded(AbstractVerse verse, LoadState state) {
		return false;
	}

	@Override
	public boolean onReferenceParsed(Reference parsedReference, boolean wasSuccessful) {
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
					return "<br/>";
				}

				@Override
				public String onPostFormat() {
					return "<br/><br/><small>" + ((ABSBible)passage.getBible()).getCopyright() + "</small>";
				}
			});
			verseView.loadSelectedBible();
			verseView.setVerse(passage);
			verseView.tryCacheOrDownloadText();
		}
		return false;
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
