package com.caseybrooks.androidbibletools.basic;

import com.caseybrooks.androidbibletools.enumeration.Book;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Stack;

public class Reference {
    public final Book book;
    public final int chapter;
    public final int verse;
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
    }

    public Reference(String expression) throws ParseException {
        ts = new TokenStream(expression);
        Book book = book();
        if(book != null) {
            int chapter = chapter();
            if(chapter != 0) {
                ArrayList<Integer> verseList = verseList();

                if(verseList != null) {
                    this.book = book;
                    this.chapter = chapter;
                    Collections.sort(verseList);
                    this.verses = verseList;
                    this.verse = verseList.get(0);
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

    //chapter ::= [1...150]
    private int chapter() {
        Token a = ts.get();
        if(a != null && a.equals(Token.NUMBER) && a.getIntValue() >= 1 && a.getIntValue() <= 150) {
            return a.getIntValue();
        }
        else {
            ts.unget(a);
            return 0;
        }
    }

    //verseList ::= (nothing) | : number {, number} | : number - number
    private ArrayList<Integer> verseList() {
        ArrayList<Integer> verseList = new ArrayList<Integer>();
        Token a = ts.get();
        if(a != null && a.equals(Token.COLON)) {
            Token b = ts.get();
            if(b != null && b.equals(Token.NUMBER)) {
                verseList.add(b.getIntValue());
                Token c = ts.get();
                if(c != null && c.equals(Token.DASH)) {
                    Token d = ts.get();
                    if(d != null && d.equals(Token.NUMBER)) {
                        for(int i = b.getIntValue() + 1; i <= d.getIntValue(); i++) {
                            verseList.add(i);
                        }
                        return verseList;
                    }
                }
                if(c != null && c.equals(Token.COMMA)) {
                    Token d = ts.get();
                    if(d != null && d.equals(Token.NUMBER)) {
                        verseList.add(d.getIntValue());
                        return verseList;
                    }
                }
                else {
                    ts.unget(c);
                    return verseList;
                }
            }
        }
        else {
            ts.unget(a);
            return verseList;
        }

        return null;
    }

    private class Token {
        public static final int WORD = 0;
        public static final int NUMBER = 1;
        public static final int COLON = 2;
        public static final int COMMA = 3;
        public static final int DASH = 4;

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

    private class TokenStream {
        LinkedList<Character> chars;
        Stack<Token> ungetTokens;

        public TokenStream(String expression) {
            String toParse = expression.replaceAll("\\s", "");
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
    }

    @Override
    public String toString() {
        String refString = book.toString();
        refString += " " + chapter;
        refString += ":";

        if(verses.size() > 0) {
            refString += verses.get(0);
            for(int i = 1; i < verses.size(); i++) {
                refString += "," + verses.get(i);
            }
        }

        return refString;
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
