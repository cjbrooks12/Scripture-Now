package com.caseybrooks.common.features.memoryverse;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.caseybrooks.androidbibletools.basic.Reference;
import com.caseybrooks.androidbibletools.providers.abs.ABSPassage;
import com.caseybrooks.androidbibletools.widget.VerseView;
import com.caseybrooks.common.R;
import com.caseybrooks.common.app.dashboard.DashboardCardBase;
import com.caseybrooks.common.app.dashboard.DashboardFeature;

public class MemoryVerseCard extends DashboardCardBase {

    TextView reference;
    VerseView verse;

    public MemoryVerseCard(Context context) {
        super(context);

        initialize();
    }

    private void initialize() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.memory_verse_card, null);
        addView(view);

        setTitle(getFeatureForView().getTitle());

        ABSPassage passage = new ABSPassage(new Reference.Builder().parseReference("Galatians 2:19-21").create());

        reference = (TextView) view.findViewById(R.id.reference);
        reference.setText(passage.getReference().toString());

        verse = (VerseView) view.findViewById(R.id.verse);
        verse.setText(passage.getText());

        setMenuResource(R.menu.card_search_result);
    }

    @Override
    public DashboardFeature getFeatureForView() {
        return DashboardFeature.MemoryVerse;
    }
}
