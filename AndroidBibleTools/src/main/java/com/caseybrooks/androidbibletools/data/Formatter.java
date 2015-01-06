package com.caseybrooks.androidbibletools.data;

/** Class to format the text output of a Verse object. Rather than setting flags
 * on the Verse itself, set flags on this Formatter and assign the formatter to
 * the Verse. This makes it easier to have consistent output, because the same
 * Formatter can be assigned to multiple Verses
 */
public interface Formatter {
    //called before we begin formatting the actual verses.
    //i.e. to print the reference before all its text
    public String onPreFormat(Reference reference);

    //called when about to format the number for a verse
    //i.e. change how the numbers will be shown
    public String onFormatNumber(int verseNumber);

    //called when formatting the main text of a verse
    //i.e. print only first letters of words, random words, etc.
    public String onFormatText(String verseText);

    //called when we encounter a word that we want to handle differently
    //i.e. ensure 'Lord' is always 'LORD'
    public String onFormatSpecial(String special);

    //called when we have finished formatting one verse and are moving to the next
    //i.e. to insert a newline between all verses
    public String onFormatNewVerse();

    //called when we have finished all other formatting
    //i.e. to print the reference at the end of all text, or a URL, or copyright info
    public String onPostFormat();
}
