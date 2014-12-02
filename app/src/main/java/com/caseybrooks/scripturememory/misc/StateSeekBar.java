package com.caseybrooks.scripturememory.misc;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

public class StateSeekBar extends SeekBar {
    Context context;

    public StateSeekBar(Context context) {
        super(context);
        this.context = context;
        init(null, 0);
    }

    public StateSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs, 0);
    }

    public StateSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {

    }


}
