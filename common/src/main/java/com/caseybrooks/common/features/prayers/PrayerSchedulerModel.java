package com.caseybrooks.common.features.prayers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.widget.SeekBar;

import com.caseybrooks.common.BR;
import com.caseybrooks.common.app.AppSettings;
import com.caseybrooks.common.databinding.DialogPrayerScheduleBinding;
import com.caseybrooks.common.widget.TimePicker;

import java.util.Calendar;

@SuppressLint("CommitPrefEdits")
public class PrayerSchedulerModel extends BaseObservable {
    private static final String PRAYER_SCHEDULE_START_H = "PRAYER_SCHEDULE_START_H";
    private static final String PRAYER_SCHEDULE_START_M = "PRAYER_SCHEDULE_START_M";
    private static final String PRAYER_SCHEDULE_END_H = "PRAYER_SCHEDULE_END_H";
    private static final String PRAYER_SCHEDULE_END_M = "PRAYER_SCHEDULE_END_M";
    private static final String PRAYER_SCHEDULE_NOTIFICATION_COUNT = "PRAYER_SCHEDULE_NOTIFICATION_COUNT";



    Context context;
    SharedPreferences prefs;

    //Bindable attributes
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;
    private int notificationCount;

    private DialogPrayerScheduleBinding binding;

    public PrayerSchedulerModel(@NonNull Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(AppSettings.SettingsFile, 0);

        loadSettings();
    }

    public void loadSettings() {
        startHour = prefs.getInt(PRAYER_SCHEDULE_START_H, 8);
        startMinute = prefs.getInt(PRAYER_SCHEDULE_START_M, 0);

        endHour = prefs.getInt(PRAYER_SCHEDULE_END_H, 18);
        endMinute = prefs.getInt(PRAYER_SCHEDULE_END_M, 0);

        notificationCount = prefs.getInt(PRAYER_SCHEDULE_NOTIFICATION_COUNT, 0);

        notifyChange();
    }

    public void initializeBinding(@NonNull DialogPrayerScheduleBinding binding) {
        this.binding = binding;
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, startHour);
        startTime.set(Calendar.MINUTE, startMinute);
        this.binding.startTime.setTime(startTime.getTimeInMillis());

        Calendar endTime = Calendar.getInstance();
        endTime.set(Calendar.HOUR_OF_DAY, endHour);
        endTime.set(Calendar.MINUTE, endMinute);
        this.binding.endTime.setTime(endTime.getTimeInMillis());

        int minutes = (endHour * 60 + endMinute) - (startHour * 60 + startMinute);
        this.binding.notificationsPerDay.setMax((int) Math.ceil(minutes / 30));

        this.binding.notificationsPerDay.setProgress(notificationCount);
    }

    public TimePicker.TimeChangedListener onStartTimeChanged = new TimePicker.TimeChangedListener() {

        @SuppressLint("CommitPrefEdits")
        @Override
        public void onTimeChanged(TimePicker picker, Calendar time, boolean fromUser) {
            startHour = time.get(Calendar.HOUR_OF_DAY);
            startMinute = time.get(Calendar.MINUTE);
            prefs
                    .edit()
                    .putInt(PRAYER_SCHEDULE_START_H, startHour)
                    .putInt(PRAYER_SCHEDULE_START_M, startMinute)
                    .commit();

            if(fromUser)
                updateNotificationCountRange();

            notifyPropertyChanged(BR.startHour);
            notifyPropertyChanged(BR.startMinute);
        }
    };

    public TimePicker.TimeChangedListener onEndTimeChanged = new TimePicker.TimeChangedListener() {

        @Override
        public void onTimeChanged(TimePicker picker, Calendar time, boolean fromUser) {
            endHour = time.get(Calendar.HOUR_OF_DAY);
            endMinute = time.get(Calendar.MINUTE);
            prefs
                    .edit()
                    .putInt(PRAYER_SCHEDULE_END_H, endHour)
                    .putInt(PRAYER_SCHEDULE_END_M, endMinute)
                    .commit();

            if(fromUser)
                updateNotificationCountRange();

            notifyPropertyChanged(BR.endHour);
            notifyPropertyChanged(BR.endMinute);
        }
    };

    public void notificationsPerDayChanged(SeekBar seekBar, int progress, boolean fromUser) {

        notificationCount = progress;

        prefs
            .edit()
            .putInt(PRAYER_SCHEDULE_NOTIFICATION_COUNT, notificationCount)
            .commit();

        notifyPropertyChanged(BR.notificationCount);
    }

    private void updateNotificationCountRange() {
        //calculate the appropriate time range
        if(startHour > endHour) {
            startHour = endHour;
        }

        if((startHour == endHour) && (startMinute > endMinute)) {
            startMinute = endMinute;
        }

        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, startHour);
        startTime.set(Calendar.MINUTE, startMinute);
        this.binding.startTime.setTime(startTime.getTimeInMillis());

        Calendar endTime = Calendar.getInstance();
        endTime.set(Calendar.HOUR_OF_DAY, endHour);
        endTime.set(Calendar.MINUTE, endMinute);
        this.binding.endTime.setTime(endTime.getTimeInMillis());

        //calculate the number of notifications to be had in that time range
        int minutes = (endHour * 60 + endMinute) - (startHour * 60 + startMinute);
        int maxNotifications = (int) Math.ceil(minutes / 30);

        if(notificationCount > maxNotifications) {
            notificationCount = maxNotifications;
            notifyPropertyChanged(BR.notificationCount);
        }
        this.binding.notificationsPerDay.setMax(maxNotifications);
    }

    @Bindable
    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    @Bindable
    public int getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    @Bindable
    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    @Bindable
    public int getEndMinute() {
        return endMinute;
    }

    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }

    @Bindable
    public int getNotificationCount() {
        return notificationCount;
    }

    public void setNotificationCount(int notificationCount) {
        this.notificationCount = notificationCount;
    }
}
