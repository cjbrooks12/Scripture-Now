package com.caseybrooks.common.widget;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.support.annotation.MenuRes;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.view.View;

import com.caseybrooks.common.R;

public class MenuWidget extends TintableImageView {
    @MenuRes int menuResourceId;
    boolean forceOverflow;
    PopupMenu.OnMenuItemClickListener menuItemClickListener;

    public MenuWidget(Context context) {
        super(context);
    }

    public MenuWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public MenuWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs, defStyle);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MenuWidget);
        menuResourceId = a.getResourceId(R.styleable.MenuWidget_menu, 0);
        a.recycle();

        setupMenu();

        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(menuResourceId != 0) {
                    PopupMenu popup = new PopupMenu(getActivity(), MenuWidget.this);
                    popup.getMenuInflater().inflate(menuResourceId, popup.getMenu());

                    if(popup.getMenu().size() == 1 && !forceOverflow) {
                        if(menuItemClickListener != null) {
                            menuItemClickListener.onMenuItemClick(popup.getMenu().getItem(0));
                        }
                    }
                    else {
                        popup.setOnMenuItemClickListener(menuItemClickListener);
                        popup.show();
                    }
                }
            }
        });
    }

    private void setupMenu() {
        if(menuResourceId == 0) {
            setVisibility(View.GONE);
        }
        else {
            setVisibility(View.VISIBLE);
            PopupMenu popup = new PopupMenu(getActivity(), MenuWidget.this);
            popup.getMenuInflater().inflate(menuResourceId, popup.getMenu());

            if(popup.getMenu().size() == 1 && !forceOverflow) {
                setImageDrawable(popup.getMenu().getItem(0).getIcon());
            }
            else {
                setImageResource(R.drawable.ic_overflow);
            }
        }
    }

    private Context getActivity() {
        Context context = getContext();
        while(context instanceof ContextWrapper) {
            if(context instanceof Activity) {
                return context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return getContext();
    }

    @Override
    public void setOnClickListener(OnClickListener l) {

    }

    public void setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener menuItemClickListener) {
        this.menuItemClickListener = menuItemClickListener;
    }

    public void setMenuResource(@MenuRes int menuResourceId) {
        this.menuResourceId = menuResourceId;
        setupMenu();
    }

    public @MenuRes int getMenuResource() {
        return this.menuResourceId;
    }

    public void forceOverflow(boolean forceOverflow) {
        this.forceOverflow = forceOverflow;
    }
}
