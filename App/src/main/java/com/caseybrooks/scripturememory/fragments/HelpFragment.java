package com.caseybrooks.scripturememory.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.misc.NavigationCallbacks;
import com.caseybrooks.scripturememory.misc.PreferenceFragment;

public class HelpFragment extends PreferenceFragment {

    public static class ViewTopicFragment extends Fragment {
		NavigationCallbacks mCallbacks;

        public static Fragment newInstance(int resId) {
            Fragment fragment = new ViewTopicFragment();
            Bundle args = new Bundle();
            args.putInt("RES_ID", resId);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            int resId = getArguments().getInt("RES_ID", R.layout.help_overview);
            View view = inflater.inflate(resId, container, false);


			String title = "";

            if (resId == R.layout.help_overview) title = "Overview";
            else if (resId == R.layout.help_adding_verses) title = "Adding Verses";
            else if (resId == R.layout.help_memorization_state)  title = "Memorization State";
            else if (resId == R.layout.help_tags) title = "Tags";
            else if (resId == R.layout.help_changelog) title = "Changelog";
            else if (resId == R.layout.help_licenses) title = "Licenses";

			TypedValue typedValue = new TypedValue();
			Resources.Theme theme = getActivity().getTheme();
			theme.resolveAttribute(R.attr.color_toolbar, typedValue, true);

			mCallbacks.setToolBar(title, typedValue.data);

            return view;
        }

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

    Context context;
	NavigationCallbacks mCallbacks;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getActivity();

        addPreferencesFromResource(R.xml.help);

        findPreference("CONTACT_BUG").setOnPreferenceClickListener(bugClick);
        findPreference("CONTACT_FEATURE").setOnPreferenceClickListener(featureClick);
        findPreference("CONTACT_COMMENT").setOnPreferenceClickListener(commentClick);
        findPreference("RateThisApp").setOnPreferenceClickListener(rateAppClick);
        findPreference("ShareApp").setOnPreferenceClickListener(shareAppClick);
    }

    @Override
    public void onResume() {
        super.onResume();

		TypedValue typedValue = new TypedValue();
		Resources.Theme theme = context.getTheme();
		theme.resolveAttribute(R.attr.color_toolbar, typedValue, true);

		mCallbacks.setToolBar("Help", typedValue.data);

        MetaSettings.putDrawerSelection(context, 5, 0);
    }

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

    //Rate App (go to Play Store) listener
//------------------------------------------------------------------------------
    OnPreferenceClickListener rateAppClick = new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            final Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
            final Intent rateAppIntent = new Intent(Intent.ACTION_VIEW, uri);

            if (context.getPackageManager().queryIntentActivities(rateAppIntent, 0).size() > 0)
            {
                startActivity(rateAppIntent);
            }
            else
            {
                Toast.makeText(context, "Google Play not installed", Toast.LENGTH_LONG).show();
            }
            return false;
        }
    };

    OnPreferenceClickListener shareAppClick = new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            MetaSettings.putPromptOnStart(context, 4);

            String shareMessage = getResources().getString(R.string.share_message);
            Intent intent = new Intent();
            intent.setType("text/plain");
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Try Scripture Memory Notifications for Android");
            intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(intent, "Share To..."));

            return false;
        }
    };

    //Contact Developer Preference Listeners
//------------------------------------------------------------------------------
    OnPreferenceClickListener bugClick = new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {

            //Gather device information
            String info =
                    "Carrier: " +
                            Build.BRAND + " " +
                            Build.DEVICE + "\n" +
                            "Device: " +
                            Build.MANUFACTURER + " " +
                            Build.MODEL + "\n" +
                            "Kernel: " +
                            Build.DISPLAY + "\n" +
                            "----------------\n" +
                            "Describe Bug: ";

            Intent Email = new Intent(Intent.ACTION_SEND);
            Email.setType("getText/email");
            Email.putExtra(Intent.EXTRA_EMAIL, new String[] { "scripture.memory.app@gmail.com" });
            Email.putExtra(Intent.EXTRA_SUBJECT, "Scripture Memory Notifications: Report Bug");
            Email.putExtra(Intent.EXTRA_TEXT, info);
            startActivity(Intent.createChooser(Email, "Send Feedback:"));

            return false;
        }
    };

    OnPreferenceClickListener featureClick = new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {

            //Gather device information
            String info =
                    "Suggested Feature: ";

            Intent Email = new Intent(Intent.ACTION_SEND);
            Email.setType("getText/email");
            Email.putExtra(Intent.EXTRA_EMAIL, new String[] { "scripture.memory.app@gmail.com" });
            Email.putExtra(Intent.EXTRA_SUBJECT, "Scripture Memory Notifications: Suggest Feature");
            Email.putExtra(Intent.EXTRA_TEXT, info);
            startActivity(Intent.createChooser(Email, "Send Feedback:"));

            return false;
        }
    };

    OnPreferenceClickListener commentClick = new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {

            //Gather device information
            String info =
                    "Comments: ";

            Intent Email = new Intent(Intent.ACTION_SEND);
            Email.setType("getText/email");
            Email.putExtra(Intent.EXTRA_EMAIL, new String[] { "scripture.memory.app@gmail.com" });
            Email.putExtra(Intent.EXTRA_SUBJECT, "Scripture Memory Notifications: Comment");
            Email.putExtra(Intent.EXTRA_TEXT, info);
            startActivity(Intent.createChooser(Email, "Send Feedback:"));

            return false;
        }
    };
}
