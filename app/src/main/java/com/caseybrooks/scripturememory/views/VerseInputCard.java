package com.caseybrooks.scripturememory.views;

import android.content.Context;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.Util;
import com.caseybrooks.scripturememory.data.VerseDB;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;

public class VerseInputCard extends FrameLayout {
//Data Members
//------------------------------------------------------------------------------
	Context context;
	
	public EditText editReference, editVerse;
	TextView addVerse;
	ImageButton contextMenu, searchButton;


//Constructors and Initialization
//------------------------------------------------------------------------------
	public VerseInputCard(Context context) {
		super(context);
		this.context = context;
		
		LayoutInflater.from(context).inflate(R.layout.card_verse_input, this);
	    
		initialize();
	}
	
	public VerseInputCard(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		
		LayoutInflater.from(context).inflate(R.layout.card_verse_input, this);
        
        initialize();
	}
	
	void initialize() {
		editReference = (EditText) findViewById(R.id.editReference);
		editReference.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					new SearchVerseAsync().execute();
					return true;
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
				if(s.length() == 0) {
					searchButton.setVisibility(View.GONE);
				}
				else {
					searchButton.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		editVerse = (EditText) findViewById(R.id.editVerse);
		searchButton = (ImageButton) findViewById(R.id.searchButton);
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new SearchVerseAsync().execute();
			}
		});
		addVerse = (TextView) findViewById(R.id.addVerse);
		addVerse.setOnClickListener(addVerseClick);
		contextMenu = (ImageButton) findViewById(R.id.overflowButton);
    	contextMenu.setOnClickListener(contextMenuClick);
	}

	public void removeFromParent() {
        setVisibility(View.GONE);
		((ViewGroup)getParent()).removeView(VerseInputCard.this);
	}
	
//Public Getters and Setters
//------------------------------------------------------------------------------	
	public void setReference(String reference) {
		editReference.setText(reference);
	}
	
	public void setVerse(String verse) {
		editVerse.setText(verse);
	}
	
//OnClickListeners
//------------------------------------------------------------------------------
	private class SearchVerseAsync extends AsyncTask<Void, Void, Passage> {
		String message;

		@Override
		protected Passage doInBackground(Void... params) {
			try {
				if(Util.isConnected(context)) {
                    try {
                        Passage passage = new Passage(editReference.getText().toString());
                        passage.setVersion(MetaSettings.getBibleVersion(context));
                        passage.retrieve();
                        return passage;
                    }
                    catch (ParseException e) {
                        return null;
                    }
				}
				else {
					message = "Cannot search verse, no internet connection";
					return null;
				}
			}
			catch(IllegalArgumentException e1) {
				message = "Verse does not exist or reference is not formatted properly";
				return null;
			}
			catch(IOException e2) {
				message = "Error while retrieving verse";

				return null;
			}
		}

		@Override
		protected void onPostExecute(Passage params) {
			super.onPostExecute(params);
			if(params != null) {
				editReference.setText(params.getReference().toString());
				editVerse.setText(params.getText());
			}
			else {
				Toast.makeText(context, message, Toast.LENGTH_LONG).show();
			}
		}
	}
 	
 	private OnClickListener addVerseClick = new OnClickListener() {
 		@Override
 		public void onClick(final View v) {
			if(editReference.getText().toString().trim().length() > 0 &&
					editVerse.getText().toString().trim().length() > 0) {
				try {
                    Passage newVerse = new Passage(editReference.getText().toString());
                    newVerse.setText(editVerse.getText().toString());
                    newVerse.setVersion(MetaSettings.getBibleVersion(context));
                    newVerse.setState(1);
                    newVerse.setMillis(Calendar.getInstance().getTimeInMillis());
                    VerseDB db = new VerseDB(context);
                    db.open();
                    db.insertVerse(newVerse);
                    db.close();

					Toast.makeText(context, "Verse Saved", Toast.LENGTH_SHORT).show();
					editReference.setText("");
					editVerse.setText("");
				}
				catch(IllegalArgumentException e1) {
					e1.printStackTrace();
					Toast.makeText(context, "reference not formatted properly", Toast.LENGTH_LONG).show();
				}
				catch(Exception e) {
					Toast.makeText(context, "Something went wrong while saving verse", Toast.LENGTH_SHORT).show();
				}
			}
			else {
				Toast.makeText(context, "Fields Cannot be Empty", Toast.LENGTH_SHORT).show();
			}
 		}
 	};

//Context Menu
//------------------------------------------------------------------------------
	private OnClickListener contextMenuClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			removeFromParent();
		}
	};

//    public void setTextboxSelectedListener(OnClickListener textboxClicked) {
//        editReference.setOnClickListener(textboxClicked);
//        editVerse.setOnClickListener(textboxClicked);
//    }
}
	
