package com.caseybrooks.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class CardView extends LinearLayout {


    public CardView(Context context) {
        super(context);

        int[] attrs = new int[] { R.attr.bg_card };
        TypedArray ta = context.obtainStyledAttributes(attrs);
        setBackgroundResource(ta.getResourceId(0, R.drawable.bg_card_light));
        ta.recycle();

        setOrientation(LinearLayout.VERTICAL);
    }

    public CardView(Context context, AttributeSet attrset) {
        super(context, attrset);

        int[] attrs = new int[] { R.attr.bg_card };
        TypedArray ta = context.obtainStyledAttributes(attrs);
        setBackgroundResource(ta.getResourceId(0, R.drawable.bg_card_light));
        ta.recycle();
    }
}
