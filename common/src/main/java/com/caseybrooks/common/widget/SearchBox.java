package com.caseybrooks.common.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.caseybrooks.common.R;
import com.caseybrooks.common.app.fragment.ActivityBaseFragment;
import com.caseybrooks.common.util.ReverseInterpolator;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

//TODO: add button to complete search manually
//TODO: add button to clear query
public class SearchBox extends RelativeLayout {
    MenuWidget menuWidget;
    ArrowDrawableToggle arrow;
    View searchRoot;
    EditText editText;

    ActivityBaseFragment listener;

    String hint;
    boolean isOpen, isRevealed;
    String query;

    public SearchBox(Context context) {
        super(context);

        initialize(context, null, 0);
    }

    public SearchBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public SearchBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyle) {
        LayoutInflater.from(getContext()).inflate(R.layout.searchbox, this);

        searchRoot = findViewById(R.id.searchRoot);
        arrow = (ArrowDrawableToggle) findViewById(R.id.arrow);
        menuWidget = (MenuWidget) findViewById(R.id.menuWidget);
        editText = (EditText) findViewById(R.id.searchEditText);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SearchBox);
        menuWidget.setMenuResource(a.getResourceId(R.styleable.SearchBox_menu, 0));
        a.recycle();


        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setVisibility(View.GONE);
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        arrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setIsOpen(!arrow.isOpen());
            }
        });

        editText.setFocusable(false);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(TextUtils.isEmpty(s)) {
                    return;
                }

                query = s.toString();

                if(listener != null) {
                    listener.onQueryChanged(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                    setIsOpen(false);

                    if(listener != null) {
                        return listener.onSearchSubmitted(query);
                    }
                }
                return false;
            }
        });
        editText.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    editText.setFocusableInTouchMode(true);

                    setIsOpen(true);
                }
                return false;
            }
        });

        menuWidget.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(listener != null) {
                    return listener.onSearchMenuItemSelected(item);
                }
                return false;
            }
        });

        if(context instanceof ActivityBaseFragment) {
            this.listener = (ActivityBaseFragment) context;
        }

        setIsOpen(false);
    }

    public void reveal(Activity activity) {
        if(isRevealed)
            return;

        isRevealed = true;

        final FrameLayout layout = (FrameLayout) activity.getWindow().getDecorView().findViewById(android.R.id.content);
        View root = findViewById(R.id.searchRoot);

        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                96,
                getResources().getDisplayMetrics());

        SupportAnimator animator = ViewAnimationUtils.createCircularReveal(
                root,
                getWidth(),
                0,
                0,
                (int) Math.max(layout.getWidth() * 1.5, px));

        animator.setInterpolator(new AccelerateInterpolator());
        animator.setDuration(500);
        setVisibility(View.VISIBLE);
        animator.start();
    }

    public void hide(Activity activity) {
        if(!isRevealed)
            return;

        isRevealed = false;

        final FrameLayout layout = (FrameLayout) activity.getWindow().getDecorView().findViewById(android.R.id.content);
        View root = findViewById(R.id.searchRoot);

        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                96,
                getResources().getDisplayMetrics());

        SupportAnimator animator = ViewAnimationUtils.createCircularReveal(
                root,
                getWidth(),
                0,
                0,
                (int) Math.max(layout.getWidth() * 1.5, px));
        animator.setInterpolator(new ReverseInterpolator(AccelerateInterpolator.class));
        animator.setDuration(500);
        animator.start();
        animator.addListener(new SupportAnimator.AnimatorListener() {

            @Override
            public void onAnimationStart() {

            }

            @Override
            public void onAnimationEnd() {
                setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel() {

            }

            @Override
            public void onAnimationRepeat() {

            }
        });
    }

    public void revealInstant(Activity activity) {
        if(isRevealed)
            return;

        isRevealed = true;
        setVisibility(View.VISIBLE);
    }

    public void hideInstant(Activity activity) {
        if(!isRevealed)
            return;
        isRevealed = false;
        setVisibility(View.GONE);
    }


    public boolean isRevealed() {
        return isRevealed;
    }

    public void setIsOpen(boolean isOpen) {
        if(this.isOpen == isOpen)
            return;

        this.isOpen = isOpen;

        if(isOpen()) {
            arrow.animateOpen();
        }
        else {
            arrow.animateClose();
        }

        updateEditText();
    }

    public boolean isOpen() {
        return isOpen;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = TextUtils.isEmpty(hint) ? "" : hint;
        updateEditText();
    }

    public void setText(String query) {
        this.query = TextUtils.isEmpty(query) ? "" : query;
        updateEditText();
    }

    public String getText() {
        return query;
    }

    private void updateEditText() {
        if(isOpen()) {
            editText.setHint("");
            editText.setText(query);
            arrow.animateOpen();
        }
        else {
            editText.setHint(getHint());
            editText.setText("");

            editText.setFocusable(false);
            arrow.animateClose();
        }
    }

    public void setMenuResource(int menuResourceId) {
        menuWidget.setMenuResource(menuResourceId);
    }
}
