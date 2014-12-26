package com.caseybrooks.androidbibletools.basic;

import com.caseybrooks.androidbibletools.enumeration.Version;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//A Passage is a group of Verse objects that are in a sequence (i.e. Galatians 2:19-21)
//	A Passage is a basic type, and so its reference is non-modifiable. In addition,
//	a Passage can only contain verses that are all adjacent in the same Book. It ensures
//	this by setting the start and end verses, then populating everything between them.
//	Passage objects can parse an input string for the reference, and will optionally
//	parse a string for the individual verse text. When parsing the verse text, if it
//  cannot determine where to split it for the different verses, it will store
//  the whole text locally and keep the Verse objects just as markers. That being
//  said, if the verse text to be parsed was passed in from anywhere in this
//  library or read from a file created by the library, then the verse text
//  will be marked up to be able to be parsed.
public class Passage extends AbstractVerse {
//Data Members
//------------------------------------------------------------------------------
	//Data that makes up the Passage
	private ArrayList<Verse> verses;
	private String allText;

    private static Pattern hashtag = Pattern.compile("#((\\w+)|(\"[\\w ]+\"))");

    //Constructors
//------------------------------------------------------------------------------
    public Passage(String reference) throws ParseException {
		super(new Reference(reference));

        Collections.sort(this.reference.verses);
        this.verses = new ArrayList<Verse>();

        for(int i = 0; i < this.reference.verses.size(); i++) {
            this.verses.add(
                    new Verse(this.reference.book,
                    this.reference.chapter,
                    this.reference.verses.get(i)));
        }
	}

//Setters and Getters
//------------------------------------------------------------------------------
    @Override
    public AbstractVerse setVersion(Version version) {
        super.setVersion(version);

        for(Verse item : verses) item.setVersion(version);
        return this;
    }

	public Passage setText(String text) {
        //parse input string and extract any tags, denoted as standard hastags
        Matcher m = hashtag.matcher(text);

        while (m.find()) {
            String match = m.group(1);
            if(match.charAt(0) == '\"') {
                addTag(match.substring(1, match.length() - 1));
            }
            else {
                addTag(match);
            }
        }

        this.allText =  m.replaceAll("");

		return this;
	}

	@Override
	public String getText() {
        if(allText == null) {
            String text = "";

            text += formatter.onPreFormat(reference);

            for (int i = 0; i < verses.size(); i++) {
                Verse verse = verses.get(i);

                text += formatter.onFormatNumber(verse.reference.verse);
                text += formatter.onFormatText(verse.verseText);

                if (i < verses.size() - 1) {
                    text += formatter.onFormatNewVerse();
                }
            }

            return text;
        }
        else {
            String text = "";

            text += formatter.onPreFormat(reference);
            text += formatter.onFormatText(allText);
            text += formatter.onPostFormat();

            return text;
        }
	}

	public Verse[] getVerses() {
		Verse[] versesArray = new Verse[verses.size()];
		verses.toArray(versesArray);
		return versesArray;
	}

	@Override
	public int compareTo(AbstractVerse verse) {
		Verse lhs = this.verses.get(0);
		Verse rhs = ((Passage) verse).verses.get(0);

		return lhs.compareTo(rhs);
	}

	@Override
	public boolean equals(AbstractVerse verse) {
        Verse lhs = this.verses.get(0);
        Verse rhs = ((Passage) verse).verses.get(0);

		return lhs.compareTo(rhs) == 0;
	}

//Retrieve verse from the Internet
//------------------------------------------------------------------------------
    @Override
    public String getURL() {
        String query = "http://www.biblestudytools.com/" + version.getCode() + "/" +
                reference.book.getName().toLowerCase().trim().replaceAll(" ",  "-") +
                "/passage.aspx?q=" +
                reference.book.getName().toLowerCase().trim().replaceAll(" ",  "-") +
                "+" +
                reference.chapter + ":" + reference.verses.get(0);

        if(reference.verses.size() > 1) {
            query += "-" + reference.verses.get(reference.verses.size()-1);
        }
        return query;
    }

    @Override
	public Passage retrieve() throws IOException {
        for(Verse verse : verses) {
            verse.retrieve();
        }

		return this;
	}
}
