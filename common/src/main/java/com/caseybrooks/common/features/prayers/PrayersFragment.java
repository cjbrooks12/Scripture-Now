package com.caseybrooks.common.features.prayers;

import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.activity.ActivityBase;
import com.caseybrooks.common.app.fragment.AppFeature;
import com.caseybrooks.common.app.fragment.FragmentBase;
import com.caseybrooks.common.databinding.DialogNewPrayerBinding;
import com.caseybrooks.common.databinding.DialogPrayerScheduleBinding;
import com.caseybrooks.common.util.CancelDialogAction;
import com.caseybrooks.common.util.Util;
import com.caseybrooks.common.widget.CardView;
import com.caseybrooks.common.widget.MenuWidget;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class PrayersFragment extends FragmentBase {
    public static PrayersFragment newInstance() {
        PrayersFragment fragment = new PrayersFragment();
        Bundle data = new Bundle();
        fragment.setArguments(data);
        return fragment;
    }

    RecyclerView realmRecyclerView;
    PrayerAdapter adapter;
    RealmResults<RealmPrayer> prayerList;

    PrayerModel prayerModel;
    PrayerSchedulerModel prayerSchedulerModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_prayers, container, false);

        realmRecyclerView = (RecyclerView) view.findViewById(R.id.realm_prayer_list);
        realmRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        prayerList = ActivityBase.getRealm(getContext()).where(RealmPrayer.class).findAllSorted("title");
        adapter = new PrayerAdapter();
        realmRecyclerView.setAdapter(adapter);

        prayerModel = new PrayerModel(getActivityBase());

        return view;
    }

    @Override
    public Pair<AppFeature, Integer> getFeatureForFragment() {
        return new Pair<>(AppFeature.Prayers, 0);
    }

    @Override
    public void onPause() {
        super.onPause();
        ActivityBase.getRealm(getContext()).removeChangeListener(prayerRealmListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActivityBase.getRealm(getContext()).addChangeListener(prayerRealmListener);
    }

//Settings Dialog
//--------------------------------------------------------------------------------------------------

    public void newPrayer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivityBase());
        builder.setTitle("Add Prayer Request");

        prayerModel = new PrayerModel(getActivityBase());

        DialogNewPrayerBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(getActivityBase()),
                R.layout.dialog_new_prayer,
                null, false);
        binding.setModel(prayerModel);

        View view = binding.getRoot();

        prayerModel.initializeBinding(binding);

        builder.setView(view);
        builder.setNegativeButton("Close", new CancelDialogAction());
        builder.setPositiveButton("Save", null);

        final AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if(prayerModel.validate()) {
                            prayerModel.saveToRealm();
                            dialog.dismiss();
                        }
                    }
                });
            }
        });

        alertDialog.show();
    }

    public void editPrayer(RealmPrayer prayer) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivityBase());
        builder.setTitle("Edit Prayer Request");

        prayerModel = new PrayerModel(getActivityBase());

        DialogNewPrayerBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(getActivityBase()),
                R.layout.dialog_new_prayer,
                null, false);
        binding.setModel(prayerModel);
        prayerModel.modifyPrayer(prayer);

        View view = binding.getRoot();
        prayerModel.initializeBinding(binding);

        builder.setView(view);
        builder.setNegativeButton("Close", new CancelDialogAction());
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(prayerModel.validate()) {
                    prayerModel.saveToRealm();
                }
            }
        });

        builder.create().show();
    }

    public void deletePrayer(final RealmPrayer prayer) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivityBase());
        builder.setTitle("Delete Prayer");
        builder.setMessage(Util.formatString("Delete '{0}'? This cannot be undone.", prayer.getTitle()));
        builder.setNegativeButton("Cancel", new CancelDialogAction());
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                prayerModel.modifyPrayer(prayer);
                prayerModel.removeFromRealm();
            }
        });

        builder.create().show();
    }

    public void setupSchedule() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivityBase());
        builder.setTitle("Prayer Schedule");

        prayerSchedulerModel = new PrayerSchedulerModel(getContext());

        DialogPrayerScheduleBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(getActivityBase()),
                R.layout.dialog_prayer_schedule,
                null, false);
        binding.setModel(prayerSchedulerModel);

        View view = binding.getRoot();

        prayerSchedulerModel.initializeBinding(binding);

        builder.setView(view);
        builder.setNegativeButton("Close", new CancelDialogAction());

        builder.create().show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_prayers_fragment, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.setupSchedule) {
            setupSchedule();
            return true;
        }
        else {
            return false;
        }
    }

//Realm prayer list
//--------------------------------------------------------------------------------------------------

    RealmChangeListener prayerRealmListener = new RealmChangeListener() {
        public void onChange() {
            if(adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    };

    public class PrayerViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView description;
        public MenuWidget menuWidget;

        public TextView dayOfWeek;
        public TextView createdAt;
        public TextView updatedAt;

        public PrayerViewHolder(View itemView) {
            super(itemView);
            this.title = (TextView) itemView.findViewById(R.id.title);
            this.description = (TextView) itemView.findViewById(R.id.description);
            this.menuWidget = (MenuWidget) itemView.findViewById(R.id.menu_widget);
            this.dayOfWeek = (TextView) itemView.findViewById(R.id.dayOfWeek);
            this.createdAt = (TextView) itemView.findViewById(R.id.createdAt);
            this.updatedAt = (TextView) itemView.findViewById(R.id.updatedAt);
        }

        public void onBind(final RealmPrayer prayer) {
            title.setText(prayer.getTitle());
            description.setText(prayer.getDescription());
            dayOfWeek.setText(prayer.getDayOfWeek() + "");
            createdAt.setText(prayer.getCreatedAt() + "");
            updatedAt.setText(prayer.getUpdatedAt() + "");

            menuWidget.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(item.getItemId() == R.id.edit) {
                        editPrayer(prayer);
                    }
                    else if(item.getItemId() == R.id.delete) {
                        deletePrayer(prayer);
                    }

                    return false;
                }
            });
        }
    }

    public class PrayerAdapter extends RecyclerView.Adapter<PrayerViewHolder> {
        @Override
        public PrayerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            CardView cardview = new CardView(getContext());
            View v = LayoutInflater.from(getContext()).inflate(R.layout.itemview_prayer, null);
            cardview.setMenuResource(R.menu.listitem_prayer);
            cardview.addView(v);

            return new PrayerViewHolder(cardview);
        }

        @Override
        public void onBindViewHolder(PrayerViewHolder holder, int position) {
            holder.onBind(prayerList.get(position));
        }

        @Override
        public int getItemCount() {
            return prayerList.size();
        }
    }

    @Override
    public boolean onFABPressed() {
        newPrayer();

        return true;
    }

    @Override
    public int getFABIcon() {
        return R.drawable.ic_thumb_up;
    }
}
