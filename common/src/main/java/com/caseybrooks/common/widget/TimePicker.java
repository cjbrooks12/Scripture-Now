package com.caseybrooks.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.caseybrooks.common.R;

import java.util.Calendar;

/**
 * Taken and improved from https://github.com/ugurtekbas/dialTimePicker
 *
 * FEATURES ADDED
 *      -Completely rewritten, design is about the only thing I kept
 *      -Ability to set the initial time
 *      -Ability to quantize the time by minutes as we move around the circle
 */
@BindingMethods(@BindingMethod(type = TimePicker.class, attribute = "app:onTimeChanged", method = "setOnTimeChangedListener"))
public class TimePicker extends View {
    public interface TimeChangedListener {
        void onTimeChanged(TimePicker picker, Calendar time, boolean fromUser);
    }

    String TAG = "TimePicker";

    Paint titleTextPaint;
    Paint timeTextPaint;
    Paint amPmTextPaint;
    Paint trackPaint;
    Paint thumbPaint;
    RectF rectF;
    Rect textBounds = new Rect();

    float radius;
    float thumbThickness;
    float offset;
    float slopX;
    float slopY;
    float posX;
    float posY;
    float thumbX;
    float thumbY;

    int hour;
    int previousHour;
    int minute;
    boolean isAm;
    int quantize;

    boolean isMoving;

    TimeChangedListener timeListener;

    String title;

    public TimePicker(Context context) {
        super(context);
        init(null);
    }

    public TimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        int textColor = Color.WHITE;
        int trackColor = Color.BLACK;
        int thumbColor = Color.RED;
        title = null;
        hour = 8;
        minute = 0;
        quantize = -1;

        if(attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TimePicker);
            textColor = a.getColor(R.styleable.TimePicker_textColor, textColor);
            trackColor = a.getColor(R.styleable.TimePicker_trackColor, trackColor);
            thumbColor = a.getColor(R.styleable.TimePicker_thumbColor, thumbColor);
            title = a.getString(R.styleable.TimePicker_title);

            hour = a.getInt(R.styleable.TimePicker_initHour, hour);
            if(hour < 0 || hour >= 24)
                hour = 0;
            if(hour >= 12)
                hour -= 12;

            minute = a.getInt(R.styleable.TimePicker_initMin, minute);
            if(minute < 0 || minute >= 60)
                minute = 0;

            quantize = a.getInt(R.styleable.TimePicker_quantize, quantize);
            if(quantize >= 60)
                quantize = 0;

