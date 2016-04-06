package com.caseybrooks.common.features.practice;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.caseybrooks.common.BR;
import com.caseybrooks.common.R;
import com.caseybrooks.common.app.AppSettings;
import com.caseybrooks.common.app.WordStyle;
import com.caseybrooks.common.databinding.DialogPracticefragmentSettingsBinding;

public class PracticeFragmentSettingsModel extends BaseObservable {
    private static final String PRACTICE_WORD_COUNT = "PRACTICE_WORD_COUNT";
    private static final String PRACTICE_RANDOM_THRESHOLD = "PRACTICE_RANDOM_THRESHOLD";
    private static final String PRACTICE_WORD_STYLE = "PRACTICE_WORD_STYLE";
    private static final String PRACTICE_INPUT_METHOD = "PRACTICE_INPUT_METHOD";

    private static final String PRACTICE_RESHUFFLE_ON_MISS = "PRACTICE_RESHUFFLE_ON_MISS";
    private static final String PRACTICE_VERSE_WORDS_ONLY = "PRACTICE_VERSE_WORDS_ONLY";
    private static final String PRACTICE_ENTER_VISIBLE_WORDS = "PRACTICE_ENTER_VISIBLE_WORDS";
    private static final String PRACTICE_CASE_SENSITIVE = "PRACTICE_CASE_SENSITIVE";

    Context context;
    SharedPreferences prefs;
    OnModelChangedListener listener;

    //Bindable attributes
    private int wordCount;
    private int randomThreshold;
    private int wordStyle;
    private int inputMethod;

    private boolean reshuffleOnMiss;
    private boolean verseWordsOnly;
    private boolean enterVisibleWords;
    private boolean caseSensitive;

//Model initialization
//--------------------------------------------------------------------------------------------------
    public PracticeFragmentSettingsModel(@NonNull Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(AppSettings.SettingsFile, 0);

        loadSettings();
    }

    public void loadSettings() {
        wordCount = prefs.getInt(PRACTICE_WORD_COUNT, 4);
        randomThreshold = prefs.getInt(PRACTICE_RANDOM_THRESHOLD, 50);
        wordStyle = prefs.getInt(PRACTICE_WORD_STYLE, 0);
        inputMethod = prefs.getInt(PRACTICE_INPUT_METHOD, 0);

        reshuffleOnMiss = prefs.getBoolean(PRACTICE_RESHUFFLE_ON_MISS, true);
        verseWordsOnly = prefs.getBoolean(PRACTICE_VERSE_WORDS_ONLY, true);
        enterVisibleWords = prefs.getBoolean(PRACTICE_ENTER_VISIBLE_WORDS, true);
        caseSensitive = prefs.getBoolean(PRACTICE_CASE_SENSITIVE, true);

        notifyChange();
    }

    public void initializeBinding(@NonNull DialogPracticefragmentSettingsBinding binding, OnModelChangedListener listener) {
        this.listener = listener;

        binding.wordCountSeekbar.setProgress(getWordCount() - 4);
        binding.randomThresholdSeekbar.setProgress(getRandomThreshold());

        if(getInputMethod() == 0) {
            binding.inputMethodRadiogroup.check(R.id.radio_multiple_choice);
        }
        else if(getInputMethod() == 1) {
            binding.inputMethodRadiogroup.check(R.id.radio_typing);
        }

        if(getWordStyle() == WordStyle.Dashes.getId()) {
            binding.wordStyleRadiogroup.check(R.id.radio_dashes);
        }
        else if(getWordStyle() == WordStyle.Letters.getId()) {
            binding.wordStyleRadiogroup.check(R.id.radio_letters);
        }
        else if(getWordStyle() == WordStyle.DashedLetters.getId()) {
            binding.wordStyleRadiogroup.check(R.id.radio_dashed_letters);
        }
        else if(getWordStyle() == WordStyle.Dimmed.getId()) {
            binding.wordStyleRadiogroup.check(R.id.radio_dimmed);
        }
        else if(getWordStyle() == WordStyle.Missing.getId()) {
            binding.wordStyleRadiogroup.check(R.id.radio_missing);
        }

        binding.reshuffleOnMiss.setChecked(isReshuffleOnMiss());
        binding.verseWordsOnly.setChecked(isVerseWordsOnly());
        binding.enterVisibleWords.setChecked(isEnterVisibleWords());
        binding.caseSensitive.setChecked(isCaseSensitive());
    }

//UI bind events
//--------------------------------------------------------------------------------------------------
    public void seekbarChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(seekBar.getId() == R.id.wordCountSeekbar) {
            wordCount = progress + 4;
            prefs.edit().putInt(PRACTICE_WORD_COUNT, wordCount).commit();
            notifyPropertyChanged(BR.wordCount);
        }
        else if(seekBar.getId() == R.id.randomThresholdSeekbar) {
            randomThreshold = progress;
            prefs.edit().putInt(PRACTICE_RANDOM_THRESHOLD, randomThreshold).commit();
            notifyPropertyChanged(BR.randomThreshold);
        }

