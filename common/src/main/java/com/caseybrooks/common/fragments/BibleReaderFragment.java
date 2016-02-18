package com.caseybrooks.common.fragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.caseybrooks.androidbibletools.ABT;
import com.caseybrooks.androidbibletools.basic.Bible;
import com.caseybrooks.androidbibletools.basic.Reference;
import com.caseybrooks.androidbibletools.data.OnResponseListener;
import com.caseybrooks.androidbibletools.providers.abs.ABSBible;
import com.caseybrooks.androidbibletools.providers.abs.ABSBibleList;
import com.caseybrooks.androidbibletools.providers.abs.ABSPassage;
import com.caseybrooks.androidbibletools.widget.BiblePickerDialog;
import com.caseybrooks.androidbibletools.widget.OnBibleSelectedListener;
import com.caseybrooks.androidbibletools.widget.OnReferenceCreatedListener;
import com.caseybrooks.androidbibletools.widget.VersePicker;
import com.caseybrooks.androidbibletools.widget.VersePickerDialog;
import com.caseybrooks.androidbibletools.widget.VerseView;
import com.caseybrooks.common.R;
import com.caseybrooks.common.app.AppFeature;
import com.caseybrooks.common.app.FragmentBase;

//TODO: (ABT) Restyle selectionMode button
//TODO: (ABT) use flags for defaults within Builder to know how to display a Reference with .toString()
//TODO: (ABT) pass flags from Builder to Reference, or make a Reference have a constant toString() value that is created by the Builder
public class BibleReaderFragment extends FragmentBase {
    public static BibleReaderFragment newInstance() {
        BibleReaderFragment fragment = new BibleReaderFragment();
        Bundle data = new Bundle();
        fragment.setArguments(data);
        return fragment;
    }

    BiblePickerDialog biblePicker;
    VersePickerDialog versePicker;
    VerseView verseView;

    FloatingActionButton next, previous;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_biblereader, container, false);

        verseView = (VerseView) view.findViewById(R.id.verse_view);

        next = (FloatingActionButton) view.findViewById(R.id.nextButton);
        previous = (FloatingActionButton) view.findViewById(R.id.previousButton);

        biblePicker = new BiblePickerDialog();
        versePicker = new VersePickerDialog();
        versePicker.setSelectionMode(VersePicker.SELECTION_WHOLE_CHAPTER);
        versePicker.setAllowSelectionModeChange(false);

        biblePicker.setBibleListClass(ABSBibleList.class, null);
        biblePicker.setOnBibleSelectedListener(versePicker.getVersePicker());
        biblePicker.setOnBibleSelectedListener(new OnBibleSelectedListener() {
            @Override
            public void onBibleSelected(Bible bible) {
                biblePicker.dismiss();
                versePicker.show(getActivity().getSupportFragmentManager(), "Sample Fragment");

                if(versePicker.getVersePicker() != null)
                    versePicker.getVersePicker().onBibleSelected(bible);
            }
        });

        versePicker.setSelectedBibleTag(null);
        versePicker.setAllowSelectionModeChange(false);
        versePicker.setSelectionMode(VersePicker.SELECTION_WHOLE_CHAPTER);
        versePicker.setOnReferenceCreatedListener(new OnReferenceCreatedListener() {
            @Override
            public void onReferenceCreated(Reference.Builder reference) {
                getActivityBase().getToolbar().setSubtitle(reference.create().toString());
                getActivityBase().getSearchbox().setText(reference.create().toString());

                final ABSPassage passage = new ABSPassage(reference.create());
                passage.download(new OnResponseListener() {
                    @Override
                    public void responseFinished() {
                        ABT.getInstance(getContext()).saveVerse(passage, "bible_reader_fragment");
                        verseView.setVerse(passage);
                    }
                });
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reference.Builder nextChapter = verseView.getVerse().getReference().next(Reference.TYPE_CHAPTER);
                nextChapter.addAllVersesInChapter();
                verseView.setText("");

                final ABSPassage passage = new ABSPassage(nextChapter.create());
                getActivityBase().getToolbar().setSubtitle(passage.getReference().toString());
                passage.download(new OnResponseListener() {
                    @Override
                    public void responseFinished() {
                        ABT.getInstance(getContext()).saveVerse(passage, "bible_reader_fragment");
                        verseView.setVerse(passage);
                    }
                });
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reference.Builder nextChapter = verseView.getVerse().getReference().previous(Reference.TYPE_CHAPTER);
                nextChapter.addAllVersesInChapter();
                verseView.setText("");

                final ABSPassage passage = new ABSPassage(nextChapter.create());
                getActivityBase().getToolbar().setSubtitle(passage.getReference().toString());
                passage.download(new OnResponseListener() {
                    @Override
                    public void responseFinished() {
                        ABT.getInstance(getContext()).saveVerse(passage, "bible_reader_fragment");
                        verseView.setVerse(passage);
                    }
                });
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        final ABSPassage savedPassage = (ABSPassage) ABT.getInstance(getContext()).getSavedVerse("bible_reader_fragment");

        if(savedPassage != null) {
            String subtitle = savedPassage.getReference().toString();
            getActivityBase().getToolbar().setSubtitle(subtitle);

            ((ABSBible) savedPassage.getReference().getBible()).download(new OnResponseListener() {
                @Override
                public void responseFinished() {
                    savedPassage.download(new OnResponseListener() {
                        @Override
                        public void responseFinished() {
                            verseView.setVerse(savedPassage);
                            next.setEnabled(true);
                            previous.setEnabled(true);
                        }
                    });
                }
            });
        }
    }

    @Override
    public Pair<AppFeature, Integer> getFeatureForFragment() {
        return new Pair<>(AppFeature.Read, 0);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_lookup_verse, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.bible_picker) {
            biblePicker.show(getActivityBase().getSupportFragmentManager(), "");
            getActivityBase().getSearchbox().hideInstant(getActivityBase());
            return true;
        }
        else if(item.getItemId() == R.id.verse_picker) {
            versePicker.show(getActivityBase().getSupportFragmentManager(), "");
            getActivityBase().getSearchbox().hideInstant(getActivityBase());
            return true;
        }
        else {
            return false;
        }
    }
}
