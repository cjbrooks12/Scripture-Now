package com.caseybrooks.common.features.prayers;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.caseybrooks.common.R;
import com.caseybrooks.common.databinding.DialogNewPrayerBinding;
import com.caseybrooks.common.app.DashboardCardBase;
import com.caseybrooks.common.app.DashboardFeature;

public class AddPrayerCard extends DashboardCardBase {
    PrayerModel prayerModel;

    public AddPrayerCard(Context context) {
        super(context);

        initialize();
    }

    private void initialize() {
        prayerModel = new PrayerModel(getActivityBase());

        DialogNewPrayerBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(getActivityBase()),
                R.layout.dialog_new_prayer,
                null, false);
        binding.setModel(prayerModel);
        View view = binding.getRoot();

        prayerModel.initializeBinding(binding);

        addView(view);

        setTitle(getFeatureForView().getTitle());

        setMenuResource(R.menu.dashboard_add_prayer);
    }

    @Override
    public boolean onOverflowMenuItemClick(MenuItem item) {
        if(item.getItemId() == R.id.save) {
            if(prayerModel.validate()) {
                prayerModel.saveToRealm();
                prayerModel.clear();
                Snackbar.make(getActivityBase().getCoordinatorLayout(), "Prayer saved", Snackbar.LENGTH_SHORT).show();
            }
        }

        return super.onOverflowMenuItemClick(item);
    }

    @Override
    public DashboardFeature getFeatureForView() {
        return DashboardFeature.AddPrayer;
    }
}
