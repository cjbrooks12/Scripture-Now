package com.caseybrooks.androidbibletools.basic;

import com.caseybrooks.androidbibletools.data.Reference;
import com.caseybrooks.androidbibletools.enumeration.Book;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;

/** The simplest unit of data in this data structure. Each verse contains one
 *  and only one Bible verse, its corresponding Book, Chapter, and Verse Number,
 *  and the version this specific Verse is.
 *
 *  Verse objects are immutable. The necessary core information, the
 *  verse reference and its getText, cannot be modified once set, as the Verse should
 *  always point to the same Verse, and the getText should correspond directly to
 *  this verse, not anything the user wants. Should the user be unable to connect
 *  to the internet at the time of creating a Verse, they may manually set the
 *  getText in the constructor, but it cannot be modified after this.
 *
 *  The display flags and Version may be changed at will, so that the user may
 *  view the same verse in different translations and formats. Think of a Verse
 *  object as a reference to a particular Verse, not the getText of the verse. In
 *  this way, it makes sense that a Verse can be displayed in multiple versions
 *  and in different formats.
 */
public class Verse extends AbstractVerse {
//Data Members
//------------------------------------------------------------------------------
    //Data members that make up the actual verse
    protected String verseText;

//Constructors
//------------------------------------------------------------------------------
    public Verse(Reference reference) {
        super(reference);
    }

    public Verse(String reference) throws ParseException {
        super(reference);
    }

//Getters and Setters
//------------------------------------------------------------------------------
    //Verse Text is mutable, should be set when a user manually inputs a verse,
    //or when downloading the verse in a new Version.
    public Verse setText(String verseText) {
        this.verseText = verseText;
        return this;
    }

	public Verse next() {
		if(reference.verses.get(0) != reference.book.numVersesInChapter(reference.chapter)) {
            Reference nextRef = new Reference(reference.book, reference.chapter, reference.verses.get(0) + 1);
			return new Verse(nextRef);
		}
		else {
			if(reference.chapter != reference.book.numChapters()) {
                Reference nextRef = new Reference(reference.book, reference.chapter + 1, reference.verses.get(0));
                return new Verse(nextRef);
			}
			else {
				for(int i = 0; i < Book.values().length; i++) {
					if((reference.book == Book.values()[i]) && (i != Book.values().length - 1)) {
                        Reference nextRef = new Reference(Book.values()[i+1], 1, 1);
						return new Verse(nextRef);
					}
				}
                Reference nextRef = new Reference(Book.values()[0], 1, 1);
				return new Verse(nextRef);
			}
		}
	}

	public Verse previous() {
		if(reference.verses.get(0) != 1) {
            Reference previousRef = new Reference(reference.book, reference.chapter, reference.verses.get(0) - 1);
            return new Verse(previousRef);
		}
		else {
			if(reference.chapter != 1) {
                Reference previousRef = new Reference(reference.book, reference.chapter - 1, reference.book.numVersesInChapter(reference.chapter - 1));
                return new Verse(previousRef);
			}
			else {
				Book newBook;
				for(int i = 0; i < Book.values().length; i++) {
					if((reference.book == Book.values()[i]) && (i != 0)) {
						newBook = Book.values()[i-1];
                        Reference previousRef = new Reference(newBook, newBook.numChapters(), newBook.lastVerseInBook());
                        return new Verse(previousRef);
					}
				}
				newBook = Book.values()[Book.values().length - 1];
                Reference previousRef = new Reference(newBook, newBook.numChapters(), newBook.lastVerseInBook());
                return new Verse(previousRef);
			}
		}
	}

//Print the formatted String
//------------------------------------------------------------------------------
    @Override
    public String getText() {
        String text = "";

        text += formatter.onPreFormat(reference);
        text += formatter.onFormatNumber(reference.verses.get(0));
        text += formatter.onFormatText(verseText);
        text += formatter.onPostFormat();

        return text.trim();
    }

//Comparison methods for sorting
//------------------------------------------------------------------------------

