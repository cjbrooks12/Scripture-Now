package com.caseybrooks.common.pickers.versepicker;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.TextView;

import com.caseybrooks.androidbibletools.basic.Bible;
import com.caseybrooks.androidbibletools.basic.Reference;
import com.caseybrooks.androidbibletools.data.Formatter;
import com.caseybrooks.androidbibletools.pickers.biblepicker.BiblePickerSettings;
import com.caseybrooks.androidbibletools.providers.abs.ABSBible;
import com.caseybrooks.androidbibletools.providers.abs.ABSPassage;
import com.caseybrooks.common.features.Util;

import org.jsoup.nodes.Document;

import java.io.IOException;

public class VerseView extends TextView {
//Data Members
//------------------------------------------------------------------------------
	Context context;

	Bible selectedBible;
	ABSPassage verse;

//Constructors and Initialization
//------------------------------------------------------------------------------
	public VerseView(Context context) {
		super(context);
		this.context = context;

		initialize();
	}

	public VerseView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

		initialize();
	}

	public void initialize() {
		getSelectedBible();
	}

	public void getSelectedBible() {
		selectedBible = BiblePickerSettings.getCachedBible(context);
		if(verse != null) {
			verse.setBible(selectedBible);
		}
	}

	public void setVerse(ABSPassage verse) {
		this.verse = verse;
		this.verse.setBible(selectedBible);
		this.verse.setFormatter(biblePageFormatter);
	}

	/**
	 * Writes the contents of the current passage to the TextView. Forces this
	 * to be done on the UI thread, so this can be safely called from
	 * any background thread.
	 */
	public void displayText() {
		((Activity)context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				setText(Html.fromHtml(verse.getText()));
			}
		});
	}

	/**
	 * Tries to get the text of the given verse from the cache to display. If
	 * a cached file exists, it will be parsed and displayed as HTML inside
	 * this TextView
	 *
	 * @return true if a cached verse was found and could be displayed
	 */
	public boolean displayCachedText() {
		Document doc = Util.getChachedDocument(context, verse.getId());

		if(doc != null) {
			verse.parseDocument(doc);
			displayText();
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Tries to download the text of this verse from the internet. It does not
	 * try to get the text from the cache, it simply downloads it new. Useful
	 * for forcing a redownload. If the verse is downloaded successfully, it
	 * will display the parsed text as HTML
	 */
	public void displayDownloadedText() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Document doc = verse.getDocument();
					if(doc != null) {
						Util.cacheDocument(context, doc, verse.getId());
						verse.parseDocument(doc);
						displayText();
					}
				}
				catch(IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * Try to get the verse text and display it. It first checks the cache, and
	 * failing to display text from the cache, will try to download it from the
	 * internet.
	 */
	public void tryCacheOrDownloadText() {
		if(!displayCachedText()) {
			displayDownloadedText();
		}
	}

	Formatter biblePageFormatter = new Formatter() {

		@Override
		public String onPreFormat(Reference reference) {
			return "";
		}

		@Override
		public String onFormatVerseStart(int i) {
			return "<b><sup>" + i + "</sup></b> ";
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
			return "<br/><br/>" + ((ABSBible)selectedBible).getCopyright();
		}
	};
}
