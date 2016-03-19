package com.caseybrooks.common.app;

public enum WordStyle {
    Dashes(0), Letters(1), DashedLetters(2), Dimmed(3), Missing(4);

    int id;

    WordStyle(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String convert(String word) {
        if(this == Dashes) {
            return word.replaceAll("\\w", "_");
        }
        else if(this == Letters) {
            return word.toUpperCase().replaceAll("(\\w)(\\w*)", "($1)");
        }
        else if(this == DashedLetters) {
            return word.toUpperCase().replaceAll("(\\B\\w)", "_");
        }
        else if(this == Dimmed) {
            return word;
        }
        else if(this == Missing) {
            return word.replaceAll("\\w", " ");
        }
        else {
            return word;
        }
    }

    public static WordStyle getWordStyleFromId(int id) {
        for(WordStyle style : WordStyle.values()) {
            if(style.getId() == id) {
                return style;
            }
        }

        throw new EnumConstantNotPresentException(WordStyle.class, Integer.toString(id));
    }
}