        if(listener != null)
            listener.onModelChanged();
    }

    public void radioButtonChanged(RadioGroup group, int checkedId) {
        if(group.getId() == R.id.wordStyleRadiogroup) {
            if(checkedId == R.id.radio_dashes) {
                wordStyle = WordStyle.Dashes.getId();
            }
            else if(checkedId == R.id.radio_letters) {
                wordStyle = WordStyle.Letters.getId();
            }
            else if(checkedId == R.id.radio_dashed_letters) {
                wordStyle = WordStyle.DashedLetters.getId();
            }
            else if(checkedId == R.id.radio_dimmed) {
                wordStyle = WordStyle.Dimmed.getId();
            }
            else if(checkedId == R.id.radio_missing) {
                wordStyle = WordStyle.Missing.getId();
            }

            prefs.edit().putInt(PRACTICE_WORD_STYLE, wordStyle).commit();
            notifyPropertyChanged(BR.wordStyle);
        }
        else if(group.getId() == R.id.inputMethodRadiogroup) {
            if(checkedId == R.id.radio_multiple_choice) {
                inputMethod = 0;
            }
            else if(checkedId == R.id.radio_typing) {
                inputMethod = 1;
            }

            prefs.edit().putInt(PRACTICE_INPUT_METHOD, inputMethod).commit();
            notifyPropertyChanged(BR.inputMethod);
        }

        if(listener != null)
            listener.onModelChanged();
    }

    public void switchChanged(CompoundButton buttonView, boolean isChecked) {
        if(buttonView.getId() == R.id.reshuffleOnMiss) {
            reshuffleOnMiss = isChecked;
            prefs.edit().putBoolean(PRACTICE_RESHUFFLE_ON_MISS, reshuffleOnMiss).commit();
            notifyPropertyChanged(BR.reshuffleOnMiss);
        }
        else if(buttonView.getId() == R.id.verseWordsOnly) {
            verseWordsOnly = isChecked;
            prefs.edit().putBoolean(PRACTICE_VERSE_WORDS_ONLY, verseWordsOnly).commit();
            notifyPropertyChanged(BR.verseWordsOnly);
        }
        else if(buttonView.getId() == R.id.enterVisibleWords) {
            enterVisibleWords = isChecked;
            prefs.edit().putBoolean(PRACTICE_ENTER_VISIBLE_WORDS, enterVisibleWords).commit();
            notifyPropertyChanged(BR.enterVisibleWords);
        }
        else if(buttonView.getId() == R.id.caseSensitive) {
            caseSensitive = isChecked;
            prefs.edit().putBoolean(PRACTICE_CASE_SENSITIVE, caseSensitive).commit();
            notifyPropertyChanged(BR.caseSensitive);
        }

        if(listener != null)
            listener.onModelChanged();
    }

//Getters and Setters
//--------------------------------------------------------------------------------------------------
    @Bindable
    public int getWordCount() {
        return wordCount;
    }

    @Bindable
    public int getRandomThreshold() {
        return randomThreshold;
    }

    @Bindable
    public int getWordStyle() {
        return wordStyle;
    }

    @Bindable
    public boolean isReshuffleOnMiss() {
        return reshuffleOnMiss;
    }

    @Bindable
    public boolean isVerseWordsOnly() {
        return verseWordsOnly;
    }

    @Bindable
    public boolean isEnterVisibleWords() {
        return enterVisibleWords;
    }

    @Bindable
    public int getInputMethod() {
        return inputMethod;
    }

    @Bindable
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

//Listener
//--------------------------------------------------------------------------------------------------
    public interface OnModelChangedListener {
        void onModelChanged();
    }
}
