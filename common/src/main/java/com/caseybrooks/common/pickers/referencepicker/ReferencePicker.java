package com.caseybrooks.common.pickers.referencepicker;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.caseybrooks.androidbibletools.basic.Bible;
import com.caseybrooks.androidbibletools.basic.Book;
import com.caseybrooks.androidbibletools.basic.Reference;
import com.caseybrooks.androidbibletools.pickers.biblepicker.BiblePickerSettings;
import com.caseybrooks.common.R;

/**
 * A simple widget to allow simple user input to choose a Reference. This may
 * be for the purpose of adding verses to Scripture Now, or as a more general
 * spinner-type input that one might see as the main navigation for a Bible reader
 */
public class ReferencePicker extends RelativeLayout {
//Data Members
//------------------------------------------------------------------------------
	Context context;

	AutoCompleteTextView editReference;
	ArrayAdapter<String> suggestionsAdapter;
	ImageView clearButton;

	Bible selectedBible;
	Reference reference;

//Constructors and Initialization
//------------------------------------------------------------------------------
	public ReferencePicker(Context context) {
		super(context);
		this.context = context;

		initialize();
	}

	public ReferencePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

		initialize();
	}

	public void initialize() {
		LayoutInflater.from(context).inflate(R.layout.reference_picker, this);

		editReference = (AutoCompleteTextView) findViewById(R.id.edit_reference);
		suggestionsAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
		editReference.setAdapter(suggestionsAdapter);
		editReference.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					editReference.showDropDown();
				}

				return false;
			}
		});
		editReference.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				editReference.setError(null);

				if(editReference.getText().toString().length() > 0) {
					clearButton.setVisibility(View.VISIBLE);
				}
				else {
					clearButton.setVisibility(View.GONE);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		clearButton = (ImageView) findViewById(R.id.clear_button);
		clearButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editReference.clearListSelection();
				editReference.setText("");
			}
		});

		getSelectedBible();
	}

	public void getSelectedBible() {
		selectedBible = BiblePickerSettings.getCachedBible(context);
		suggestionsAdapter.clear();

		for(Book book : selectedBible.getBooks()) {
			suggestionsAdapter.add(book.getName());
		}
	}

	public boolean checkReference() {
		if(editReference.getText().length() > 0) {
			Reference.Builder builder = new Reference.Builder();
			builder.setBible(selectedBible);
			builder.parseReference(editReference.getText().toString());
			reference = builder.create();

			//check flags for defaults. If the book is default, then don't post
			//the reference, just prompt the user to try with default or edit it

			if(!builder.checkFlag(Reference.Builder.DEFAULT_BOOK_FLAG)) {
				setText(reference.toString());
				return true;
			}
		}

		setError("Cannot parse reference");
		return false;
	}

	/**
	 * Always call checkReference() before this to ensure the most up-to-date
	 * parse of the reference, and be sure that the reference is even something
	 * that can be worked with further.
	 *
	 * @return the most recently parsed reference, regardless of whether it was
	 * a fully successful parse.
	 */
	public Reference getReference() {
		return reference;
	}

	public void setError(String errorMessage) {
		editReference.setError(errorMessage);
	}

	public void setText(String text) {
		editReference.setText(text);
	}

}