    //Compares two Verses with respect to classical reference order
    //RETURN VALUES (negative indicates lhs is less than rhs)
    //
    //  0: Verses are equal, since they point to the same verse
    //  1: Verses are adjacent
    //  2: Verses are not adjacent, but are in the same chapter
    //  3: Verses are not adjacent, but are in different chapters of the same Book
    //  4: Verses are not adjacent, and aren't even in the same Book
    @Override
    public int compareTo(AbstractVerse verse) {
        Verse lhs = this;
        Verse rhs = (Verse) verse;

        //get the position of each book as an integer so we can work with it
        int aBook = -1, bBook = -1;
        for(int i = 0; i < Book.values().length; i++) {
            if(Book.values()[i] == lhs.reference.book) aBook = i;
            if(Book.values()[i] == rhs.reference.book) bBook = i;
        }

        if(aBook - bBook == 1) {
            if((lhs.reference.chapter == 1 && lhs.reference.verses.get(0) == 1) &&
               (rhs.reference.chapter == rhs.reference.book.numChapters() &&
                 (rhs.reference.verses.get(0) == rhs.reference.book.numVersesInChapter(rhs.reference.chapter)))) return 1;
            else return 4;
        }
        else if(aBook - bBook == -1) {
            if((rhs.reference.chapter == 1 && rhs.reference.verses.get(0) == 1) &&
               (lhs.reference.chapter == lhs.reference.book.numChapters() &&
                 (lhs.reference.verses.get(0) == lhs.reference.book.numVersesInChapter(lhs.reference.chapter)))) return -1;
            else return -4;
        }
        else if(aBook > bBook) return 4;
        else if(aBook < bBook) return -4;
        else {
            //same book
            if(lhs.reference.chapter - rhs.reference.chapter == 1) {
                if((lhs.reference.verses.get(0) == 1) &&
                   (rhs.reference.verses.get(0) == rhs.reference.book.numVersesInChapter(rhs.reference.chapter))) return 1;
                else return 3;
            }
            if(lhs.reference.chapter - rhs.reference.chapter == -1) {
                if((rhs.reference.verses.get(0) == 1) &&
                   (lhs.reference.verses.get(0) == lhs.reference.book.numVersesInChapter(lhs.reference.chapter))) return -1;
                else return -3;
            }
            else if(lhs.reference.chapter > rhs.reference.chapter) return 3;
            else if(lhs.reference.chapter < rhs.reference.chapter) return -3;
            else {
                //same chapter
                if(lhs.reference.verses.get(0) - rhs.reference.verses.get(0) == 1) return 1;
                else if(lhs.reference.verses.get(0) - rhs.reference.verses.get(0) == -1) return -1;
                else if(lhs.reference.verses.get(0) > rhs.reference.verses.get(0)) return 2;
                else if(lhs.reference.verses.get(0) < rhs.reference.verses.get(0)) return -2;
                else return 0; //lhs.reference.verses.get(0) == rhs.reference.verses.get(0)
            }
        }
    }

	@Override
    public boolean equals(AbstractVerse verse) {
        Verse lhs = this;
        Verse rhs = (Verse) verse;

        return (lhs.reference.book == rhs.reference.book) &&
               (lhs.reference.chapter == rhs.reference.chapter) &&
               (lhs.reference.verses.get(0) == rhs.reference.verses.get(0));
    }

//Retrieve verse from the internet
//------------------------------------------------------------------------------
    @Override
    public String getURL() {
        return "http://www.biblestudytools.com/" + version.getCode() + "/" +
            reference.book.getName().toLowerCase().trim().replaceAll(" ",  "-") +
            "/passage.aspx?q=" +
            reference.book.getName().toLowerCase().trim().replaceAll(" ",  "-") +
            "+" + reference.chapter + ":" + reference.verse;
    }

    @Override
	public Verse retrieve() throws IOException {
        Document doc = Jsoup.connect(getURL()).get();

		Elements passage = doc.select(".versetext");

		for(Element element : passage) {
			element.select(".versenum").remove();
			element.select("a").remove();
			verseText = element.text();
		}

		return this;
	}
}
