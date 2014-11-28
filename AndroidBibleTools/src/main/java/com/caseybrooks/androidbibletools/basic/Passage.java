package com.caseybrooks.androidbibletools.basic;

import android.util.Log;

import com.caseybrooks.androidbibletools.enumeration.Flags;
import com.caseybrooks.androidbibletools.enumeration.Version;

import java.io.IOException;
import java.util.ArrayList;
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
    private final Verse start, end;
	private String allText;

	private static Pattern oneVerse = Pattern.compile("((\\d\\s*)?\\w+\\s*\\d+\\s*\\W\\s*\\d+)");
	private static Pattern rangeInChapter = Pattern.compile("((\\d\\s*)?\\w+\\s*\\d+\\W+)(\\d+)\\W+(\\d+)");
	private static Pattern rangeDifferentChapters = Pattern.compile("((\\d\\s*)?\\w+\\s*)(\\d+\\W+\\d+)\\W+(\\d+\\W+\\d+)");
    private static Pattern hashtag = Pattern.compile("#((\\w+)|(\"[\\w ]+\"))");

    //Constructors
//------------------------------------------------------------------------------
    public Passage(String reference) {
		super();

		reference = reference.trim().toLowerCase();
		reference = reference.replaceAll(" and ", "-");

		Matcher m1 = oneVerse.matcher(reference);
        Matcher m2 = rangeInChapter.matcher(reference);
        Matcher m3 = rangeDifferentChapters.matcher(reference);

        try {
            Verse a, b;
            if (m1.matches()) {
                a = new Verse(m1.group(1));
                b = new Verse(m1.group(1));
            } else if (m2.matches()) {
                a = new Verse(m2.group(1) + m2.group(3));
                b = new Verse(m2.group(1) + m2.group(4));
            } else if (m3.matches()) {
                a = new Verse(m3.group(1) + m3.group(3));
                b = new Verse(m3.group(1) + m3.group(4));
            } else {
                throw new IllegalArgumentException("Passage does not exist: String not formatted properly, cannot parse '" + reference + "'");
            }


            if(a.compareTo(b) < 0) {
                start = a;
                end = b;
            }
            else if(a.compareTo(b) > 0) {
                start = b;
                end = a;
            }
            else {
                start = a;
                end = a;
            }

            verses = new ArrayList<Verse>();
            verses.add(start);

            //walk through bible verses, adding a verse to the container for each one between start and end
            Verse nextVerse = start;
            while(true) {
                if(nextVerse.equals(end)) break;
                else {
                    nextVerse = nextVerse.next();
                    verses.add(nextVerse);
                }
            }
        }
        catch (IllegalArgumentException e1) {
            throw new IllegalArgumentException("Passage does not exist: One or more Verses in '" + reference + "' does not exist", e1);
        }
	}

	public Passage(Version version, String reference) {
		this(reference);
		this.version = version;
		for(Verse item : verses) item.setVersion(version);
	}

	public Passage(String reference, String text) {
		this(reference);
		setText(text);
	}

	public Passage(Version version, String reference, String text) {
		this(version, reference);
		setText(text);
	}

