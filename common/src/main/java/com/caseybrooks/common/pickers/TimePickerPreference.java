package com.caseybrooks.common.pickers;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.Preference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Pair;
import android.widget.TimePicker;

import com.caseybrooks.common.app.AppSettings;
import com.caseybrooks.common.R;
import com.caseybrooks.common.util.Util;

public class TimePickerPreference extends Preference implements
        Preference.OnPreferenceClickListener,
        TimePickerDialog.OnTimeSetListener {

    TimePickerDialog.OnTimeSetListener listener;
    boolean is24hour;

    public TimePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialize(attrs);
    }

    public TimePickerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initialize(attrs);
    }

    private void initialize(AttributeSet attrs) {
        super.setOnPreferenceClickListener(this);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TimePickerPreference);
        int is24hourEnum = a.getInt(R.styleable.TimePickerPreference_is24hour, 0);
        a.recycle();

        if(is24hourEnum == 0)
            is24hour = DateFormat.is24HourFormat(getContext());
        else if(is24hourEnum == 1)
            is24hour = true;
        else if(is24hourEnum == 2)
            is24hour = false;
    }

    @Override
    public void onDependencyChanged(Preference dependency, boolean disableDependent) {
        super.onDependencyChanged(dependency, disableDependent);
        setEnabled(!disableDependent);
    }

    @Override
    public final void setOnPreferenceClickListener(OnPreferenceClickListener onPreferenceClickListener) {

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Pair<Integer, Integer> time = AppSettings.getTime(getContext(), getKey());

        TimePickerDialog dialog = new TimePickerDialog(getContext(), this, time.first, time.second, is24hour);

        dialog.show();

        return true;
    }

    @Override
    public CharSequence getSummary() {
        String summary = super.getSummary().toString();

        Pair<Integer, Integer> time = AppSettings.getTime(getContext(), getKey());

        if(!DateFormat.is24HourFormat(getContext())) {
            String hour;
            String minute;
            String am_pm;

            if(time.first > 12)
                hour = "" + (time.first - 12);
            else
                hour = "" + time.first;

            if(time.second < 10)
                minute = "0" + time.second;
            else
                minute = "" + time.second;

            if(time.first > 12)
                am_pm = "pm";
            else
                am_pm = "am";

            return Util.formatString(summary, hour, minute, am_pm);
        }
        else {
            String hour;
            String minute;

            hour = "" + time.first;

            if(time.second < 10)
                minute = "0" + time.second;
            else
                minute = "" + time.second;

            return Util.formatString(summary, hour, minute, "");
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        AppSettings.putTime(getContext(), getKey(), hourOfDay, minute);
        setSummary(getSummary());

        if(getOnPreferenceChangeListener() != null)
            getOnPreferenceChangeListener().onPreferenceChange(this, AppSettings.getTime(getContext(), getKey()));
    }
}