package com.caseybrooks.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.MenuRes;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.ActivityBase;

public class CardView extends FrameLayout {
    FrameLayout viewLayout;
    TextView title;
    MenuWidget menuWidget;

    public CardView(Context context) {
        super(context);
        init(null, 0);
    }

    public CardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        LayoutInflater.from(getContext()).inflate(R.layout.view_cardview, this);

        title = (TextView) findViewById(R.id.title);
        menuWidget = (MenuWidget) findViewById(R.id.menu_widget);
        viewLayout = (FrameLayout) findViewById(R.id.main_content);

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CardView, defStyle, 0);

        title.setText(a.getString(R.styleable.CardView_title));
        menuWidget.setMenuResource(a.getResourceId(R.styleable.CardView_menu, 0));
        menuWidget.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOverflowMenuItemClick(item);
            }
        });

        a.recycle();
    }

    public boolean onOverflowMenuItemClick(MenuItem item) {
        return false;
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public String getTitle() {
        return this.title.getText().toString();
    }

    public void setMenuResource(@MenuRes int menuResource) {
        menuWidget.setMenuResource(menuResource);
    }

    public @MenuRes int getMenuResource() {
        return menuWidget.getMenuResource();
    }

    public void addView(View view) {
        viewLayout.addView(view);
    }

    public void removeView(View view) {
        viewLayout.removeView(view);
    }

    public void removeAllViews() {
        viewLayout.removeAllViews();
    }

    @Override
    public ViewGroup.LayoutParams getLayoutParams() {
        return super.getLayoutParams();
    }

    public ActivityBase getActivityBase() {
        return (ActivityBase) getContext();
    }
}