//Setters and Getters
//------------------------------------------------------------------------------


    @Override
    public AbstractVerse setVersion(Version version) {
        super.setVersion(version);

        for(Verse item : verses) item.setVersion(version);
        return this;
    }

    @Override
	public String getReference() {
		switch(Math.abs(start.compareTo(end))) {
			case 0: return start.getReference();
			case 1:
				if(start.getChapter() == end.getChapter()) {
					return start.getReference() + "-" + end.getVerseNumber();
				}
				else {
					return start.getReference() + " - " + end.getChapter() + ":" + end.getVerseNumber();
				}
			case 2: return start.getReference() + "-" + end.getVerseNumber();
			case 3: return start.getReference() + " - " + end.getChapter() + ":" + end.getVerseNumber();
			default: return start.getReference() + " - " + end.getReference();
		}
	}

	public Passage setText(String text) {
//		if(text.matches(".+\\(\\n+\\).+")) {
//			String[] splitVerses = text.split(".+\\(\\n+\\).+");
//			if(splitVerses.length == verses.size()) {
//				for(int i = 0; i < verses.size(); i++) {
//					verses.get(i).setText(splitVerses[i]);
//				}
//				allText = null;
//			}
//			else {
//				this.allText = text;
//			}
//		}
//		else {
            //parse input string and extract any tags, denoted as standard hastags
        Matcher m = hashtag.matcher(text);

        while (m.find()) {
            String match = m.group(1);
            if(match.charAt(0) == '\"') {
                addTag(match.substring(1, match.length()-1));
                Log.i("HASHTAG FOUND", match.substring(1, match.length()-1));

            }
            else {
                addTag(match);
                Log.i("HASHTAG FOUND", match);

            }
        }

        this.allText =  m.replaceAll("");
//		}

		return this;
	}

	@Override
	public String getText() {
		if(allText == null) {
			String text = "";

			for (Verse verse : verses) {
				verse.setFlags(flags);
				text += verse.getText();
			}

			return text;
		}
		else {
			String text = "";

			//Will print only the first flag that is set, or normal if none are set
			if(flags.contains(Flags.TEXT_NORMAL)) {
				text += allText + " ";
			}
			else if(flags.contains(Flags.TEXT_DASHES)) {
				text += allText.replaceAll("\\w", "_") + " ";
			}
			else if(flags.contains(Flags.TEXT_LETTERS)) {
				text += allText.toUpperCase().replaceAll("(\\w)(\\w*)", "$1 ") + " ";
			}
			else if(flags.contains(Flags.TEXT_DASHED_LETTERS)) {
				text += allText.toUpperCase().replaceAll("(\\B\\w)", "_") + " ";
			}
			else { //if no flags are given, print out normal
				text += allText + " ";
			}

			if(flags.contains(Flags.PRINT_NEWLINE)) {
				text += "\n";
			}
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
		Verse lhs = this.start;
		Verse rhs = ((Passage) verse).start;

		return lhs.compareTo(rhs);
	}

	@Override
	public boolean equals(AbstractVerse verse) {
		Verse lhs = this.start;
		Verse rhs = ((Passage) verse).start;

		return lhs.compareTo(rhs) == 0;
	}

//Retrieve verse from the Internet
//------------------------------------------------------------------------------
	@Override
	public Passage retrieve() throws IOException {
//		//build URL
//		String query = "http://www.biblestudytools.com/" +
//							   version.getCode() + "/" +
//							   start.getBook().getName().toLowerCase() + "/passage.aspx?q=" +
//							   start.getBook().getName().toLowerCase() + "+" +
//							   start.getChapter() + ":" + start.getVerseNumber();
//
//
//
//		if(start.compareTo(end) == 3) {
//			query += "-" + end.getChapter() + ":" + end.getVerseNumber();
//		}
//        else if(start.compareTo(end) == 1) {
//
//        }
//
//        switch(start.compareTo(end)) {
//            case 1:
//            case 2:
//                break;
//            case 3:
//                query += "-" + end.getChapter() + ":" + end.getVerseNumber();
//
//        }
//
//		//get webpage
//		Document doc = Jsoup.connect(query).get();
//		Elements passage = doc.select(".versetext");
//		flags = EnumSet.of(Flags.TEXT_NORMAL, Flags.PRINT_VERSE_NUMBER, Flags.NUMBER_DOT);
//
//		String passageText = "";
//
//		//parse webpage
//		for(Element element : passage) {
//			int versenum = Integer.parseInt(element.select(".versenum").text());
//			element.select(".versenum").remove();
//			element.select("a").remove();
//			passageText += /*" (" + versenum + ") " +*/ element.text() + " ";
//		}

//		setText(passageText);
        for(Verse item : verses) {
            item.retrieve();
        }
		return this;
	}
}
