package com.caseybrooks.androidbibletools.data;

import com.caseybrooks.androidbibletools.enumeration.Book;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Stack;

public class Reference implements Comparable<Reference> {
    public final Book book;
    public final int chapter;
    public final int verse;
    public final boolean isValid;
    public final ArrayList<Integer> verses;
    TokenStream ts;

//Parse the input string using recursive descent parsing
//------------------------------------------------------------------------------
    public Reference(Book book, int chapter, int... verses) {
        this.book = book;
        this.chapter = chapter;
        if(verses.length == 1) {
            this.verses = null;
            verse = verses[0];
        }
        else {
            verse = 0;
            this.verses = new ArrayList<Integer>();

            for (int i = 0; i < verses.length; i++) {
                this.verses.add(verses[i]);
            }
            Collections.sort(this.verses);
        }

        isValid = validate();
    }

    public Reference(String expression) throws ParseException {
        this(new TokenStream(expression));
    }

    private Reference(TokenStream ts) throws ParseException {
        String expression = ts.toString();
        this.ts = ts;
        Book book = book();
        if(book != null) {
            int chapter = chapter();
            if(chapter != 0) {
                ArrayList<Integer> verseList = verseList();

                if(verseList != null && verseList.size() > 0) {
                    this.book = book;
                    this.chapter = chapter;
                    Collections.sort(verseList);
                    this.verses = verseList;
                    this.verse = verseList.get(0);

                    isValid = validate();
                }
                else {
                    throw new ParseException("'" + expression + "' is not formatted correctly(verseList)", 3);
                }
            }
            else {
                throw new ParseException("'" + expression + "' is not formatted correctly(chapter)", 2);
            }
        }
        else {
            throw new ParseException("'" + expression + "' is not formatted correctly(book)", 1);
        }
    }

    public boolean validate() {
        if(chapter > book.numChapters() || chapter < 1) return false;
        if(verses != null && verses.size() > 0) {
            for (Integer i : verses) {
                if (i > book.numVersesInChapter(chapter) || i < 1) return false;
            }
        }
        else {
            if (verse > book.numVersesInChapter(chapter) || verse < 1) return false;
        }

        return true;
    }

    public static Reference extractReference(String reference) {
        TokenStream streamBase = new TokenStream(reference);
        TokenStream stream = new TokenStream(reference);

        while(stream.toString().length() > 0) {
            try {
                Reference ref = new Reference(stream);
                return ref;
            }
            catch(ParseException e) {
                streamBase.get();
                stream = new TokenStream(streamBase.toString());
                continue;
            }
        }

        return null;
    }

    //book ::= [123] WORD | WORD
    private Book book() {
        Token a = ts.get();
        if(a != null && a.equals(Token.NUMBER) && a.getIntValue() <= 3) {
            Token b = ts.get();
            if(b != null && b.equals(Token.WORD)) {
                return Book.parseBook(a.getIntValue() + " " + b.getStringValue());
            }
            else {
                ts.unget(b);
                ts.unget(a);
                return null;
            }
        }
        if(a != null && a.equals(Token.WORD)) {
            return Book.parseBook(a.getStringValue());
        }
        else {
            ts.unget(a);
            return null;
        }
    }

    //chapter ::= number
    private int chapter() {
        Token a = ts.get();
        if(a != null && a.equals(Token.NUMBER)) {
            return a.getIntValue();
        }
        else {
            ts.unget(a);
            return 0;
        }
    }

    //verse ::= number
    private int verse() {
        Token a = ts.get();
        if(a != null && a.equals(Token.NUMBER)) {
            return a.getIntValue();
        }
        else {
            ts.unget(a);
            return 0;
        }
    }

    //verseSequence ::= verse - verse
    private ArrayList<Integer> verseSequence() {
        int a = verse();
        if(a > 0) {
            Token b = ts.get();
            if(b != null && (b.equals(Token.DASH))) {
                int c = verse();
                if(c > 0) {
                    ArrayList<Integer> verses = new ArrayList<Integer>();
                    for(int i = a; i <= c; i++) {
                        verses.add(i);
                    }
                    return verses;
                }
                else {
                    ts.unget(b);
                    ts.unget(new Token(Token.NUMBER, a));
                }
            }
            else {
                ts.unget(b);
                ts.unget(new Token(Token.NUMBER, a));
            }
        }


        return null;
    }

