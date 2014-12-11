package com.caseybrooks.scripturememory.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.basic.Verse;
import com.caseybrooks.androidbibletools.enumeration.Book;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchFragment extends Fragment {
    //Data Members
//------------------------------------------------------------------------------
    Context context;
    View view;
    RelativeLayout searchLayout;
    ActionBar ab;

	//To test how the comparison methods work
    TextView comparisonResult;
    EditText a_et, b_et;
	Verse verseA, verseB;

	//To test the Passage structure
	TextView passageResult;
	EditText p_et;
	Passage passage;
    LinearLayout layout;

	//To test putting it all together to make a simple search
	Verse spinnerVerse;
	TextView searchResultReference, searchResultVerse;

	Spinner bookSpinner;
	ArrayAdapter<String> bookAdapter;
	String[] bookItems;

	Spinner chapterSpinner;
	ArrayAdapter<String> chapterAdapter;
	String[] chapterItems;

	Spinner verseSpinner;
	ArrayAdapter<String> verseAdapter;
	String[] verseItems;

	//To test the correctness of my Matcher regex
	TextView matcherResult;
	EditText m_et;
//	Pattern oneVerse = Pattern.compile("((\\d\\s*)?\\w+\\s*\\d+\\s*\\W\\s*\\d+)");
	Pattern oneVerse = Pattern.compile("((\\d\\s*)?\\w+)\\s*(\\d+)\\W+(\\d+)");
	Pattern rangeInChapter = Pattern.compile("((\\d\\s*)?\\w+\\s*\\d+\\W+)(\\d+)\\W+(\\d+)");
	Pattern rangeDifferentChapters = Pattern.compile("((\\d\\s*)?\\w+\\s*)(\\d+\\W+\\d+)\\W+(\\d+\\W+\\d+)");



	//Lifecycle and Initialization
//------------------------------------------------------------------------------
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_search, container, false);
        context = getActivity();
        initialize();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void initialize() {
        searchLayout = (RelativeLayout) view.findViewById(R.id.search_results_layout);

		//comparison test card
        comparisonResult = (TextView) view.findViewById(R.id.comparison_result);
        a_et = (EditText) view.findViewById(R.id.a_et);
		a_et.setText("Galatians 2:19");
		verseA = new Verse(a_et.getText().toString());

        b_et = (EditText) view.findViewById(R.id.b_et);
		b_et.setText("Galatians 2:20");
		verseB = new Verse(b_et.getText().toString());

		view.findViewById(R.id.a_next).setOnClickListener(comparisonClick);
		view.findViewById(R.id.a_previous).setOnClickListener(comparisonClick);
		view.findViewById(R.id.b_next).setOnClickListener(comparisonClick);
		view.findViewById(R.id.b_previous).setOnClickListener(comparisonClick);

		//passage test card
		passageResult = (TextView) view.findViewById(R.id.passage_result);
        layout = (LinearLayout) view.findViewById(R.id.p_layout);
		p_et = (EditText) view.findViewById(R.id.p_et);
		p_et.setText("Galatians 2:19-21");
		passage = new Passage(p_et.getText().toString());

		view.findViewById(R.id.p_add).setOnClickListener(passageClick);

		setupActionBar();

		spinnerVerse = new Verse(MetaSettings.getBibleVersion(context), "Genesis 1:1");
		searchResultReference = (TextView) view.findViewById(R.id.search_result_reference);
		searchResultVerse = (TextView) view.findViewById(R.id.search_result_verse);

		bookSpinner = (Spinner) view.findViewById(R.id.book_spinner);
		bookItems = Book.getList();
		bookAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, bookItems);
		bookAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		bookSpinner.setAdapter(bookAdapter);
		bookSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				spinnerVerse = new Verse(Book.values()[position], spinnerVerse.getChapter(), spinnerVerse.getVerseNumber());
				searchResultReference.setText(spinnerVerse.getReference());

				chapterItems = new String[spinnerVerse.getBook().numChapters()];
				for(int i = 0; i < spinnerVerse.getBook().numChapters(); i++) {
					chapterItems[i] = Integer.toString(i+1);
				}

				chapterAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, chapterItems);
				chapterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				chapterSpinner.setAdapter(chapterAdapter);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
	  	});

		chapterSpinner = (Spinner) view.findViewById(R.id.chapter_spinner);
		chapterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				spinnerVerse = new Verse(spinnerVerse.getBook(), position+1, spinnerVerse.getVerseNumber());
				searchResultReference.setText(spinnerVerse.getReference());

				verseItems = new String[spinnerVerse.getBook().numVersesInChapter(position + 1)];
				for(int i = 0; i < spinnerVerse.getBook().numVersesInChapter(position + 1); i++) {
					verseItems[i] = Integer.toString(i+1);
				}

				verseAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, verseItems);
				verseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				verseSpinner.setAdapter(verseAdapter);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		verseSpinner = (Spinner) view.findViewById(R.id.verse_spinner);
		verseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				spinnerVerse = new Verse(spinnerVerse.getBook(), spinnerVerse.getChapter(), position+1);
				searchResultReference.setText(spinnerVerse.getReference());
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		view.findViewById(R.id.s_search).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new SearchVerseAsync().execute();
			}
		});

		//Matcher testing
		matcherResult = (TextView) view.findViewById(R.id.matcher_result);
		m_et = (EditText) view.findViewById(R.id.m_et);
		m_et.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String reference = s.toString().trim().toLowerCase();

				Matcher m1 = oneVerse.matcher(reference);
				Matcher m2 = rangeInChapter.matcher(reference);
				Matcher m3 = rangeDifferentChapters.matcher(reference);

				if(m1.matches()) {
					matcherResult.setText("Single Verse");
				}
				else if(m2.matches()) {
					matcherResult.setText("Range in Same Chapter");
				}
				else if(m3.matches()) {
					matcherResult.setText("Range in Different Chapters");
				}
				else {
					matcherResult.setText("Not formatted correctly");
				}
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

    }

	private class SearchVerseAsync extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {


			try {
				spinnerVerse.retrieve();
			}
			catch (Exception e) {
                e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void params) {
			super.onPostExecute(params);

			searchResultVerse.setText(spinnerVerse.getText());
		}
	}

