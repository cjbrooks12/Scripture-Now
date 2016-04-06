package com.caseybrooks.common.features.practice;


import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.caseybrooks.androidbibletools.basic.AbstractVerse;
import com.caseybrooks.androidbibletools.basic.Reference;
import com.caseybrooks.androidbibletools.providers.simple.SimplePassage;
import com.caseybrooks.common.R;
import com.caseybrooks.common.app.AppFeature;
import com.caseybrooks.common.app.FragmentBase;
import com.caseybrooks.common.app.WordStyle;
import com.caseybrooks.common.databinding.DialogPracticefragmentSettingsBinding;
import com.caseybrooks.common.util.CancelDialogAction;
import com.caseybrooks.common.util.GridSpacingItemDecoration;
import com.caseybrooks.common.widget.FlowLayoutManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Random;

public class PracticeFragment extends FragmentBase implements PracticeFragmentSettingsModel.OnModelChangedListener {
    public static PracticeFragment newInstance(AbstractVerse passage) {
        PracticeFragment fragment = new PracticeFragment();
        Bundle args = new Bundle();
        String ref = passage.getReference().toString();
        String text = passage.getFormattedText();
        args.putString("reference", ref);
        args.putString("text", text);
        args.putString("id", passage.getId());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Pair<AppFeature, Integer> getFeatureForFragment() {
        return new Pair<>(AppFeature.Practice, 0);
    }

//Data Members
//--------------------------------------------------------------------------------------------------
    private static class WordStatus {
        public String word;
        public int guesses;
        public boolean userChosen;
        public boolean isHidden;
    }

    SimplePassage passage;
    TextView reference;

    int progress;
    ArrayList<WordStatus> words;
    AlertDialog settingsDialog;

    RecyclerView wordsList;
    WordsAdapter wordsAdapter;

    RecyclerView choicesList;
    ChoiceAdapter choicesAdapter;
    Random randomizer;
    int seed;

    RelativeLayout typingLayout;
    EditText typingInput;
    Button typingSubmitButton;

    PracticeFragmentSettingsModel settingsModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(getArguments() != null) {
            Reference ref = new Reference.Builder().parseReference(getArguments().getString("reference")).create();
            passage = new SimplePassage(ref);
            passage.setText(getArguments().getString("text"));
            passage.setId(getArguments().getString("id"));

            words = new ArrayList<>();
        }

        settingsModel = new PracticeFragmentSettingsModel(getContext());

        View view = inflater.inflate(R.layout.fragment_practice, container, false);

        randomizer = new Random(Calendar.getInstance().getTimeInMillis());
        seed = 0;

        reference = (TextView) view.findViewById(R.id.reference);

        choicesList = (RecyclerView) view.findViewById(R.id.inputMethod_multipleChoice);
        choicesList.setLayoutManager(new GridLayoutManager(getContext(), 3));
        choicesList.addItemDecoration(new GridSpacingItemDecoration(getContext(), 3, 8, 8, true));
        choicesAdapter = new ChoiceAdapter();
        choicesList.setAdapter(choicesAdapter);

        wordsList = (RecyclerView) view.findViewById(R.id.wordsList);
        wordsList.setLayoutManager(new FlowLayoutManager(getContext()));
        wordsAdapter = new WordsAdapter();
        wordsList.setAdapter(wordsAdapter);

        typingLayout = (RelativeLayout) view.findViewById(R.id.inputMethod_typing);
        typingInput = (EditText) view.findViewById(R.id.inputMethod_typing_edittext);
        typingSubmitButton = (Button) view.findViewById(R.id.inputMethod_typing_button);
        typingSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(typingInput.getText())) {
                    typingInput.setError("Cannot be empty");
                    return;
                }
                else {
                    submitWord(typingInput.getText().toString());
                    typingInput.setText("");
                }
            }
        });

        initializeSettingsDialog();

        if(settingsModel.getInputMethod() == 0) {
            choicesList.setVisibility(View.VISIBLE);
            typingLayout.setVisibility(View.GONE);
        }
        else if(settingsModel.getInputMethod() == 1) {
            choicesList.setVisibility(View.GONE);
            typingLayout.setVisibility(View.VISIBLE);
        }

        if(passage != null) {
            reference.setText(passage.getReference().toString());
            reset();
        }

        return view;
    }

