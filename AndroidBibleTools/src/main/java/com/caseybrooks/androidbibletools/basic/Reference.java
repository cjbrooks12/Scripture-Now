package com.caseybrooks.androidbibletools.basic;

public class Reference {
    public Book book;
    public int chapter;
    public int[] verses;

    private String text;

    public Reference(String reference) {
        this.text = reference;

        this.book = Book.John;
        this.chapter = 3;
        this.verses = new int[] {16};
    }

    public Reference(Book book, int chapter, int... verses) {
        this.book = book;
        this.chapter = chapter;
        this.verses = verses;
    }

//Parse the input string using recursive descent parsing
//------------------------------------------------------------------------------
    public Reference parseString(String reference) {
        return new Reference(reference);
    }

    //parser methods
    private static class Parser {
        public Book parseBook() {
            return Book.John;
        }

        public int parseChapter() {
            return 3;
        }

        public int[] parseVerses() {
            return new int[] {16};
        }
    }


    //lexer methods
    private static class Tokenizer {
        private enum TokenType {
            NUMBER, //any number, can be a chapter or verse number, or start of numbered books
            WORD, //any word
            C_SEPARATOR, //chapter separator, the separator between the chapter and its verses
            V_SEPARATOR, //verse separator, the separator denoting a continuous list of verses (i.e 3-7)
            V_COMMA, //verse comma, the separator denoting non-continuous verses, (i.e. 3, 7)
        }

        private class Token {
            public TokenType type;
            public String value;
        }

        String expression;

        public Tokenizer(String expression) {
            this.expression = expression;
        }

        List<Token> tokenize() {
            for(int i = 0; i < expression.length; i++) {

            }
        }
    }
}
