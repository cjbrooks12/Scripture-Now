package com.caseybrooks.androidbibletools.search;

/** An API to make searching for Bible verses on the internet easy. This class
 *  accomplishes the goal of making it easy for developers to populate their app
 *  with Bible verses without needing to find a service and use it on their own.
 *  Users can find verses in two ways: by using the class as a builder, or
 *  searching by String.
 *
 *  String searches should follow the following format:
 *  [Book] [Start Chapter] : [Start Verse] - [End Chapter] : [End Verse]
 *
 *  At minimum, the book and chapter must be provided, and the
 *
 */
public class SearchVerse {
//
////Data Members
////------------------------------------------------------------------------------
//    private Version version;
//    private Passage verses;
//
//
//
//
//
//
//
//    //TODO: Include topical searching
//    public Verses retrieve() throws IOException {
//        String message = checkIfReadyToSearch();
//
//            return parseBibleStudyTools();
//
//    }
//
////BibleStudyTools.com
////------------------------------------------------------------------------------
//    /** Builds a String URL which is ges to a page from BibleStudyTools.com
//     *
//     * @return String URL to query BibleStudyTools.com
//     */
//    private String getBibleStudyToolsURL() {
//        String query = "http://www.biblestudytools.com/" +
//                version.getCode() + "/" +
//                book.getName().toLowerCase() + "/passage.aspx?q=" + book.getName().toLowerCase() + "+" +
//                startChapter + ":" + startVerse;
//
//        if(endChapter != 0 && endVerse != 0) {
//            query += "-" + endChapter + ":" + endVerse;
//        }
//        else if(endChapter == 0 && endVerse != 0) {
//            query += "-" + endVerse;
//        }
//
//        return query;
//    }
//
//    /** Go online to find verses from BibleStudyTools.com
//     *
//     * @return Verses list of all verses retrieved
//     * @throws java.io.IOException
//     */
//    private Verses parseBibleStudyTools() throws IOException {
//        Document doc = Jsoup.connect(getBibleStudyToolsURL()).get();
//
//        Elements passage = doc.select(".versetext");
//        verses = new Verses().setFlags(EnumSet.of(
//                Flags.TEXT_NORMAL,
//                Flags.PRINT_VERSE_NUMBER,
//                Flags.NUMBER_DOT));
//
//
//        for(Element element : passage) {
//            int versenum = Integer.parseInt(element.select(".versenum").text());
//            element.select(".versenum").remove();
//            element.select("a").remove();
//            String verseText = element.text();
//            Verse newVerse = new Verse(
//                    version,
//                    book,
//                    startChapter,
//                    versenum
//                    );
//            newVerse = newVerse.setText(verseText);
//            verses.add(newVerse);
//        }
//
//        return verses;
//    }
//
//    public Verses getVerses() {
//        return verses;
//    }
}