//Gameplay
//--------------------------------------------------------------------------------------------------
    private void reset() {
        String[] passageWords = passage.getText().split("\\s");
        Random randomWords = new Random(passage.getReference().hashCode() + seed);
        words.clear();

        for(String word : passageWords) {
            int randomness = randomWords.nextInt(100);

            WordStatus wordStatus = new WordStatus();
            wordStatus.word = word;
            wordStatus.isHidden = (randomness <= settingsModel.getRandomThreshold()) ? true : false;
            wordStatus.guesses = 0;
            wordStatus.userChosen = false;

            words.add(wordStatus);
        }

        progress = -1;
        advanceProgress();
    }

    private void submitWord(String word) {
        String correctWord = words.get(progress).word.replaceAll("\\W", "");
        words.get(progress).guesses++;

        if(word.equalsIgnoreCase(correctWord)) {
            advanceProgress();
        }
        else {
            if(settingsModel.isReshuffleOnMiss()) {
                choicesAdapter.setCorrectWord(words.get(progress).word);
            }
        }
    }

    private void advanceProgress() {
        if(progress >= 0 && progress < words.size()) {
            //set word to show that it has been selected by the user, and whether that selection was correct
            words.get(progress).userChosen = true;
        }

        if(progress < words.size()) {
            if(settingsModel.isEnterVisibleWords()) {
                progress++;
                if(progress == words.size()) {
                    finishGame();
                }
            }
            else {
                int i = 0;
                for(WordStatus wordStatus : words) {
                    if(i > progress && wordStatus.isHidden) {
                        progress = i;
                        break;
                    }

                    i++;
                }
                if(i == words.size()) {
                    progress++;
                    finishGame();
                }
            }
        }

        updateText();
    }

    private void finishGame() {
        Toast.makeText(getContext(), "Verse Completed", Toast.LENGTH_SHORT).show();
    }

    private void updateText() {

        if(progress > words.size()) {
            progress = words.size();
            choicesAdapter.setCorrectWord(null);
        }
        else if(progress < 0) {
            progress = 0;
            choicesAdapter.setCorrectWord(words.get(0).word);
        }
        else {
            if(progress < words.size()) {
                choicesAdapter.setCorrectWord(words.get(progress).word);
            }
        }

        Random randomWords = new Random(passage.getReference().hashCode() + seed);
        for(WordStatus wordStatus : words) {
            int randomness = randomWords.nextInt(100);

            wordStatus.isHidden = (randomness <= settingsModel.getRandomThreshold()) ? true : false;
        }

        wordsAdapter.notifyDataSetChanged();
    }

//Words Adapter
//--------------------------------------------------------------------------------------------------

    private class WordsViewHolder extends RecyclerView.ViewHolder {
        TextView text;

        public WordsViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView;
        }

        public void onBind(WordStatus item, int position) {

            if(position >= progress) {
                if(position == progress) {
                    text.setTextColor(Color.BLUE);
                }
                else if(item.isHidden && settingsModel.getWordStyle() == WordStyle.Dimmed.getId()) {
                    text.setTextColor(Color.LTGRAY);
                }
                else {
                    text.setTextColor(Color.BLACK);
                }

                if(item.isHidden) {
                    text.setText(WordStyle.getWordStyleFromId(settingsModel.getWordStyle()).convert(item.word) + " ");
                }
                else {
                    text.setText(item.word + " ");
                }
            }
            else {
                if(item.userChosen) {
                    if(item.guesses == 1) {
                        text.setTextColor(Color.GREEN);
                    }
                    else {
                        text.setTextColor(Color.RED);
                    }
                }
                else {
                    text.setTextColor(Color.BLACK);
                }

                text.setText(item.word + " ");
            }
        }
    }

    private class WordsAdapter extends RecyclerView.Adapter<WordsViewHolder> {
        public WordsAdapter() {
        }

        @Override
        public WordsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView itemView = new TextView(getContext());
            itemView.setTypeface(Typeface.create("monospace", Typeface.NORMAL));
            itemView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            return new WordsViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(WordsViewHolder holder, int position) {
            holder.onBind(words.get(position), position);
        }

        @Override
        public int getItemCount() {
            return words.size();
        }
    }

