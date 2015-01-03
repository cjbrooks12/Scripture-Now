package com.caseybrooks.scripturememory.test;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.caseybrooks.androidbibletools.data.Reference;
import com.caseybrooks.androidbibletools.enumeration.Book;

import java.text.ParseException;

public class ReferenceClassTest extends InstrumentationTestCase {

    public void testReferenceParser() throws Throwable {
        String[] references = new String[] {
                "John 3:16",
                "1 John 3:16",
                "Phili 4:11",
                "Eph 1:1-8",
                "Eph 1:1 through 8",
                "Eph 1:1 to 8",
                "Ecc 4:1, 4",
                "Ecc 4:1 and 4",
                "Gen 1:1-3"
        };

        Book[] refBooks = new Book[] {
                Book.John,
                Book.FirstJohn,
                Book.Philippians,
                Book.Ephesians,
                Book.Ephesians,
                Book.Ephesians,
                Book.Ecclesiastes,
                Book.Ecclesiastes,
                Book.Genesis
        };

        int[] refChapters = new int[] {
                3, 3, 4, 1, 1, 1, 4, 4, 1
        };

        int[][] refVerses = new int[][] {
                {16},
                {16},
                {11},
                {1, 2, 3, 4, 5, 6, 7, 8},
                {1, 2, 3, 4, 5, 6, 7, 8},
                {1, 2, 3, 4, 5, 6, 7, 8},
                {1, 4},
                {1, 4},
                {1, 2, 3}
        };

        for(int i = 0; i < references.length; i++) {
            try {
                Log.e("TEST REF PARSER", i + ":" + references[i]);
                Reference ref = new Reference(references[i]);

                assertEquals(refBooks[i], ref.book);
                assertEquals(refChapters[i], ref.chapter);
                assertEquals(refVerses[i].length, ref.verses.size());

                for(int j = 0; j < refVerses[i].length; j++) {
                    assertEquals(refVerses[i][j], (int)ref.verses.get(j));
                }
            }
            catch(ParseException e) {
                Log.e("TEST REF PARSER ERROR", "ERROR: " + i + ":" + references[i]);

                throw new Throwable("Raw Error in parsing reference", e);
            }
        }
    }

    //this test shows that for any book except Judges, Jude, Philippians, and
    //Philemon, I can successfully parse its name based on the first three letters
    //of the word that was unput. For Judges and Jude, I need a fourth letter to
    //determine which book it is, and I need 5 letters for Philippians and Philemon
    public void testBookNameLikeness() throws Throwable {
        String[] bookNames = Book.getList();

        for(int i = 0; i < bookNames.length; i++) {
            Book book;
            String pre, post;

            //test parsing the names based on the fewest letters of full name
            pre = bookNames[i].toLowerCase().trim().replaceAll("\\s", "");
            if(pre.equals("judges") || pre.equals("jude")) {
                book = Book.parseBook(pre.substring(0, 4));
            }
            else if(pre.equals("philemon") || pre.equals("philippians")) {
                book = Book.parseBook(pre.substring(0, 5));
            }
            else {
                book = Book.parseBook(pre.substring(0, 3));
            }
            post = book.getName().toLowerCase().trim().replaceAll("\\s", "");
            assertEquals(pre, post);

            //test parsing the names based on their code. we have already found
            //the book, so just get its code and parse
            pre = book.getCode().toLowerCase().trim().replaceAll("\\s", "");
            book = Book.parseBook(pre);
//
            post = book.getCode().toLowerCase().trim().replaceAll("\\s", "");
            assertEquals(pre, post);
        }
    }

    public void testHashCodes() throws Throwable {
        Reference ref1 = new Reference(Book.John, 3, 16, 17);
        Reference ref2 = new Reference("John 3:16-17");
        Reference ref3 = new Reference("John 3:16, 17");

        assertEquals(true, ref1.equals(ref2));
        assertEquals(true, ref2.equals(ref3));
        assertEquals(true, ref1.equals(ref3));

        assertEquals(ref1.hashCode(), ref2.hashCode());
        assertEquals(ref2.hashCode(), ref3.hashCode());
        assertEquals(ref1.hashCode(), ref3.hashCode());
    }

    public void testPrintingReferences() throws Throwable {
        String refStringManual1 = "John 3:16-19,     24, 27-29, 31, 33";
        Reference ref1 = new Reference(Book.John, 3,
                16, 17, 18, 19, 24, 27, 28, 29, 31, 33);

        String refStringManual2 = "Mark 1:1-7, 14, 19, 22, 29-32, 34-35";
        Reference ref2 = new Reference(Book.Mark, 1,
                1, 2, 3, 4, 5, 6, 7, 14, 19, 22, 29, 30, 31, 32, 34, 35);


        assertEquals(refStringManual1.replaceAll("\\s+", " "), ref1.toString());
        assertEquals(refStringManual2.replaceAll("\\s+", " "), ref2.toString());

        Reference ref3 = new Reference(ref1.toString());
        Reference ref4 = new Reference(ref2.toString());

        assertEquals(ref1.toString(), ref3.toString());
        assertEquals(ref2.toString(), ref4.toString());
    }

    public void testExtractVerse() throws Throwable {
        Reference ref = Reference.extractReference("Lets see if I can find John 3:16-18");

        assertNotNull(ref);
        assertEquals(Book.John, ref.book);
        assertEquals(3, ref.chapter);
        assertEquals(16, (int)ref.verses.get(0));
        assertEquals(17, (int)ref.verses.get(1));
        assertEquals(18, (int)ref.verses.get(2));
    }

    public void testExtractSharedVerse() {
        String youVersion = "http://bible.com/111/gen.3.1.niv Now the serpent was more crafty";
        Reference youVersionRef = Reference.extractReference(youVersion);
        assertNotNull(youVersionRef);
        assertEquals(Book.Genesis, youVersionRef.book);
        assertEquals(3, youVersionRef.chapter);
        assertEquals(1, (int)youVersionRef.verse);

        String faithlife = "\"Now the serpent was more crafty\"\n\n http://ref.ly/r/niv2011/Ge3.1 via the FaithLife";
        Reference faithlifeRef = Reference.extractReference(faithlife);
        assertNotNull(faithlifeRef);
        assertEquals(Book.Genesis, faithlifeRef.book);
        assertEquals(3, faithlifeRef.chapter);
        assertEquals(1, (int)faithlifeRef.verse);
    }
}
