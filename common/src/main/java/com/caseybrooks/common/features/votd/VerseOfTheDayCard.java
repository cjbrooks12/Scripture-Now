package com.caseybrooks.common.features.votd;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.caseybrooks.androidbibletools.data.OnResponseListener;
import com.caseybrooks.androidbibletools.widget.VerseView;
import com.caseybrooks.common.R;
import com.caseybrooks.common.app.features.dashboard.DashboardCardBase;
import com.caseybrooks.common.app.features.dashboard.DashboardCardConfiguration;

public class VerseOfTheDayCard extends DashboardCardBase {

    VerseOfTheDayService service;

    TextView reference;
    VerseView text;

    public VerseOfTheDayCard(Context context) {
        super(context);

        initialize();
    }

    private void initialize() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.votd_dashcard, null);
        addView(view);

        setTitle(getConfiguration().getTitle());

        reference = (TextView) view.findViewById(R.id.reference);
        text = (VerseView) view.findViewById(R.id.verse);

        service = new VerseOfTheDayService();
        service.run(new OnResponseListener() {
            @Override
            public void responseFinished(boolean b) {
                if(b) {
                    reference.setText(service.getVerseOfTheDay().getPassage().getReference().toString());
                    text.setText(service.getVerseOfTheDay().getPassage().getText());
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
    public DashboardCardConfiguration getConfiguration() {
        return configuration;
    }

    public static DashboardCardConfiguration configuration = new DashboardCardConfiguration() {
        public String getTitle() {
            return "Verse of the Day";
        }

        public Class<? extends DashboardCardBase> getCardClass() {
            return VerseOfTheDayCard.class;
        }

        public int getRelativePosition() {
            return 100;
        }
    };
}
