package com.caseybrooks.scripturememory.nowcards.input;

import android.content.Context;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.data.Bible;
import com.caseybrooks.androidbibletools.data.Reference;
import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
import com.caseybrooks.androidbibletools.io.Download;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.Util;
import com.caseybrooks.scripturememory.data.VerseDB;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerseInputCard extends FrameLayout {
//Data Members
//------------------------------------------------------------------------------
	Context context;
	Bible selectedVersion;
	Bible defaultESV;
	private final ReentrantLock lock = new ReentrantLock();

	public EditText editReference, editVerse;
	TextView addVerse;
	ImageButton contextMenu, searchButton;
	ProgressBar progress;


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
		progress = (ProgressBar) findViewById(R.id.progress);

		selectedVersion = MetaSettings.getBibleVersion(context);

		new AsyncTask<Void, Void, Void>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				progress.setVisibility(View.VISIBLE);
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);
				progress.setVisibility(View.INVISIBLE);
			}

			@Override
			protected Void doInBackground(Void... params) {
				//We need to allow the user to enter a verse and check it as soon
				//as possible. But it takes several seconds to parse the selectedVersion
				//document, and the user may enter a verse before that is finished.
				//In that case, we must be able to detect that this thread is still
				//trying to initialize that information, and immediately use the
				//'defaultESV' object instead of 'selectedVersion' to check the book name
				//against.

				lock.lock(); //get exclusive access to 'selectedVersion'
				try {
					//try to get the information for selectedVersion, either from cache or download it
					Document doc = Util.getChachedDocument(context, "selectedVersion.xml");

					if(doc == null && Util.isConnected(context)) {
						doc = Download.versionInfo(
								getResources().getString(R.string.bibles_org),
								selectedVersion.getVersionId());

						if(doc != null) {
							Util.cacheDocument(context, doc, "selectedVersion.xml");
							selectedVersion.getVersionInfo(doc);
						}
					}
					else {
						selectedVersion.getVersionInfo(doc);
					}
				}
				catch(IOException ioe) {
					ioe.printStackTrace();
				}
				finally {
					lock.unlock();
				}
				return null;
			}
		}.execute();


		defaultESV = new Bible(null);

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
	private class SearchVerseAsync extends AsyncTask<Void, Void, Void> {
		Passage passage;
		String toastMessage;
		String errorMessage;

		private void setErrorMessage(ParseException e) {
			e.printStackTrace();
			passage = null;
			toastMessage = null;
			if(e.getErrorOffset() == 1) {
				Matcher m = Pattern.compile("\\[(.*)\\]").matcher(e.getMessage());

				if(m.find()) {
					errorMessage = "'" + m.group(1) + "' is not a valid book name";
				}
			}
			else if(e.getErrorOffset() == 2) {
				errorMessage = "Required chapter number missing";
			}
			else if(e.getErrorOffset() == 3) {
				errorMessage = "Expected a list of verses, i.e. 3, 5-7";
			}
		}

		private boolean checkIfAlreadySaved() {
			if(passage != null) {
				VerseDB db = new VerseDB(context).open();

				int id = db.getVerseId(passage.getReference());
				db.close();

				return id != -1;
			}

			return false;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			progress.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				//if we have finished parsing the language document and have released
				//the lock, try parsing the input against that version. If it fails,
				//try parsing against 'defaultESV'. In the case that both the first
				//and the second fail, set the error message to that of the exception
				//thrown by the first, indicating that it is the primary version to
				//be used, and as such is the primary version to throw the exception
				if(lock.tryLock(250, TimeUnit.MILLISECONDS)) {
					Reference.Builder builder = new Reference.Builder()
							.setBible(selectedVersion)
							.parseReference(editReference.getText().toString());

					passage = new Passage(builder.create());
					Log.i("Input Verse", "Successfully parsed '" + editReference.getText().toString() + "' from Bible '" + selectedVersion.getVersionId() + "'");
				}

				//if we still haven't finished parsing the language document, just
				//parse the input against 'defaultESV'
				else {
					Reference.Builder builder = new Reference.Builder()
							.parseReference(editReference.getText().toString());

					passage = new Passage(builder.create());
					Log.i("Input Verse", "Successfully parsed '" + editReference.getText().toString() + "' from default ESV bible version");
				}

				//assuming we have parsed a verse...
				if(passage != null) {

					//if the verse was parsed such that it can be downloaded...
					if(passage.getReference().book.getId() != null) {
						String filename = passage.getReference().book.getId() + "." + passage.getReference().chapter;
						Document doc = Util.getChachedDocument(context, filename);

						//document wasn't cached, so try to get online and download it
						if(doc == null && Util.isConnected(context)) {
							doc = Download.bibleChapter(
									getResources().getString(R.string.bibles_org),
									passage.getReference());
							Util.cacheDocument(context, doc, filename);
						}

						//assuming we have either gotten a document out of the cache
						//or downloaded it, we can now parse the verse
						if(doc != null) {
							passage.getVerseInfo(doc);
							errorMessage = null;
							toastMessage = null;
							return null;
						}
						else {
							errorMessage = null;
							toastMessage = "Could not download verse text right now. Check your internet connection and try again.";
							return null;
						}
					}
					else {
						errorMessage = "The verse cannot be downloaded. The reference may be formatted poorly, or" +
								" it is not available for download in the current version.";
						return null;
					}
				}
				else {
					errorMessage = "There was an issue getting the verse from your reference. Make sure it is formatted correctly and try again.";
					return null;
				}
			}
			catch(IllegalArgumentException e1) {
				passage = null;
				errorMessage = null;
				toastMessage = "Verse does not exist or reference is not formatted properly";
				return null;
			}
			catch(IOException e2) {
				passage = null;
				errorMessage = null;
				toastMessage = "Something went wrong while downloading verse text. Check your internet connection and try again";
				return null;
			}
			catch(InterruptedException ie) {
				passage = null;
				errorMessage = null;
				toastMessage = "An unexpected error occurred. Please try again";
				return null;
			}
		}

		@Override
		protected void onPostExecute(Void params) {
			super.onPostExecute(params);

			progress.setVisibility(View.INVISIBLE);

			if(passage != null) {
				if(checkIfAlreadySaved()) {
					errorMessage = "Verse has already been added";
					editReference.setError(errorMessage);
				}

				editReference.setText(passage.getReference().toString());
				editVerse.setText(passage.getText());
			}
			else {
				if(toastMessage != null) Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show();
				if(errorMessage != null) editReference.setError(errorMessage);
			}
		}
	}

 	private OnClickListener addVerseClick = new OnClickListener() {
 		@Override
 		public void onClick(final View v) {
			if(editReference.getText().toString().trim().length() > 0 &&
					editVerse.getText().toString().trim().length() > 0) {
				try {
					Reference.Builder builder = new Reference.Builder()
							.setBible(MetaSettings.getBibleVersion(context))
							.parseReference(editReference.getText().toString());

                    Passage newVerse = new Passage(builder.create());
                    newVerse.setText(editVerse.getText().toString());
                    newVerse.getMetadata().putInt(DefaultMetaData.STATE, 1);
                    newVerse.getMetadata().putLong(DefaultMetaData.TIME_CREATED, Calendar.getInstance().getTimeInMillis());
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
}