            a.recycle();
        }

        titleTextPaint = new Paint();
        titleTextPaint.setAntiAlias(true);
        titleTextPaint.setStrokeCap(Paint.Cap.ROUND);
        titleTextPaint.setTextAlign(Paint.Align.CENTER);
        titleTextPaint.setColor(textColor);
        titleTextPaint.setStrokeWidth(1);
        titleTextPaint.setStyle(Paint.Style.FILL);
        titleTextPaint.setAlpha(255);

        timeTextPaint = new Paint();
        timeTextPaint.setAntiAlias(true);
        timeTextPaint.setStrokeCap(Paint.Cap.ROUND);
        timeTextPaint.setTextAlign(Paint.Align.CENTER);
        timeTextPaint.setColor(textColor);
        timeTextPaint.setStrokeWidth(1);
        timeTextPaint.setStyle(Paint.Style.FILL);
        timeTextPaint.setAlpha(255);

        amPmTextPaint = new Paint();
        amPmTextPaint.setAntiAlias(true);
        amPmTextPaint.setStrokeCap(Paint.Cap.ROUND);
        amPmTextPaint.setTextAlign(Paint.Align.CENTER);
        amPmTextPaint.setColor(textColor);
        amPmTextPaint.setStrokeWidth(1);
        amPmTextPaint.setStyle(Paint.Style.FILL);
        amPmTextPaint.setAlpha(255);

        trackPaint = new Paint();
        trackPaint.setAntiAlias(true);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setColor(trackColor);
        trackPaint.setStrokeWidth(1);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setAlpha(255);

        thumbPaint = new Paint();
        thumbPaint.setAntiAlias(true);
        thumbPaint.setStrokeCap(Paint.Cap.ROUND);
        thumbPaint.setTextAlign(Paint.Align.CENTER);
        thumbPaint.setColor(thumbColor);
        thumbPaint.setStrokeWidth(1);
        thumbPaint.setStyle(Paint.Style.FILL);
        thumbPaint.setAlpha(255);

        rectF = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int min = Math.min(width, height);
        setMeasuredDimension(min, min);

        offset = min * 0.5f;
        radius = min / 2 - ((min / 20) * 2);
        thumbThickness = radius / 7;
        rectF.set(-radius, -radius, radius, radius);

        titleTextPaint.setTextSize(min / 10);
        timeTextPaint.setTextSize(min / 5);
        amPmTextPaint.setTextSize(min / 10);

        trackPaint.setStrokeWidth(min / 25);
        thumbPaint.setStrokeWidth(min / 25);

        float angle = (float) (Math.toRadians(hour * 30) - (Math.PI / 2));
        thumbX = (float) (radius * Math.cos(angle));
        thumbY = (float) (radius * Math.sin(angle));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(offset, offset);

        String timeString = "";
        String amPmString = "";

        if(DateFormat.is24HourFormat(getContext())) {
            int tempHour = hour;
            if(!isAm) {
                tempHour += 12;
            }

            if(tempHour < 10) timeString += "0";
            timeString += tempHour;
            timeString += ":";
            if(minute < 10) timeString += "0";
            timeString += minute;

            amPmString = "";
        }
        else {
            int tempHour = hour;
            if(tempHour == 0) {
                tempHour += 12;
            }

            timeString += tempHour;
            timeString += ":";
            if(minute < 10) timeString += "0";
            timeString += minute;

            amPmString = isAm ? "AM" : "PM";
        }

        timeTextPaint.getTextBounds(timeString, 0, timeString.length(), textBounds);
        float timeHeight = textBounds.height();
        float center = timeHeight / 2;
        canvas.drawText(timeString, 0, center, timeTextPaint);

        titleTextPaint.getTextBounds(title, 0, title.length(), textBounds);
        canvas.drawText(title, 0, center - 2.5f*textBounds.height(), titleTextPaint);

        amPmTextPaint.getTextBounds(amPmString, 0, amPmString.length(), textBounds);
        canvas.drawText(amPmString, 0, center + 1.75f*textBounds.height(), amPmTextPaint);

        canvas.drawOval(rectF, trackPaint);
        canvas.drawCircle(thumbX, thumbY, thumbThickness, thumbPaint);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);

        posX = event.getX() - offset;
        posY = event.getY() - offset;

        switch(event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            if(posX >= (thumbX - thumbThickness) &&
                    posX <= (thumbX + thumbThickness) &&
                    posY >= (thumbY - thumbThickness) &&
                    posY <= (thumbY + thumbThickness)) {

                slopX = posX - thumbX;
                slopY = posY - thumbY;
                isMoving = true;
                invalidate();
            }
            else {
                getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
            break;
        case MotionEvent.ACTION_MOVE:
            if(isMoving) {
                double angle = Math.atan2(posY - slopY, posX - slopX);
                double degrees = ((Math.toDegrees(angle) + 360) + 90) % 360;

                boolean timeHasChanged = false;


                //calculate the time given the position around the circle
                int tmpHour = ((int) degrees / 30);

                if(tmpHour != hour) {
                    hour = tmpHour;
                    timeHasChanged = true;
                }

                int nextHour = ((hour + 1) % 13);
                int tmpMinute = (int) (60 - 2 * Math.abs(degrees - (nextHour * 30)));

                //quantize the minutes
                if(quantize > 0) {
                    tmpMinute = quantize * (tmpMinute / quantize);
                }
                else if(quantize == 0) {
                    tmpMinute = 0;
                }

                if(tmpMinute != minute) {
                    minute = tmpMinute;
                    timeHasChanged = true;
                }

                //determine if time is now AM or PM
                if((hour == 0 && previousHour == 11) || (hour == 11 && previousHour == 0)) {
                    isAm = !isAm;
                    timeHasChanged = true;
                }
                previousHour = hour;

                //calculate position of thumb based on time
                int clampedDegrees = (int) ((hour * 30) + (minute * 0.5));

                thumbX = (float) (radius * Math.cos(Math.toRadians(clampedDegrees) - Math.PI / 2));
                thumbY = (float) (radius * Math.sin(Math.toRadians(clampedDegrees) - Math.PI / 2));

                if(timeHasChanged && timeListener != null) {
                    Calendar changedTime = Calendar.getInstance();
                    changedTime.set(Calendar.HOUR, hour);
                    changedTime.set(Calendar.MINUTE, minute);
                    changedTime.set(Calendar.AM_PM, (isAm) ? Calendar.AM : Calendar.PM);
                    timeListener.onTimeChanged(this, changedTime, true);
                }

                invalidate();
            }
            else {
                getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            isMoving = false;
            invalidate();
            break;
        }

        return true;
    }

    public void setTime(long millis) {
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(millis);

        boolean timeHasChanged = (
            hour != time.get(Calendar.HOUR) ||
            minute != time.get(Calendar.MINUTE) ||
            isAm != (time.get(Calendar.AM_PM) == Calendar.AM));

        hour = time.get(Calendar.HOUR);
        minute = time.get(Calendar.MINUTE);
        isAm = time.get(Calendar.AM_PM) == Calendar.AM;

        //quantize the minutes
        if(quantize > 0) {
            minute = quantize * (minute / quantize);
        }
        else if(quantize == 0) {
            minute = 0;
        }

        previousHour = hour;

        //calculate position of thumb based on time
        int clampedDegrees = (int) ((hour * 30) + (minute * 0.5));

        thumbX = (float) (radius * Math.cos(Math.toRadians(clampedDegrees) - Math.PI / 2));
        thumbY = (float) (radius * Math.sin(Math.toRadians(clampedDegrees) - Math.PI / 2));

        if(timeHasChanged && timeListener != null) {
            Calendar changedTime = Calendar.getInstance();
            changedTime.set(Calendar.HOUR, hour);
            changedTime.set(Calendar.MINUTE, minute);
            changedTime.set(Calendar.AM_PM, (isAm) ? Calendar.AM : Calendar.PM);
            timeListener.onTimeChanged(this, changedTime, false);
        }

        invalidate();
    }

    public void setOnTimeChangedListener(TimeChangedListener timeChangedListener) {
        this.timeListener = timeChangedListener;
    }
}