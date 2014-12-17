package com.caseybrooks.scripturememory.fragments;

import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.activities.MainActivity;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.Util;
import com.caseybrooks.scripturememory.misc.NavigationCallbacks;
import com.caseybrooks.scripturememory.notifications.MainNotification;
import com.caseybrooks.scripturememory.views.NotificationVerseCard;
import com.caseybrooks.scripturememory.views.VOTDCard;
import com.caseybrooks.scripturememory.views.VerseInputCard;

public class DashboardFragment extends Fragment {
//Data Members
//------------------------------------------------------------------------------
	Context context;
	View view;

	LinearLayout dashboardLayout;
    NavigationCallbacks mCallbacks;
	
	NotificationVerseCard notify_card;
	VerseInputCard input_card;
	VOTDCard votd_card;
	
	RefreshReceiver receiver;
	
//Lifecycle and Initialization
//------------------------------------------------------------------------------
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        context = getActivity();
        initialize();

        return view;
    }
	
	@Override
	public void onResume() {
		super.onResume();
	    receiver = new RefreshReceiver();
		context.registerReceiver(receiver, new IntentFilter(REFRESH));

	    notify_card.refresh();
		if(MetaSettings.getNotificationActive(context)) {
		    MainNotification.notify(context).show();
		}

        ActionBar ab = ((MainActivity) context).getSupportActionBar();
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.color_toolbar, typedValue, true);
        int color = typedValue.data;
        ColorDrawable colorDrawable = new ColorDrawable(color);
        ab.setBackgroundDrawable(colorDrawable);
        ab.setTitle("Dashboard");
    }

	@Override
	public void onPause() {
		super.onPause();
		context.unregisterReceiver(receiver);
	}

	private void initialize() {
		dashboardLayout = (LinearLayout) view.findViewById(R.id.dashboardLayout);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			setLayoutTransition();
		}

		notify_card = new NotificationVerseCard(context);
		dashboardLayout.addView(notify_card);

		input_card = new VerseInputCard(context);
        input_card.setVisibility(View.GONE);
        View.OnFocusChangeListener inputTextboxListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    final ScrollView sv = (ScrollView) dashboardLayout.getParent();
                    final int y_pos;
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        y_pos = (int) input_card.getY();
                    }
                    else {
                        y_pos = 0;
                    }
                    sv.smoothScrollTo(0, y_pos);
                }
            }
        };
        input_card.editReference.setOnFocusChangeListener(inputTextboxListener);
        input_card.editVerse.setOnFocusChangeListener(inputTextboxListener);

		votd_card = new VOTDCard(context);
        votd_card.setVisibility(View.VISIBLE);
        dashboardLayout.addView(votd_card, 1);

        setHasOptionsMenu(true);
        receiveImplicitIntent();
	}


	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void setLayoutTransition() {
		LayoutTransition transition = new LayoutTransition();

        transition.enableTransitionType(LayoutTransition.CHANGING);

        transition.setAnimator(LayoutTransition.DISAPPEARING, Util.Animations.slideOutRight(getActivity()));
        transition.setAnimator(LayoutTransition.APPEARING, Util.Animations.slideInLeft(getActivity()));

        dashboardLayout.setLayoutTransition(transition);

	}

	public void receiveImplicitIntent() {
		Bundle extras = getArguments();
		if(extras != null && extras.containsKey(Intent.EXTRA_TEXT)) {
			String message = extras.getString(Intent.EXTRA_TEXT);

			if(input_card.getVisibility() == View.GONE) {
                input_card.setVisibility(View.VISIBLE);
                dashboardLayout.addView(input_card, 1);
                input_card.setVerse(message);
            }
            else {
            	input_card.setVerse(message);
            }
		}
	}

//ActionBar
//------------------------------------------------------------------------------
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater = getActivity().getMenuInflater();
	    inflater.inflate(R.menu.menu_dashboard, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.menu_dashboard_add:
	    	if(input_card.getVisibility() == View.GONE) {
                input_card.setVisibility(View.VISIBLE);
                dashboardLayout.addView(input_card, 1);
            }
            else {
                input_card.setVisibility(View.GONE);
                dashboardLayout.removeView(input_card);
            }
	    	return true;
	    case R.id.menu_dashboard_votd:
	    	if(votd_card.getVisibility() == View.GONE) {
                votd_card.setVisibility(View.VISIBLE);
                dashboardLayout.addView(votd_card, 1);
			}
			else {
				votd_card.setVisibility(View.GONE);
                dashboardLayout.removeView(votd_card);
			}
	    	return true;
        default:
            return super.onOptionsItemSelected(item);
	    }
	}

//Broadcast Receiver to update NotificationVerseCard upon user hitting "next"
//	in main notification
//------------------------------------------------------------------------------
	public static final String REFRESH = "com.caseybrooks.scripturememory.fragments.DashboardFragment.DASHBOARD_REFRESH";

 	public class RefreshReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			notify_card.refresh();
		}
	}
	
//Host Activity Interface
//------------------------------------------------------------------------------
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }
}
