package com.caseybrooks.scripturememory.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.VerseDB;

public class NavDrawerHeader extends RelativeLayout {
    Context context;
    TextView reference;
    TextView verseText;

    Passage passage;

    public NavDrawerHeader(Context context) {
        super(context);
        this.context = context;
        init(null, 0);
    }

    public NavDrawerHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs, 0);
    }

    public NavDrawerHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.nav_drawer_header, this);

        reference = (TextView) findViewById(R.id.navHeaderRef);
        verseText = (TextView) findViewById(R.id.navHeaderText);

        VerseDB db = new VerseDB(context).open();
        int id = MetaSettings.getVerseId(context);
        passage = db.getVerse(id);
        db.close();

        reference.setText(passage.getReference().toString());
        verseText.setText(passage.getText());
    }

    public void refresh() {
        VerseDB db = new VerseDB(context).open();
        int id = MetaSettings.getVerseId(context);
        passage = db.getVerse(id);
        db.close();

        reference.setText(passage.getReference().toString());
        verseText.setText(passage.getText());
    }
}
