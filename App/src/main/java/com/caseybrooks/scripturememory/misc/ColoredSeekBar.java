package com.caseybrooks.scripturememory.misc;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

import java.util.ArrayList;

public class ColoredSeekBar extends SeekBar {
    ArrayList<Integer> colors;
    Context context;
    int levels;

    public OnSeekBarChangeListener listener;

    public ColoredSeekBar(Context context) {
        super(context);
        init(context, null, 0);
    }

    public ColoredSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public ColoredSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        this.context = context;

        //parse attributes from XML
        this.setOnSeekBarChangeListener(listener);
    }

    public void setOnSeekBarChangeListener(ColoredSeekBarChangeListener l) {
        super.setOnSeekBarChangeListener(l);
    }

    public class ColoredSeekBarChangeListener implements OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
//            Drawable line = seekBar.getProgressDrawable();
//            line.setColorFilter(color, PorterDuff.Mode.SRC_IN);
//
//            if(Build.VERSION.SDK_INT >= 16) {
//                Drawable thumb = seekBar.getThumb();
//                thumb.setColorFilter(color, PorterDuff.Mode.SRC_IN);
//            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }
}