//Settings Dialog
//--------------------------------------------------------------------------------------------------
    public void initializeSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivityBase());
        builder.setTitle("Settings");

        DialogPracticefragmentSettingsBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(getActivityBase()),
                R.layout.dialog_practicefragment_settings,
                null, false);
        binding.setSettingsModel(settingsModel);

        View view = binding.getRoot();
        settingsModel.initializeBinding(binding, this);

        builder.setView(view);
        builder.setNegativeButton("Close", new CancelDialogAction());
        settingsDialog = builder.create();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_practice_fragment, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.settings) {
            settingsDialog.show();
            return true;
        }
        else if(item.getItemId() == R.id.skip) {
            advanceProgress();
            return true;
        }
        else if(item.getItemId() == R.id.shuffle) {
            seed++;
            updateText();
            return true;
        }
        else if(item.getItemId() == R.id.reset) {
            reset();
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void onModelChanged() {
        updateText();

        if(settingsModel.getInputMethod() == 0) {
            choicesList.setVisibility(View.VISIBLE);
            choicesAdapter.setCorrectWord(words.get(progress).word);

            typingLayout.setVisibility(View.GONE);
        }
        else if(settingsModel.getInputMethod() == 1) {
            choicesList.setVisibility(View.GONE);
            typingLayout.setVisibility(View.VISIBLE);
        }
    }

//Multiple Choice Adapter
//--------------------------------------------------------------------------------------------------
    private static class WordChoice {
        public String word;
        public boolean isCorrectChoice;
        public boolean hasBeenGuessed;
    }

    private class ChoiceViewHolder extends RecyclerView.ViewHolder {
        TextView text;

        public ChoiceViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.chip);
        }

        public void onBind(final WordChoice choice) {
            text.setText(choice.word);
            text.setEnabled(!choice.hasBeenGuessed);
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    submitWord(choice.word);
                    choice.hasBeenGuessed = true;

                    choicesAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private String getRandomWord() {

        //try ten times to get a random word from the verse that does not match any in the list
        for(int i = 0; i < 10; i++) {
            int rand = randomizer.nextInt(words.size());
            String randomWord = words.get(rand).word.replaceAll("\\W+", "").toLowerCase();

            boolean foundWordInList = true;
            for(WordChoice choice : choicesAdapter.choices) {
                if(randomWord.equals(choice.word)) {
                    foundWordInList = true;
                    break;
                }
                else {
                    foundWordInList = false;
                }
            }
            if(!foundWordInList) {
                return randomWord;
            }
        }

        //if, after 10 tries, the random word always matches words we already have, try
        //to just get a word from the beginning of the verse and move forward until we
        //don't match. If we still can't find a non-match, then stop adding words, we
        //cannot add any more words that don't match.
        for(WordStatus word : words) {
            String randomWord = word.word.replaceAll("\\W+", "").toLowerCase();

            boolean foundWordInList = true;
            for(WordChoice choice : choicesAdapter.choices) {
                if(randomWord.equals(choice.word)) {
                    foundWordInList = true;
                    break;
                }
                else {
                    foundWordInList = false;
                }
            }
            if(!foundWordInList) {
                return randomWord;
            }
        }

        return null;
    }

    /*
    TODO: Intelligently pick words for the choices. Build an index of all the words in the players'
    TODO: library, and use TF-IDF scores to pick words that have a similar score to the correct one.
    TODO: Also take word length into account, especially if the user has each word visible as dashes
    TODO: which indicate word length.
     */
    private class ChoiceAdapter extends RecyclerView.Adapter<ChoiceViewHolder> {
        ArrayList<WordChoice> choices;

        public ChoiceAdapter() {
            this.choices = new ArrayList<>();
        }

        public void setCorrectWord(String word) {
            choices.clear();

            if(word == null) {
                return;
            }
            else {
                for(int i = 0; i < settingsModel.getWordCount(); i++) {
                    if(i == 0) {
                        WordChoice choice = new WordChoice();
                        choice.word = word.replaceAll("\\W+", "").toLowerCase();
                        choice.isCorrectChoice = true;
                        choice.hasBeenGuessed = false;
                        choices.add(choice);
                    }
                    else {
                        String randomWord = getRandomWord();

                        if(TextUtils.isEmpty(randomWord)) {
                            break;
                        }
                        else {
                            WordChoice choice = new WordChoice();
                            choice.word = randomWord;
                            choice.isCorrectChoice = false;
                            choice.hasBeenGuessed = false;
                            choices.add(choice);
                        }
                    }
                }

                Collections.shuffle(choices, randomizer);
            }

            notifyDataSetChanged();
        }

        @Override
        public ChoiceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_chip, null, false);
            return new ChoiceViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ChoiceViewHolder holder, int position) {
            holder.onBind(choices.get(position));
        }

        @Override
        public int getItemCount() {
            return choices.size();
        }
    }
}
