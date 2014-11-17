package com.caseybrooks.androidbibletools.basic;

import com.caseybrooks.androidbibletools.enumeration.Book;
import com.caseybrooks.androidbibletools.enumeration.Flags;
import com.caseybrooks.androidbibletools.enumeration.Version;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private final Book book;
    private final int chapter, verseNumber;
    private String verseText;

	private static Pattern oneVerse = Pattern.compile("((\\d\\s*)?\\w+)\\s*(\\d+)\\W+(\\d+)");

	//Constructors
//------------------------------------------------------------------------------
    public Verse(Book book, int chapter, int verseNumber) {
        super();

        if(chapter > book.lastChapter() || chapter <= 0)
			throw new IllegalArgumentException("Verse does not exist: chapter out of range: " + book.getName() + " " + chapter + ":" + verseNumber);
		if(verseNumber > book.verseInChapterCount(chapter) || verseNumber <= 0)
			throw new IllegalArgumentException("Verse does not exist: verse number out of range: " + book.getName() + " " + chapter + ":" + verseNumber);

		this.book = book;
		this.chapter = chapter;
		this.verseNumber = verseNumber;
    }

    public Verse(Version version, Book book, int chapter, int verseNumber) {
        this(book, chapter, verseNumber);

		this.version = version;
    }

    public Verse(String reference) {
		super();
        Matcher m = oneVerse.matcher(reference);

        if(m.matches()) {
            this.book = Book.fromString(m.group(1));
            this.chapter = Integer.parseInt(m.group(3));
            this.verseNumber = Integer.parseInt(m.group(4));

            if(book == null)
                throw new IllegalArgumentException("Verse does not exist: Book '" + m.group(1) + "' not found in " + reference);
            if(chapter > book.lastChapter() || chapter <= 0)
				throw new IllegalArgumentException("Verse does not exist: chapter out of range: " + reference);
			if(verseNumber > book.verseInChapterCount(chapter) || verseNumber <= 0)
				throw new IllegalArgumentException("Verse does not exist: verse number out of range: " + reference);

        }
        else throw new IllegalArgumentException("Verse does not exist: String not formatted properly, cannot parse '" + reference + "'");
    }

    public Verse(Version version, String reference) {
        this(reference);
		this.version = version;
    }

//Getters and Setters
//------------------------------------------------------------------------------
    //Book is immutable
    public Book getBook() {
        return book;
    }

    public Verse setBook(Book book) {
        Verse newVerse = new Verse(book, chapter, verseNumber);
        newVerse.setVersion(version);
                newVerse.setText(verseText)
                .setFlags(flags)
                .setId(id)
                .setTags(getTags());

        return newVerse;
    }

    //Chapter is immutable
    public int getChapter() {
        return chapter;
    }

    public Verse setChapter(int chapter) {
        Verse newVerse = new Verse(book, chapter, verseNumber);
        newVerse.setVersion(version);
                newVerse.setText(verseText)
                .setFlags(flags)
                .setId(id)
                .setTags(getTags());

        return newVerse;
    }

    //Verse Number is immutable
    public int getVerseNumber() {
        return verseNumber;
    }

    public Verse setVerseNumber(int verseNumber) {
        Verse newVerse = new Verse(book, chapter, verseNumber);
        newVerse.setVersion(version);
                newVerse.setText(verseText)
                .setFlags(flags)
                .setId(id)
                .setTags(getTags());
        return newVerse;
    }

    //Verse Text is mutable, should be set when a user manually inputs a verse,
    //or when downloading the verse in a new Version.
    public Verse setText(String verseText) {
        this.verseText = verseText;
        return this;
    }

//Auxiliary helper functions
    public static Verse fromString(String reference) {
        return new Verse(reference);
    }

	public Verse next() {
		if(verseNumber != book.verseInChapterCount(chapter)) {
			return new Verse(version, book, chapter, verseNumber + 1);
		}
		else {
			if(chapter != book.chapterCount()) {
				return new Verse(version, book, chapter + 1, 1);
			}
			else {
				for(int i = 0; i < Book.values().length; i++) {
					if((book == Book.values()[i]) && (i != Book.values().length - 1)) {
						return new Verse(version, Book.values()[i+1], 1, 1);
					}
				}
				return new Verse(version, Book.values()[0], 1, 1);
			}
		}
	}

	public Verse previous() {
		if(verseNumber != 1) {
			return new Verse(version, book, chapter, verseNumber - 1);
		}
		else {
			if(chapter != 1) {
				return new Verse(version, book, chapter - 1, book.verseInChapterCount(chapter - 1));
			}
			else {
				Book newBook;
				for(int i = 0; i < Book.values().length; i++) {
					if((book == Book.values()[i]) && (i != 0)) {
						newBook = Book.values()[i-1];
						return new Verse(version, newBook,
										newBook.lastChapter(),
										newBook.lastVerse());
					}
				}
				newBook = Book.values()[Book.values().length - 1];
				return new Verse(version, newBook,
										newBook.lastChapter(),
										newBook.lastVerse());
			}
		}
	}

