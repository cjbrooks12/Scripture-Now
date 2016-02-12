package com.caseybrooks.common.dashboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.caseybrooks.androidbibletools.ABT;
import com.caseybrooks.androidbibletools.basic.Bible;
import com.caseybrooks.androidbibletools.basic.Reference;
import com.caseybrooks.androidbibletools.data.Downloadable;
import com.caseybrooks.androidbibletools.data.OnResponseListener;
import com.caseybrooks.androidbibletools.providers.abs.ABSPassage;
import com.caseybrooks.androidbibletools.widget.VerseView;
import com.caseybrooks.common.R;
import com.caseybrooks.common.app.ActivityBase;
import com.caseybrooks.common.app.Util;

public class SearchResultCard extends FrameLayout {
    String reference;

    Bible bible;
    boolean searchIsReady;
    boolean searchPending;

    TextView searchReference;
    VerseView searchText;

    public SearchResultCard(Context context) {
        super(context);

        initialize(null);
    }

    public SearchResultCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(attrs);
    }

    public SearchResultCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initialize(attrs);
    }

    private void initialize(AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.card_search_result, this);

        searchReference = (TextView) findViewById(R.id.search_reference);
        searchText = (VerseView) findViewById(R.id.search_text);

        bible = ABT.getInstance(getContext()).getSelectedBible(null);

        if(bible instanceof Downloadable) {
            searchIsReady = false;
            ((Downloadable) bible).download(new OnResponseListener() {
                @Override
                public void responseFinished() {
                    searchIsReady = true;
                    if(searchPending) {
                        searchReference();
                    }
                }
            });
        }
        else {
            searchIsReady = true;
        }
    }

    public void setReference(String reference) {
        this.reference = reference;
        if(searchIsReady) {
            searchReference();
        }
        else {
            searchPending = true;
        }
    }

    public void searchReference() {
        Reference.Builder builder = new Reference.Builder();
        builder.setBible(bible);
        builder.setFlag(Reference.Builder.PREVENT_AUTO_ADD_VERSES_FLAG);
        builder.parseReference(reference);

        final Reference ref = builder.create();

        if(builder.checkFlag(Reference.Builder.PARSE_SUCCESS)) {
            ((ActivityBase) getContext()).setActivityProgress(-1);

            final ABSPassage passage = new ABSPassage(ref);
            passage.download(new OnResponseListener() {
                @Override
                public void responseFinished() {
                    searchReference.setText(ref.toString());
                    searchText.setText(passage.getFormattedText());
                    ((ActivityBase) getContext()).setActivityProgress(0);
                }
            });
        }
        else {
            searchReference.setText(Util.formatString("Reference not found"));
            searchText.setText(Util.formatString("'{0}' could not be found in the selected bible '{1}'. Do you want to add it anyway with your own text?", ref.toString(), bible.getAbbreviation()));
        }
        searchPending = false;
    }
}
