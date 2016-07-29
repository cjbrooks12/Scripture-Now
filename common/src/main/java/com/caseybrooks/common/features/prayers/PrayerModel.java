package com.caseybrooks.common.features.prayers;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.CompoundButton;

import com.caseybrooks.common.BR;
import com.caseybrooks.common.R;
import com.caseybrooks.common.app.ActivityBase;
import com.caseybrooks.common.databinding.DialogNewPrayerBinding;
import com.caseybrooks.common.util.SimpleTextWatcher;
import com.caseybrooks.common.util.Util;
import com.touchboarder.weekdaysbuttons.WeekdaysDataItem;
import com.touchboarder.weekdaysbuttons.WeekdaysDataSource;

import java.util.ArrayList;
import java.util.Calendar;

import io.realm.Realm;

public class PrayerModel extends BaseObservable implements WeekdaysDataSource.Callback {
    ActivityBase context;
    DialogNewPrayerBinding binding;

    String title;
    String description;
    int dayOfWeek;
    boolean answered;
    String answerNotes;
    WeekdaysDataSource weekdays;

    RealmPrayer prayer;

    public PrayerModel(@NonNull ActivityBase context) {
        this.context = context;
        prayer = new RealmPrayer();
    }

    public void initializeBinding(@NonNull DialogNewPrayerBinding binding) {
        this.binding = binding;
        this.binding.title.setText(getTitle());
        this.binding.description.setText(getDescription());

        weekdays = new WeekdaysDataSource(context, R.id.weekdays_recycler_view, binding.getRoot());
        weekdays.setNumberOfLetters(2);
        weekdays.setFontBaseSize(16);
        weekdays.start(this);
    }

    public void modifyPrayer(RealmPrayer prayer) {
        this.prayer = prayer;
        setTitle(prayer.getTitle());
        setDescription(prayer.getDescription());
        setDayOfWeek(prayer.getDayOfWeek());
    }

    @Bindable
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        notifyPropertyChanged(BR.description);
    }

    @Bindable
    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
        notifyPropertyChanged(BR.dayOfWeek);
    }

    @Bindable
    public boolean isAnswered() {
        return answered;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
        notifyPropertyChanged(BR.answered);
    }

    @Bindable
    public String getAnswerNotes() {
        return answerNotes;
    }

    public void setAnswerNotes(String answerNotes) {
        this.answerNotes = answerNotes;
        notifyPropertyChanged(BR.answerNotes);
    }

    public TextWatcher onTitleChanged = new SimpleTextWatcher() {
        @Override
        public void onTextChanged(String s) {
            setTitle(s);
        }
    };

    public TextWatcher onDescriptionChanged = new SimpleTextWatcher() {
        @Override
        public void onTextChanged(String s) {
            setDescription(s);
        }
    };

    public TextWatcher onAnswerNotesChanged = new SimpleTextWatcher() {
        @Override
        public void onTextChanged(String s) {
            setAnswerNotes(s);
        }
    };

    public void checkboxChanged(CompoundButton buttonView, boolean isChecked) {
        setAnswered(isChecked);
        notifyPropertyChanged(BR.answered);
    }

    @Override
    public void onWeekdaysItemClicked(int attachId, WeekdaysDataItem item) {

    }

    @Override
    public void onWeekdaysSelected(int attachId, ArrayList<WeekdaysDataItem> items) {

    }

    public void clear() {
        setTitle("");
        setDescription("");
        setDayOfWeek(0);
        setAnswered(false);
        setAnswerNotes("");
    }

    public boolean validate() {
        int errors = 0;

        if(TextUtils.isEmpty(getTitle())) {
            errors++;
            binding.title.setError("Cannot be empty");
        }

        if(TextUtils.isEmpty(getDescription())) {
            errors++;
            binding.description.setError("Cannot be empty");
        }

        return (errors == 0) ? true : false;
    }

    public void saveToRealm() {
        Realm realm = ActivityBase.getRealm(context);
        realm.beginTransaction();

        prayer.setTitle(getTitle());
        prayer.setDescription(getDescription());
        prayer.setDayOfWeek(getDayOfWeek());

        if(TextUtils.isEmpty(prayer.getId())) {
            prayer.setId(Util.generateUUID());
        }

        long time = Calendar.getInstance().getTimeInMillis();
        if(prayer.getCreatedAt() == 0) {
            prayer.setCreatedAt(time);
        }

        prayer.setUpdatedAt(time);

        realm.copyToRealmOrUpdate(prayer);
        realm.commitTransaction();
    }

    public void removeFromRealm() {
        Realm realm = ActivityBase.getRealm(context);
        realm.beginTransaction();
        prayer.removeFromRealm();
        realm.commitTransaction();
    }
}
