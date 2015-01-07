package com.caseybrooks.scripturememory.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.scripturememory.R;

public class NavDrawerHeader extends RelativeLayout {
    Context context;
    TextView reference;
    Passage passage;

    public NavDrawerHeader(Context context) {
        super(context);
        this.context = context;
        init(null, 0);
    }

    public NavDrawerHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs, 0);
    }

    public NavDrawerHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.parallax_drawer_header, this);
    }
}
