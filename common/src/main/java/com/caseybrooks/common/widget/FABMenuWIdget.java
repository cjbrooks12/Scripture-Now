package com.caseybrooks.common.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class FABMenuWIdget extends RecyclerView {

    public FABMenuWIdget(Context context) {
        super(context);
        initialize(context, null);
    }

    public FABMenuWIdget(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public FABMenuWIdget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {

    }
}
