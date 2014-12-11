package com.caseybrooks.scripturememory.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.Util;

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

    private class SearchVerseAsync extends AsyncTask<String, Passage, Void> {
        String message;

        @Override
        protected void onProgressUpdate(Passage... values) {
            super.onProgressUpdate(values);

            CardView cv = new CardView(context);
            LinearLayout cardLayout = new LinearLayout(context);
            cardLayout.setOrientation(LinearLayout.VERTICAL);
            TextView title = new TextView(context);
            title.setTextSize(20);
            title.setText(values[0].getReference().toString());

            TextView text = new TextView(context);
            text.setTextSize(15);
            text.setText(values[0].getText());

            cardLayout.addView(title);
            cardLayout.addView(text);
            cv.addView(cardLayout);

            verseLayout.addView(cv);
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                if(Util.isConnected(context)) {
                    String query = "http://www.openbible.info/topics/" +
                            params[0].trim().replaceAll(" ", "_");

                    Document doc = Jsoup.connect(query).get();

                    Elements passages = doc.select(".verse");

                    int count = 0;

                    for(Element element : passages) {
                        if(count > 10) break; //only get the first 10 verses

                        try {
                            Passage passage = new Passage(element.select(".bibleref").first().ownText());
                            passage.setVersion(MetaSettings.getBibleVersion(context));
                            passage.retrieve();
                            publishProgress(passage);
                            count++;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        this.context = getActivity();

        verseLayout = (LinearLayout) view.findViewById(R.id.discoverVerseLayout);

        searchEditText = (EditText) view.findViewById(R.id.discoverEditText);
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    new SearchVerseAsync().execute(searchEditText.getText().toString());
                    return true;
                }
                return false;
            }
        });

        return view;
    }
}
