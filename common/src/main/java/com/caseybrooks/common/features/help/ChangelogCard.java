package com.caseybrooks.common.features.help;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.DashboardFeature;
import com.caseybrooks.common.app.DashboardCardBase;

public class ChangelogCard extends DashboardCardBase {

    TextView content;

    public ChangelogCard(Context context) {
        super(context);

        initialize();
    }

    private void initialize() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.card_search_result, null);
        addView(view);

        setTitle(getFeatureForView().getTitle());

        content = (TextView) view.findViewById(R.id.search_reference);
        content.setText(getFeatureForView().getTitle());

        setMenuResource(R.menu.card_search_result);
    }

    @Override
    public DashboardFeature getFeatureForView() {
        return DashboardFeature.Changelog;
    }
}
