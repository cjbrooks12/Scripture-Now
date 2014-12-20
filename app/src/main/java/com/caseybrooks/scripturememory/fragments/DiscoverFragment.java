package com.caseybrooks.scripturememory.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.activities.MainActivity;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.Util;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.misc.NavigationCallbacks;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;

public class DiscoverFragment extends Fragment {
    Context context;

    EditText searchEditText;
    LinearLayout verseLayout;
    NavigationCallbacks mCallbacks;
    ProgressBar progress;

    public static DiscoverFragment newInstance() {
        DiscoverFragment fragment = new DiscoverFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public DiscoverFragment() {
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
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        this.context = getActivity();

        verseLayout = (LinearLayout) view.findViewById(R.id.discoverVerseLayout);
        progress = (ProgressBar) view.findViewById(R.id.progress);

        ColorFilter filter = new PorterDuffColorFilter(Color.parseColor("#508a4c"), PorterDuff.Mode.SRC_IN);

        progress.getProgressDrawable().setColorFilter(filter);
        progress.getIndeterminateDrawable().setColorFilter(filter);

        searchEditText = (EditText) view.findViewById(R.id.discoverEditText);
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String text = searchEditText.getText().toString();
                    if(text.length() > 1) {
                        new SearchVerseAsync().execute(text);
                        return true;
                    }
                }
                return false;
            }
        });

        return view;
    }

    private class Data {
        Passage passage;
        String searchTerm;
        int upVotes;
    }

    private class SearchVerseAsync extends AsyncTask<String, Data, Void> {
        String message;
        int count;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            count = 0;

            progress.setVisibility(View.VISIBLE);
            progress.setIndeterminate(true);
            progress.setProgress(0);
        }

        @Override
        protected void onProgressUpdate(Data... data) {
            super.onProgressUpdate(data);

            progress.setProgress(count);
            View view = LayoutInflater.from(context).inflate(R.layout.open_bible_verse_card, null);

            TextView reference = (TextView) view.findViewById(R.id.reference);
            TextView version = (TextView) view.findViewById(R.id.version);
            TextView verse = (TextView) view.findViewById(R.id.verse);
            TextView helpful = (TextView) view.findViewById(R.id.upVotes);

            final Data currentItem = data[0];

            reference.setText(currentItem.passage.getReference().toString());
            version.setText(currentItem.passage.getVersion().getCode());
            verse.setText(currentItem.passage.getText());
            helpful.setText(currentItem.upVotes + " helpful votes");

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final View view = LayoutInflater.from(context).inflate(R.layout.popup_add_verse, null);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setView(view);

                    final AlertDialog dialog = builder.create();

                    TextView cancelButton = (TextView) view.findViewById(R.id.cancel_button);
                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.cancel();
                        }
                    });
                    TextView addVerseButton = (TextView) view.findViewById(R.id.add_verse_button);
                    addVerseButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            VerseDB db = new VerseDB(context).open();
                            Passage passage = currentItem.passage;
                            passage.setState(VerseDB.CURRENT_NONE);
                            passage.addTag(currentItem.searchTerm);
                            db.insertVerse(passage);
                            dialog.cancel();
                        }
                    });

                    dialog.show();
                }
            });

            verseLayout.addView(view);
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                if(Util.isConnected(context)) {
                    String query = "http://www.openbible.info/topics/" +
                            params[0].trim().replaceAll(" ", "_");

                    Document doc = Jsoup.connect(query).get();
                    Elements passages = doc.select(".verse");
                    progress.setIndeterminate(false);

                    for(Element element : passages) {
                        if(count > 9) break; //only get the first 10 verses
                        count++;

                        try {
                            Passage passage = new Passage(element.select(".bibleref").first().ownText());
                            passage.setVersion(MetaSettings.getBibleVersion(context));
                            passage.setText(element.select("p").get(1).text());
                            passage.setVersion(MetaSettings.getBibleVersion(context));
                            passage.retrieve();

                            Data data = new Data();
                            data.passage = passage;
                            String notesString = element.select(".note").get(0).ownText();
                            data.upVotes = Integer.parseInt(notesString.replaceAll("\\D", ""));
                            data.searchTerm = params[0].trim();

                            publishProgress(data);
                        }
                        catch(ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    message = "Finished";
                }
                else {
                    message = "Cannot search, no internet connection";
                }
            }
            catch(IllegalArgumentException e1) {
                message = "Verse does not exist or reference is not formatted properly";
            }
            catch(IOException e2) {
                message = "Error while retrieving verse";
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            progress.setVisibility(View.GONE);
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
