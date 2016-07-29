package com.caseybrooks.common.features.joshuaproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.caseybrooks.androidbibletools.data.OnResponseListener;
import com.caseybrooks.androidbibletools.providers.joshuaproject.JoshuaProject;
import com.caseybrooks.common.R;
import com.caseybrooks.common.app.DashboardCardBase;
import com.caseybrooks.common.app.DashboardFeature;
import com.caseybrooks.common.util.Util;

public class JoshuaProjectCard extends DashboardCardBase {

    JoshuaProject joshuaProject;

    ImageView pg_image;
    TextView pg_name;
    TextView pg_info;

    public JoshuaProjectCard(Context context) {
        super(context);

        initialize();
    }

    private void initialize() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.card_joshua_project, null);
        addView(view);

        setTitle(getFeatureForView().getTitle());

        pg_image = (ImageView) view.findViewById(R.id.pg_image);
        pg_name = (TextView) view.findViewById(R.id.pg_name);
        pg_info = (TextView) view.findViewById(R.id.pg_info);

        joshuaProject  = new JoshuaProject();
        joshuaProject.download(new OnResponseListener() {
            @Override
            public void responseFinished(boolean b) {
                if(b) {
                    pg_name.setText(Util.formatString("{0} of {1}",
                            joshuaProject.getPeopleNameInCountry(),
                            joshuaProject.getCountry()));

                    pg_info.setText(Util.formatString(
                            "Population: {0}\nLanguage: {1}\nReligion: {2}\nStatus: {3}",
                            joshuaProject.getPopulation(),
                            joshuaProject.getLanguage(),
                            joshuaProject.getReligion(),
                            joshuaProject.getStatus()
                    ));

                    pg_image.setImageBitmap(joshuaProject.getPhoto());
                }
            }
        });


        setMenuResource(R.menu.card_search_result);
    }

    @Override
    public DashboardFeature getFeatureForView() {
        return DashboardFeature.JoshuaProjectPrayer;
    }
}