//OnClickListeners
//------------------------------------------------------------------------------
    private View.OnClickListener comparisonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
			switch(v.getId()) {
				case R.id.a_next:
					verseA = new Verse(a_et.getText().toString());
					verseA = verseA.next();
					a_et.setText(verseA.getReference());
					break;
				case R.id.a_previous:
					verseA = new Verse(a_et.getText().toString());
					verseA = verseA.previous();
					a_et.setText(verseA.getReference());
					break;
				case R.id.b_next:
					verseB = new Verse(b_et.getText().toString());
					verseB = verseB.next();
					b_et.setText(verseB.getReference());
					break;
				case R.id.b_previous:
					verseB = new Verse(b_et.getText().toString());
					verseB = verseB.previous();
					b_et.setText(verseB.getReference());
					break;
			}

			switch(verseA.compareTo(verseB)) {
				case -4:
					comparisonResult.setText("A is before B, but in a different book");
					break;
				case -3:
					comparisonResult.setText("A is before B, but in a different chapter");
					break;
				case -2:
					comparisonResult.setText("A is before B in the same chapter");
					break;
				case -1:
					comparisonResult.setText("A is immediately before B");
					break;
				case 0:
					if(verseA.equals(verseB) && verseB.equals(verseA) && verseB.compareTo(verseA) == 0)
						comparisonResult.setText("A and B are equal");
					else
						comparisonResult.setText("Check comaprison methods, because they are incorrect");
					break;
				case 1:
					comparisonResult.setText("A is immediately after B");
					break;
				case 2:
					comparisonResult.setText("A is after B in the same chapter");
					break;
				case 3:
					comparisonResult.setText("A is after B, but in a different chapter");
					break;
				case 4:
					comparisonResult.setText("A is after B, but in a different book");
					break;
			}
        }
    };

	private View.OnClickListener passageClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			passage = new Passage(p_et.getText().toString());
            layout.removeAllViews();

			passageResult.setText(passage.getReference());
            Verse[] verses = passage.getVerses();
            for(Verse item : verses) {
                TextView tv = new TextView(context);
                tv.setText(item.getReference());
                layout.addView(tv);
            }
		}
	};



//ActionBar
//------------------------------------------------------------------------------
    private void setupActionBar() {
        ab = ((ActionBarActivity) context).getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
