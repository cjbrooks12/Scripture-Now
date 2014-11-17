package com.caseybrooks.scripturememory.data;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class TimePreference extends DialogPreference {
    private Calendar calendar;
    private TimePicker picker = null;

    public TimePreference(Context context) {
        this(context, null);
    }

    public TimePreference(Context context, AttributeSet attrs) {
        //"hack" to ensure custom Preferences all look the same
        this(context, attrs, context.getResources().getSystem().getIdentifier("dialogPreferenceStyle", "attr", "android"));
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");
        calendar = new GregorianCalendar();
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());
        return picker;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        picker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        picker.setCurrentMinute(calendar.get(Calendar.MINUTE));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            calendar.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
            calendar.set(Calendar.MINUTE, picker.getCurrentMinute());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            //If the set and current hours are the same and the set minute is not later than current
            if(calendar.get(Calendar.HOUR) == Calendar.getInstance().get(Calendar.HOUR) &&
                    calendar.get(Calendar.MINUTE) <= Calendar.getInstance().get(Calendar.MINUTE)) {
                calendar.set(Calendar.DATE, Calendar.getInstance().get(Calendar.DATE) + 1);
            }
            //If the set hour is before the current hour
            else if(calendar.get(Calendar.HOUR) < Calendar.getInstance().get(Calendar.HOUR)) {
                calendar.set(Calendar.DATE, Calendar.getInstance().get(Calendar.HOUR) + 1);
            }
            else {
                calendar.set(Calendar.DATE, Calendar.getInstance().get(Calendar.DATE));
            }

            setSummary(getSummary());
            persistLong(calendar.getTimeInMillis());
            notifyChanged();
            callChangeListener(calendar.getTimeInMillis());
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        if (restoreValue) {
            if (defaultValue == null) {
                calendar.setTimeInMillis(getPersistedLong(System.currentTimeMillis()));
            } else {
                calendar.setTimeInMillis(Long.parseLong(getPersistedString((String) defaultValue)));
            }
        } else {
            if (defaultValue == null) {
                calendar.setTimeInMillis(System.currentTimeMillis());
            } else {
                calendar.setTimeInMillis(Long.parseLong((String) defaultValue));
            }
        }
        setSummary(getSummary());
    }

    @Override
    public CharSequence getSummary() {
        if (calendar == null) {
            return null;
        }
        return DateFormat.getTimeFormat(getContext()).format(new java.util.Date(calendar.getTimeInMillis()));
    }
}
