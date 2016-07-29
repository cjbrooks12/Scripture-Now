package com.caseybrooks.common.features.votd;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.caseybrooks.androidbibletools.data.OnResponseListener;
import com.caseybrooks.androidbibletools.providers.votd.VerseOfTheDay;
import com.caseybrooks.common.R;
import com.caseybrooks.common.app.DashboardFeature;
import com.caseybrooks.common.app.DashboardCardBase;

public class VerseOfTheDayCard extends DashboardCardBase {

    VerseOfTheDay verseOfTheDay;

    TextView reference;
    TextView text;

    public VerseOfTheDayCard(Context context) {
        super(context);

        initialize();
    }

    private void initialize() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.card_verse_of_the_day, null);
        addView(view);

        setTitle(getFeatureForView().getTitle());

        reference = (TextView) view.findViewById(R.id.reference);
        text = (TextView) view.findViewById(R.id.text);

        verseOfTheDay = new VerseOfTheDay();
        verseOfTheDay.download(new OnResponseListener() {
            @Override
            public void responseFinished(boolean b) {
                if(b) {
                    reference.setText(verseOfTheDay.getPassage().getReference().toString());
                    text.setText(verseOfTheDay.getPassage().getText());
                }
                else {
                    reference.setText("Cannot load verse");
                    text.setText("Check your internet connection and try again");
                }
            }
        });

        setMenuResource(R.menu.card_search_result);
    }

    @Override
    public DashboardFeature getFeatureForView() {
        return DashboardFeature.VerseOfTheDay;
    }
}