    //verseList ::= : { verse | verseSequence {,} }
    private ArrayList<Integer> verseList() {
        ArrayList<Integer> verseList = new ArrayList<Integer>();
        Token a = ts.get();
        if(a != null && (a.equals(Token.COLON) || a.equals(Token.COMMA))) {
            while (true) {
                ArrayList<Integer> b = verseSequence();
                if (b != null && b.size() > 0) {
                    for(Integer i : b) {
                        if(!verseList.contains(i)) {
                            verseList.add(i);
                        }
                    }
                }
                else {
                    int c = verse();
                    if (c > 0 &&  !verseList.contains(c)) {
                        verseList.add(c);
                    }
                }

                Token comma = ts.get();
                if (comma != null && comma.equals(Token.COMMA)) {
                    continue;
                }
                else {
                    ts.unget(comma);
                    return verseList;
                }
            }
        }
        else {
            ts.unget(a);
            return null;
        }
    }

    private static class Token {
        public static final int WORD = 0;
        public static final int NUMBER = 1;
        public static final int COLON = 2;
        public static final int COMMA = 3;
        public static final int DASH = 4;
        public static final int WHITESPACE = 5;

        private int tokenType;
        private String stringValue;
        private int intValue;

        Token(int tokenType, String value) {
            this.tokenType = tokenType;
            this.stringValue = value;
        }

        Token(int tokenType, int value) {
            this.tokenType = tokenType;
            this.intValue = value;
        }

        public String getStringValue() { return stringValue; }
        public int getIntValue() { return intValue; }

        public boolean equals(int tokenType) {
            return this.tokenType == tokenType;
        }
    }

    private static class TokenStream {
        LinkedList<Character> chars;
        Stack<Token> ungetTokens;

        public TokenStream(String expression) {
            String toParse = expression.replaceAll("\\s+", "~");
            chars = new LinkedList<Character>();
            for(int i = 0; i < toParse.length(); i++) {
                chars.add(toParse.charAt(i));
            }
            ungetTokens = new Stack<Token>();
        }

