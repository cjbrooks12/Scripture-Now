package com.caseybrooks.scripturememory.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.activities.MainActivity;
import com.caseybrooks.scripturememory.misc.NavigationCallbacks;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;

public class ImportVersesFragment extends Fragment {
    Context context;

    AutoCompleteTextView searchEditText;
    ArrayAdapter<String> suggestionsAdapter;

    LinearLayout verseLayout;
    NavigationCallbacks mCallbacks;
    ProgressBar progress;

    public static Fragment newInstance() {
        Fragment fragment = new ImportVersesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ImportVersesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar ab = ((MainActivity) context).getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(context.getResources().getColor(R.color.open_bible_brown));
        ab.setBackgroundDrawable(colorDrawable);
        ab.setTitle("Topical Bible");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_topical_bible, container, false);

        this.context = getActivity();

        verseLayout = (LinearLayout) view.findViewById(R.id.discoverVerseLayout);
        progress = (ProgressBar) view.findViewById(R.id.progress);

        ColorFilter filter = new PorterDuffColorFilter(Color.parseColor("#508a4c"), PorterDuff.Mode.SRC_IN);

        progress.getProgressDrawable().setColorFilter(filter);
        progress.getIndeterminateDrawable().setColorFilter(filter);

        String path = Environment.getExternalStorageDirectory().toString()+"/scripturememory";
        File f = new File(path);
        File file[] = f.listFiles();
        for (int i=0; i < file.length; i++) {
            try {
                Document doc = Jsoup.parse(file[i], null);

                if(doc.select("verses").size() > 0) {
                    View itemView = LayoutInflater.from(context).inflate(R.layout.list_open_bible, null);
                    TextView reference = (TextView) itemView.findViewById(R.id.reference);
                    TextView version = (TextView) itemView.findViewById(R.id.version);
                    TextView verse = (TextView) itemView.findViewById(R.id.verse);

                    reference.setText(doc.select("verses").attr("name"));
                    version.setText(doc.select("passage").size() + " Verses");
                    verse.setText(file[i].getPath());

                    verseLayout.addView(itemView);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        return view;
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
