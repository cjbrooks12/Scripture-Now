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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import java.util.ArrayList;

public class TopicalBibleFragment extends Fragment {
    Context context;

    AutoCompleteTextView searchEditText;
    ArrayAdapter<String> suggestionsAdapter;

    LinearLayout verseLayout;
    NavigationCallbacks mCallbacks;
    ProgressBar progress;

    public static TopicalBibleFragment newInstance() {
        TopicalBibleFragment fragment = new TopicalBibleFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public TopicalBibleFragment() {
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

        searchEditText = (AutoCompleteTextView) view.findViewById(R.id.discoverEditText);
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String text = searchEditText.getText().toString();
                    if (text.length() > 1) {
                        new SearchVerseAsync().execute(text);
                        return true;
                    }
                }
                return false;
            }
        });
        suggestionsAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1);
        searchEditText.setAdapter(suggestionsAdapter);
        searchEditText.addTextChangedListener(new TextWatcher() {
            Character searchedChar;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i2, int i3) {
                if(searchedChar == null || (s.length() > 0 && s.charAt(0) != searchedChar)) {
                    new GetSuggestionsAsync().execute(s.charAt(0));
                    searchedChar = s.charAt(0);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        searchEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String s = suggestionsAdapter.getItem(position);
                new SearchVerseAsync().execute(s);
            }
        });


        return view;
    }

    private class Data {
        Passage passage;
        String searchTerm;
        int upVotes;
    }

    private class GetSuggestionsAsync extends AsyncTask<Character, Void, Void> {
        String message;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setVisibility(View.VISIBLE);
            progress.setIndeterminate(true);
            progress.setProgress(0);
            suggestionsAdapter.clear();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progress.setVisibility(View.GONE);
            int threshold = searchEditText.getThreshold();
            searchEditText.setThreshold(1);
            searchEditText.showDropDown();
            searchEditText.setThreshold(threshold);
        }

        @Override
        protected Void doInBackground(Character... params) {
            try {
                if(Util.isConnected(context)) {
                    for(Character character : params) {
                        String query = "http://www.openbible.info/topics/" + character;

                        Document doc = Jsoup.connect(query).get();
                        Elements passages = doc.select("li");

                        for (Element element : passages) {

                            suggestionsAdapter.add(element.text());
                            suggestionsAdapter.notifyDataSetChanged();
                        }
                    }
                    message = "Finished";
                }
                else {
                    message = "Cannot search, no internet connection";
                }
            }
            catch(IOException e2) {
                message = "Error while retrieving verse";
            }





            return null;
        }
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
