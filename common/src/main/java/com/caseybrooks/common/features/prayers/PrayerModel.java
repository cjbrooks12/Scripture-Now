package com.caseybrooks.common.features.prayers;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import com.caseybrooks.common.BR;
import com.caseybrooks.common.R;
import com.caseybrooks.common.app.ActivityBase;
import com.caseybrooks.common.databinding.DialogNewPrayerBinding;
import com.caseybrooks.common.util.SimpleTextWatcher;
import com.caseybrooks.common.util.Util;

import java.util.Calendar;

import io.realm.Realm;

public class PrayerModel extends BaseObservable {
    Context context;
    DialogNewPrayerBinding binding;

    String title;
    String description;
    int dayOfWeek;
    boolean answered;
    String answerNotes;

    RealmPrayer prayer;

    public PrayerModel(@NonNull Context context) {
        this.context = context;
        prayer = new RealmPrayer();
    }

    public void initializeBinding(@NonNull DialogNewPrayerBinding binding) {
        this.binding = binding;
        this.binding.title.setText(getTitle());
        this.binding.description.setText(getDescription());

        if(getDayOfWeek() == Calendar.SUNDAY) {
            this.binding.dayOfWeekRadioGroup.check(R.id.radio_sunday);
        }
        else if(getDayOfWeek() == Calendar.MONDAY) {
            this.binding.dayOfWeekRadioGroup.check(R.id.radio_monday);
        }
        else if(getDayOfWeek() == Calendar.TUESDAY) {
            this.binding.dayOfWeekRadioGroup.check(R.id.radio_tuesday);
        }
        else if(getDayOfWeek() == Calendar.WEDNESDAY) {
            this.binding.dayOfWeekRadioGroup.check(R.id.radio_wednesday);
        }
        else if(getDayOfWeek() == Calendar.THURSDAY) {
            this.binding.dayOfWeekRadioGroup.check(R.id.radio_thursday);
        }
        else if(getDayOfWeek() == Calendar.FRIDAY) {
            this.binding.dayOfWeekRadioGroup.check(R.id.radio_friday);
        }
        else if(getDayOfWeek() == Calendar.SATURDAY) {
            this.binding.dayOfWeekRadioGroup.check(R.id.radio_saturday);
        }
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

    public void radioButtonChanged(RadioGroup group, int checkedId) {
        if(checkedId == R.id.radio_sunday) {
            setDayOfWeek(Calendar.SUNDAY);
        }
        else if(checkedId == R.id.radio_monday) {
            setDayOfWeek(Calendar.MONDAY);
        }
        else if(checkedId == R.id.radio_tuesday) {
            setDayOfWeek(Calendar.TUESDAY);
        }
        else if(checkedId == R.id.radio_wednesday) {
            setDayOfWeek(Calendar.WEDNESDAY);
        }
        else if(checkedId == R.id.radio_thursday) {
            setDayOfWeek(Calendar.THURSDAY);
        }
        else if(checkedId == R.id.radio_friday) {
            setDayOfWeek(Calendar.FRIDAY);
        }
        else if(checkedId == R.id.radio_saturday) {
            setDayOfWeek(Calendar.SATURDAY);
        }
    }

    public void checkboxChanged(CompoundButton buttonView, boolean isChecked) {
        setAnswered(isChecked);
        notifyPropertyChanged(BR.answered);
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
