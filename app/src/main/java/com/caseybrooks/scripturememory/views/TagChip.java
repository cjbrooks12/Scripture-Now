package com.caseybrooks.scripturememory.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.Util;
import com.caseybrooks.scripturememory.data.VerseDB;

public class TagChip extends RelativeLayout {
//Data Members
//------------------------------------------------------------------------------
    Context context;

    TextView tagName;
    ImageView tagCircle;

    /*
    0 = full chip
    1 = small chip
    2 = ask for new tag
     */
    int mode;

    int tagId;

//Constructors and Initialization
//------------------------------------------------------------------------------    
    public TagChip(Context context) {
        super(context);
        this.context = context;

        LayoutInflater.from(context).inflate(R.layout.chip_tag_full, this);

        initialize();
    }

    public TagChip(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        LayoutInflater.from(context).inflate(R.layout.chip_tag_full, this);

        initialize();
    }

    void initialize() {
        tagName = (TextView) findViewById(R.id.chip_tag_name);
        tagCircle = (ImageView) findViewById(R.id.chip_tag_circle);
    }

    public void removeFromParent() {
        ((ViewGroup)getParent()).removeView(TagChip.this);
    }

    public void setMode(int mode) {
        this.mode = mode;

        if(mode == 0) {
            tagName.setVisibility(View.VISIBLE);
        }
        else if(mode == 1) {
            tagName.setVisibility(View.GONE);
        }
        else if(mode == 2) {
            tagName.setVisibility(View.VISIBLE);
            tagName.setText("Add New Tag");
            tagCircle.setBackgroundDrawable(Util.Drawables.circle(Color.parseColor("#000000")));
            tagCircle.setImageResource(R.drawable.abc_ic_clear_mtrl_alpha);
        }
    }

    public void setTag(int tagId) {
        this.tagId = tagId;

        VerseDB verseDB = new VerseDB(context);
        verseDB.open();
        String name = verseDB.getTagName(tagId);
        tagName.setText(name);
        int color = verseDB.getTagColor(name);
        //TODO: make getTagColor also accept int as ID, makes more sense here to use the same reference
        verseDB.close();

        tagCircle.setBackgroundDrawable(Util.Drawables.circle(color));
    }



}