//Print the formatted String
//------------------------------------------------------------------------------
	@Override
	public String getReference() {
        return book.getName() + " " + chapter + ":" + verseNumber;
    }

	@Override
    public String getText() {
        String text = "";

        if(flags.contains(Flags.PRINT_VERSE_NUMBER)) {
            if(flags.contains(Flags.NUMBER_PLAIN))
                text += verseNumber + " ";
            else if(flags.contains(Flags.NUMBER_DOT))
                text += verseNumber + ". ";
            else if(flags.contains(Flags.NUMBER_PARENTHESIS))
                text += verseNumber + ") ";
            else if(flags.contains(Flags.NUMBER_DOUBLE_PARENTHESIS))
                text += "(" + verseNumber + ") ";
        }

        //Will print only the first flag that is set, or normal if none are set
        if(flags.contains(Flags.TEXT_NORMAL)) {
            text += verseText + " ";
        }
        else if(flags.contains(Flags.TEXT_DASHES)) {
            text += verseText.replaceAll("\\w", "_") + " ";
        }
        else if(flags.contains(Flags.TEXT_LETTERS)) {
            text += verseText.toUpperCase().replaceAll("(\\w)(\\w*)", "$1 ") + " ";
        }
        else if(flags.contains(Flags.TEXT_DASHED_LETTERS)) {
            text += verseText.toUpperCase().replaceAll("(\\B\\w)", "_") + " ";
        }
        else { //if no flags are given, print out normal
            text += verseText + " ";
        }

        if(flags.contains(Flags.PRINT_NEWLINE)) {
            text += "\n";
        }
        return text;
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
            if(Book.values()[i] == lhs.getBook()) aBook = i;
            if(Book.values()[i] == rhs.getBook()) bBook = i;
        }

        if(aBook - bBook == 1) {
            if((lhs.getChapter() == 1 && lhs.getVerseNumber() == 1) &&
               (rhs.getChapter() == rhs.getBook().chapterCount() &&
                 (rhs.getVerseNumber() == rhs.getBook().verseInChapterCount(rhs.getChapter())))) return 1;
            else return 4;
        }
        else if(aBook - bBook == -1) {
            if((rhs.getChapter() == 1 && rhs.getVerseNumber() == 1) &&
               (lhs.getChapter() == lhs.getBook().chapterCount() &&
                 (lhs.getVerseNumber() == lhs.getBook().verseInChapterCount(lhs.getChapter())))) return -1;
            else return -4;
        }
        else if(aBook > bBook) return 4;
        else if(aBook < bBook) return -4;
        else {
            //same book
            if(lhs.getChapter() - rhs.getChapter() == 1) {
                if((lhs.getVerseNumber() == 1) &&
                   (rhs.getVerseNumber() == rhs.getBook().verseInChapterCount(rhs.getChapter()))) return 1;
                else return 3;
            }
            if(lhs.getChapter() - rhs.getChapter() == -1) {
                if((rhs.getVerseNumber() == 1) &&
                   (lhs.getVerseNumber() == lhs.getBook().verseInChapterCount(lhs.getChapter()))) return -1;
                else return -3;
            }
            else if(lhs.getChapter() > rhs.getChapter()) return 3;
            else if(lhs.getChapter() < rhs.getChapter()) return -3;
            else {
                //same chapter
                if(lhs.getVerseNumber() - rhs.getVerseNumber() == 1) return 1;
                else if(lhs.getVerseNumber() - rhs.getVerseNumber() == -1) return -1;
                else if(lhs.getVerseNumber() > rhs.getVerseNumber()) return 2;
                else if(lhs.getVerseNumber() < rhs.getVerseNumber()) return -2;
                else return 0; //lhs.getVerseNumber() == rhs.getVerseNumber()
            }
        }
    }

	@Override
    public boolean equals(AbstractVerse verse) {
        Verse lhs = this;
        Verse rhs = (Verse) verse;

        return (lhs.getBook() == rhs.getBook()) &&
               (lhs.getChapter() == rhs.getChapter()) &&
               (lhs.getVerseNumber() == rhs.getVerseNumber());
    }

//Retrieve verse from the internet
//------------------------------------------------------------------------------
	@Override
	public Verse retrieve() throws IOException{
		String query = "http://www.biblestudytools.com/" +
							   version.getCode() + "/" +
							   book.getName().toLowerCase().replaceAll(" ", "-") + "/" +
							   chapter + "-" + verseNumber + ".html";

		Document doc = Jsoup.connect(query).get();

		Elements passage = doc.select(".versetext");
		flags = EnumSet.of(Flags.TEXT_NORMAL, Flags.PRINT_VERSE_NUMBER, Flags.NUMBER_DOT);

		for(Element element : passage) {
			element.select(".versenum").remove();
			element.select("a").remove();
			verseText = element.text();
		}

		return this;
	}
}
