package com.caseybrooks.scripturememory.fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.data.Formatter;
import com.caseybrooks.androidbibletools.data.Reference;
import com.caseybrooks.androidbibletools.defaults.DefaultFormatter;
import com.caseybrooks.androidbibletools.io.Download;
import com.caseybrooks.scripturememory.R;

public class TestFragment extends Fragment {

	public static TestFragment newInstance() {
		TestFragment fragment = new TestFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	public TestFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(getArguments() != null) {
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		final View view = inflater.inflate(R.layout.fragment_test, container, false);

		//Card 1 stuff
		try {
			new AsyncTask<Void, Void, Void>() {
				Passage passage;
				String reference;
				String plain;
				Spanned html;
				String markup;
				Spanned htmlMarkup;

				@Override
				protected Void doInBackground(Void... params) {
					try {
						Reference.Builder builder = new Reference.Builder()
								.parseReference("Galatians 2:19");
						passage = new Passage(builder.create());
						passage.getVerseInfo(Download.bibleChapter(
								getResources().getString(R.string.bibles_org),
								passage.getReference()
						));

						reference = passage.getReference().toString();

						passage.setFormatter(new DefaultFormatter.Normal());
						plain = passage.getText();

						passage.setFormatter(verseFormatter);
						html = Html.fromHtml(passage.getText());


						passage.setText(markup);
						passage.setFormatter(verseFormatter);
						htmlMarkup = Html.fromHtml(passage.getText());
					}
					catch(Exception e) {
						e.printStackTrace();
					}
					return null;
				}

				@Override
				protected void onPostExecute(Void aVoid) {
					super.onPostExecute(aVoid);
					((TextView)view.findViewById(R.id.testReference1)).setText(reference);
					((TextView)view.findViewById(R.id.testVerse1)).setText(plain);

					((TextView)view.findViewById(R.id.testReference2)).setText(reference);
					((TextView)view.findViewById(R.id.testVerse2)).setText(html);

					((TextView)view.findViewById(R.id.testReference3)).setText(reference);
					((TextView)view.findViewById(R.id.testVerse3)).setText(markup);

					((TextView)view.findViewById(R.id.testReference4)).setText(reference);
					((TextView)view.findViewById(R.id.testVerse4)).setText(htmlMarkup);
				}
			}.execute();
		}
		catch(Exception e) {

		}

		return view;
	}

	Formatter verseFormatter = new Formatter() {
		@Override
		public String onPreFormat(Reference reference) {
			return "";
		}

		@Override
		public String onFormatVerseStart(int verseNumber) {
			return "<small><sup><b>" + verseNumber + "</b></sup></small>";
		}

		@Override
		public String onFormatText(String verseText) {
			return onFormatSpecial(verseText);
		}

		@Override
		public String onFormatSpecial(String special) {
			return "<font color=\"red\" >" + special + "</font>";
		}

		@Override
		public String onFormatVerseEnd() {
			return "<br>";
		}

		@Override
		public String onPostFormat() {
			return "";
		}
	};
}
