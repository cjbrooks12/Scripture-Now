package com.caseybrooks.scripturememory.test;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.caseybrooks.androidbibletools.basic.Reference;
import com.caseybrooks.androidbibletools.enumeration.Book;

import java.text.ParseException;

public class AndroidTest extends InstrumentationTestCase {

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
}
