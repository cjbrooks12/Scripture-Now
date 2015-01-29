package com.caseybrooks.scripturememory.misc;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

//based on answer t o SO question: http://stackoverflow.com/questions/5533078/timepicker-in-preferencescreen
//modified to correctly set the initial value in the local timezone given millis UTC

public class TimePreference extends DialogPreference {
    private Calendar calendar;
    private TimePicker picker = null;
	private Context context;

    public TimePreference(Context context) {
        this(context, null);
    }

    public TimePreference(Context context, AttributeSet attrs) {
        //"hack" to ensure custom Preferences all look the same
        this(context, attrs, context.getResources().getSystem().getIdentifier("dialogPreferenceStyle", "attr", "android"));
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
		this.context = context;

        setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");
        calendar = Calendar.getInstance();
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(context);
		picker.setIs24HourView(DateFormat.is24HourFormat(context));

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
                calendar.set(Calendar.DATE, Calendar.getInstance().get(Calendar.DATE) + 1);
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
		Calendar time8am = Calendar.getInstance();
		time8am.set(Calendar.HOUR_OF_DAY, 8);
		time8am.set(Calendar.MINUTE, 0);
		time8am.set(Calendar.SECOND, 0);

		if (restoreValue) {
			calendar.setTimeInMillis(getPersistedLong(time8am.getTimeInMillis()));
        } else {
			time8am.setTimeInMillis(Long.parseLong((String) defaultValue));
			Log.i("TimePreference", "time8am[h:" + time8am.get(Calendar.HOUR_OF_DAY) + ", m:" + time8am.get(Calendar.MINUTE) + ", s:" + time8am.get(Calendar.SECOND));

			Calendar defCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			defCalendar.setTimeInMillis(Long.parseLong((String) defaultValue));
			calendar.set(Calendar.HOUR_OF_DAY, defCalendar.get(Calendar.HOUR_OF_DAY));
			calendar.set(Calendar.MINUTE, defCalendar.get(Calendar.MINUTE));
			calendar.set(Calendar.SECOND, defCalendar.get(Calendar.SECOND));
			calendar.set(Calendar.MILLISECOND, defCalendar.get(Calendar.MILLISECOND));
		}
        setSummary(getSummary());

		if(shouldPersist()) {
			persistLong(calendar.getTimeInMillis());
		}
    }

    @Override
    public CharSequence getSummary() {
        if (calendar == null) {
            return null;
        }
        return DateFormat.getTimeFormat(context).format(new Date(calendar.getTimeInMillis()));
    }
}