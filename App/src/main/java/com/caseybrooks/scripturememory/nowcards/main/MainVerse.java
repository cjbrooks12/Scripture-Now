package com.caseybrooks.scripturememory.nowcards.main;

import android.content.Context;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.defaults.DefaultFormatter;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.VerseDB;

public class MainVerse {
    private Passage passage;
    private Context context;

    public MainVerse(Context context) {
        VerseDB db = new VerseDB(context).open();
        passage = db.getVerse(MetaSettings.getVerseId(context));
        db.close();
    }

    public Passage getNormalPassage() {
        passage.setFormatter(new DefaultFormatter.Normal());
        return passage;
    }

    public Passage getFormattedPassage() {
        switch (MetaSettings.getVerseDisplayMode(context)) {
            case 0: passage.setFormatter(new DefaultFormatter.Normal()); break;
            case 1: passage.setFormatter(new DefaultFormatter.Dashes()); break;
            case 2: passage.setFormatter(new DefaultFormatter.FirstLetters()); break;
            case 3: passage.setFormatter(new DefaultFormatter.DashedLetter()); break;
            case 4: passage.setFormatter(new DefaultFormatter.RandomWords(MetaSettings.getRandomnessLevel(context))); break;
            default: passage.setFormatter(new DefaultFormatter.Normal()); break;
        }

        return passage;
    }

    public void getNextVerse() {

    }
}
