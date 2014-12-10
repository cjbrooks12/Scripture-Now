package com.caseybrooks.androidbibletools.basic;

import android.util.Log;

import com.caseybrooks.androidbibletools.enumeration.Book;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

public class Reference {
    public Book book;
    public int chapter;
    public ArrayList<Integer> verses;

    public Reference(Book book, int chapter, int... verses) {
        this.book = book;
        this.chapter = chapter;
        //this.verses = verses;
    }

//Parse the input string using recursive descent parsing
//------------------------------------------------------------------------------
    TokenStream ts;
    boolean matchAll;

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
                    this.verses = verseList;
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
            if(b != null && b.equals(Token.WHITESPACE)) {
                Token c = ts.get();
                if(c != null && c.equals(Token.WORD)) {
                    return Book.fromString(a.getIntValue() + " " + c.getStringValue());
                }
                else {
                    ts.unget(c);
                    ts.unget(b);
                    ts.unget(a);
                    return null;
                }
            }
            else {
                ts.unget(b);
                ts.unget(a);
                return null;
            }
        }
        if(a != null && a.equals(Token.WORD)) {
            return Book.fromString(a.getStringValue());
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
                        for(int i = b.getIntValue(); i <= d.getIntValue(); i++) {
                            verseList.add(i);
                        }
                        return verseList;
                    }
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

        public int getType() { return tokenType; }
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
            chars = new LinkedList<Character>();
            for(int i = 0; i < expression.length(); i++) {
                chars.add(expression.charAt(i));
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
                            Log.e("RETURN TOKEN", "_" + ch);
                            return new Token(Token.COLON, ch);
                        case ',':
                            Log.e("RETURN TOKEN", "_" + ch);
                            return new Token(Token.COMMA, ch);
                        case '-':
                            Log.e("RETURN TOKEN", "_" + ch);
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
                            while (chars.getFirst() != null &&
                                    Character.isDigit(chars.getFirst())) {
                                s += chars.removeFirst();
                            }
                            Log.e("RETURN TOKEN", "_" + Integer.parseInt(s));
                            return new Token(Token.NUMBER, Integer.parseInt(s));
                        default:
                            //ignore all whitespace at this point
                            if (Character.isWhitespace(ch)) {
                                s = "";
                                s += ch;
                                while (chars.getFirst() != null &&
                                        Character.isWhitespace(chars.getFirst())) {
                                    s += chars.removeFirst();
                                }
                            }

                            s = "";
                            s += ch;
                            while (chars.getFirst() != null &&
                                    Character.isLetter(chars.getFirst())) {
                                s += chars.removeFirst();
                            }

                            if (s.equalsIgnoreCase("through")) {
                                Log.e("RETURN TOKEN", "_" + s);
                                return new Token(Token.DASH, s);
                            } else if (s.equalsIgnoreCase("to")) {
                                Log.e("RETURN TOKEN", "_" + s);
                                return new Token(Token.DASH, s);
                            } else if (s.equalsIgnoreCase("and")) {
                                Log.e("RETURN TOKEN", "_" + s);
                                return new Token(Token.COMMA, s);
                            } else {
                                Log.e("RETURN TOKEN", "_" + s);
                                return new Token(Token.WORD, s);
                            }
                    }
                } else {
                    Log.e("RETURN TOKEN", "none remaining: " + chars.size());
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
}
