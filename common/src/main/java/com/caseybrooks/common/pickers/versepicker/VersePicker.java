package com.caseybrooks.common.pickers.versepicker;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.caseybrooks.androidbibletools.basic.Bible;
import com.caseybrooks.androidbibletools.basic.Book;
import com.caseybrooks.androidbibletools.basic.Reference;
import com.caseybrooks.androidbibletools.pickers.biblepicker.BiblePickerDialog;
import com.caseybrooks.androidbibletools.pickers.biblepicker.BiblePickerSettings;
import com.caseybrooks.androidbibletools.providers.abs.ABSBible;
import com.caseybrooks.common.R;

public class VersePicker extends RelativeLayout {
//Data Members
//------------------------------------------------------------------------------
	Context context;

	AutoCompleteTextView editReference;
	EditText editVerse;

	ImageView checkReferenceButton, lookupTextButton, saveVerseButton, showVersionsButton;

	Bible selectedBible;
	Reference reference;

	boolean shouldUseDefault = false;
	ArrayAdapter<String> suggestionsAdapter;


	//Constructors and Initialization
//------------------------------------------------------------------------------
	public VersePicker(Context context) {
		super(context);
		this.context = context;

		initialize();
	}

	public VersePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

		initialize();
	}


	public void initialize() {
		LayoutInflater.from(context).inflate(R.layout.verse_picker, this);

		editReference = (AutoCompleteTextView) findViewById(R.id.edit_reference);
		suggestionsAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
		editReference.setAdapter(suggestionsAdapter);

		editVerse = (EditText) findViewById(R.id.edit_verse);

		checkReferenceButton = (ImageView) findViewById(R.id.check_reference_button);
		checkReferenceButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				checkReference();
			}
		});

		lookupTextButton = (ImageView) findViewById(R.id.lookup_text_button);
		lookupTextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				lookupText();
			}
		});

		saveVerseButton = (ImageView) findViewById(R.id.save_verse_button);
		saveVerseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveVerse();
			}
		});

		showVersionsButton = (ImageView) findViewById(R.id.show_versions_button);
		showVersionsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showVersions();
			}
		});

		getBible();
	}

	public void getBible() {
		selectedBible = BiblePickerSettings.getCachedBible(context);
		suggestionsAdapter.clear();

		for(Book book : selectedBible.getBooks()) {
			suggestionsAdapter.add(book.getName());
		}

	}

	/**
	 * How to tell if input was parsed correctly:
	 * 	TODO: set flag in Reference.Builder to tell if any field has been defaultd
	 */
	public void checkReference() {
		if(editReference.getText().length() > 0) {
			Reference.Builder builder = new Reference.Builder();

			if(shouldUseDefault) {
				builder.setBible(new ABSBible(context.getResources().getString(R.string.bibles_org_key), null));
			}
			else {
				builder.setBible(selectedBible);
			}


			builder.parseReference(editReference.getText().toString());
			reference = builder.create();

			//check flags for defaults. If the book is default, then don't post
			//the reference, just prompt the user to try with default or edit it

			if(builder.checkFlag(Reference.Builder.DEFAULT_BOOK_FLAG)) {
				if(shouldUseDefault) {
					editReference.setError("Cannot parse reference");
				}
				else {
					editReference.setError("Book not recognized in " + selectedBible.getAbbreviation() +
							", click again to search in the default ESV");
					shouldUseDefault = true;
					if(resetError != null && resetError.isAlive()) {
						resetError.interrupt();
						resetError = new Thread(resetErrorRunnable);
						resetError.start();
					}
					else {
						resetError = new Thread(resetErrorRunnable);
						resetError.start();
					}
				}
			}
			else {
				editReference.setText(reference.toString());
			}
		}
	}

	Thread resetError;
	Runnable resetErrorRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				Thread.sleep(5000);
			}
			catch(InterruptedException ie) {
			}

			if(!Thread.interrupted()) {
				((Activity) context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						editReference.setError(null);
						shouldUseDefault = false;
					}
				});
			}
		}
	};

	public void lookupText() {
//		new AsyncTask<Void, Void, Void>() {
//			ABSPassage passage;
//			@Override
//			protected void onPreExecute() {
//				super.onPreExecute();
//				//get a progressbar spinning...
//			}
//
//			@Override
//			protected void onPostExecute(Void aVoid) {
//				super.onPostExecute(aVoid);
//				//stop the progressbar spinning...
//
//				editReference.setText(text);
//			}
//
//			@Override
//			protected Void doInBackground(Void... params) {
//
//				return null;
//			}
//		}.execute();
	}

	public void saveVerse() {

	}

	public void showVersions() {
		BiblePickerDialog.create(context).show();
		getBible();
	}
}