        public Token get() {
            try {
                if (ungetTokens.size() > 0) {
                    return ungetTokens.pop();
                }
                else if (chars.size() > 0) {
                    char ch = chars.removeFirst();
                    String s;

                    switch (ch) {
                    case '~':
                        return get();
                    case ':':
                        return new Token(Token.COLON, ch);
                    case ',':
                        return new Token(Token.COMMA, ch);
                    case '-':
                        return new Token(Token.DASH, ch);
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        s = "";
                        s += ch;
                        while (chars.size() > 0 && chars.getFirst() != null &&
                                Character.isDigit(chars.getFirst())) {
                            s += chars.removeFirst();
                        }
                        return new Token(Token.NUMBER, Integer.parseInt(s));
                    default:
                        s = "";
                        s += ch;
                        while (chars.size() > 0 && chars.getFirst() != null &&
                                Character.isLetter(chars.getFirst())) {
                            s += chars.removeFirst();
                        }

                        if (s.equalsIgnoreCase("through")) {
                            return new Token(Token.DASH, s);
                        }
                        else if (s.equalsIgnoreCase("to")) {
                            return new Token(Token.DASH, s);
                        }
                        else if (s.equalsIgnoreCase("and")) {
                            return new Token(Token.COMMA, s);
                        }
                        else {
                            return new Token(Token.WORD, s);
                        }
                    }
                } else {
                    return null;
                }
            }
            catch(Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public void unget(Token token) {
            ungetTokens.push(token);
        }

        @Override
        public String toString() {
            String s = "";
            for(int i = 0; i < chars.size(); i++) {
                s += chars.get(i);
            }
            return s;
        }
    }

    @Override
    public String toString() {
        String refString = book.getName();
        refString += " " + chapter;
        refString += ":";

        if(verses.size() == 1) {
            refString += verse;
        }
        else {
            refString += verses.get(0);
            int lastVerse = verses.get(0);

            int i = 1;
            while(i < verses.size()) {
                if(verses.get(i) == lastVerse + 1) {
                    refString += "-";
                    while(true) {
                        if (i < verses.size() && verses.get(i) == lastVerse + 1) {
                            lastVerse++;
                            i++;
                        }
                        else {
                            refString += Integer.toString(lastVerse);
                            break;
                        }
                    }
                }
                else {
                    refString += ", " + verses.get(i);
                    lastVerse = verses.get(i);
                    i++;
                }
            }
        }

        return refString;
    }

    //Compares two Reference with respect to classical reference order, according to the first verse
    //RETURN VALUES (negative indicates lhs is less than rhs)
    //
    //  0: Verses are equal, since they point to the same verse
    //  1: Verses are adjacent
    //  2: Verses are not adjacent, but are in the same chapter
    //  3: Verses are not adjacent, but are in different chapters of the same Book
    //  4: Verses are not adjacent, and aren't even in the same Book
    @Override
    public int compareTo(Reference rhs) {
        Reference lhs = this;

        //get the position of each book as an integer so we can work with it
        int aBook = -1, bBook = -1;
        for(int i = 0; i < Book.values().length; i++) {
            if(Book.values()[i] == lhs.book) aBook = i;
            if(Book.values()[i] == rhs.book) bBook = i;
        }

        if(aBook - bBook == 1) {
            if((lhs.chapter == 1 && lhs.verses.get(0) == 1) &&
                    (rhs.chapter == rhs.book.numChapters() &&
                            (rhs.verses.get(0) == rhs.book.numVersesInChapter(rhs.chapter)))) return 1;
            else return 4;
        }
        else if(aBook - bBook == -1) {
            if((rhs.chapter == 1 && rhs.verses.get(0) == 1) &&
                    (lhs.chapter == lhs.book.numChapters() &&
                            (lhs.verses.get(0) == lhs.book.numVersesInChapter(lhs.chapter)))) return -1;
            else return -4;
        }
        else if(aBook > bBook) return 4;
        else if(aBook < bBook) return -4;
        else {
            //same book
            if(lhs.chapter - rhs.chapter == 1) {
                if((lhs.verses.get(0) == 1) &&
                        (rhs.verses.get(0) == rhs.book.numVersesInChapter(rhs.chapter))) return 1;
                else return 3;
            }
            if(lhs.chapter - rhs.chapter == -1) {
                if((rhs.verses.get(0) == 1) &&
                        (lhs.verses.get(0) == lhs.book.numVersesInChapter(lhs.chapter))) return -1;
                else return -3;
            }
            else if(lhs.chapter > rhs.chapter) return 3;
            else if(lhs.chapter < rhs.chapter) return -3;
            else {
                //same chapter
                if(lhs.verses.get(0) - rhs.verses.get(0) == 1) return 1;
                else if(lhs.verses.get(0) - rhs.verses.get(0) == -1) return -1;
                else if(lhs.verses.get(0) > rhs.verses.get(0)) return 2;
                else if(lhs.verses.get(0) < rhs.verses.get(0)) return -2;
                else return 0; //lhs.verses.get(0) == rhs.verses.get(0)
            }
        }
    }

    public boolean equals(Reference ref) {
        if(ref == null) return false;
        if(this.book != ref.book) return false;
        if(this.chapter != ref.chapter) return false;
        if(this.verses.size() != ref.verses.size()) return false;

        for(Integer i : this.verses) {
            if(!ref.verses.contains(i)) return false;
        }
        for(Integer i : ref.verses) {
            if(!this.verses.contains(i)) return false;
        }

        return true;
    }

    public int hashCode() {
        int result = book.hashCode();
        result = 31 * result + chapter;
        result = 31 * result + (verses != null ? verses.hashCode() : 0);
        return result;
    }
}
