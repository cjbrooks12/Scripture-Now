package com.caseybrooks.scripturememory.nowcards.main;

import android.content.Context;
import android.util.Pair;

import com.caseybrooks.androidbibletools.basic.Metadata;
import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.data.Formatter;
import com.caseybrooks.androidbibletools.defaults.DefaultFormatter;
import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
import com.caseybrooks.scripturememory.activities.SNApplication;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.fragments.VerseListFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Random;

public class Main {
//Data Members
//------------------------------------------------------------------------------
    private Context context;
	private Passage mainPassage;
	private Passage activePassage;

	public SNApplication getApplication() {
		return (SNApplication) context.getApplicationContext();
	}

    public Main(Context context) {
        this.context = context;

		VerseDB db = new VerseDB(context).open();
		mainPassage = db.getVerse(MainSettings.getMainId(context));
		db.close();
    }

	public Passage getMainPassage() {
		return mainPassage;
	}

	public String getFormattedText() {
		if(mainPassage != null) {
			Formatter formatter;
			switch (MainSettings.getDisplayMode(context)) {
				case 0: formatter = new DefaultFormatter.Dashes(); break;
				case 1: formatter = new DefaultFormatter.FirstLetters(); break;
				case 2: formatter = new DefaultFormatter.DashedLetter(); break;
				case 3: formatter = new DefaultFormatter.RandomWords(
						MainSettings.getRandomness(context).first,
						MainSettings.getRandomness(context).second); break;
				default: formatter = new DefaultFormatter.Normal(); break;
			}

			mainPassage.setFormatter(formatter);
			String text = mainPassage.getText();
			mainPassage.setFormatter(new DefaultFormatter.Normal());
			return text;
		}
		else return null;
	}

	public String getNormalText() {
		if(mainPassage != null) {
			mainPassage.setFormatter(new DefaultFormatter.Normal());
			return mainPassage.getText();
		}
		else return null;
	}

    public void getNextVerse() {
        Pair<Integer, Integer> workingList = MainSettings.getWorkingList(context);
        if(workingList.first == -1) return;

        VerseDB db = new VerseDB(context);

        //get the active list of verses
        ArrayList<Passage> passages;
        if(workingList.first == VerseListFragment.STATE) {
            db.open();
            passages = db.getStateVerses(workingList.second);
            db.close();
        }
        else if(workingList.first == VerseListFragment.TAGS) {
            db.open();
            passages = db.getTaggedVerses(workingList.second);
            db.close();
        }
        else return;

        //sort the verses as chosen by the users
        Metadata.Comparator comparator;
        switch(MetaSettings.getSortBy(context)) {
            case 0: comparator = new Metadata.Comparator(DefaultMetaData.TIME_CREATED); break;
            case 1: comparator = new Metadata.Comparator(Metadata.Comparator.KEY_REFERENCE); break;
            case 2: comparator = new Metadata.Comparator(Metadata.Comparator.KEY_REFERENCE_ALPHABETICAL); break;
            case 3: comparator = new Metadata.Comparator(DefaultMetaData.STATE); break;
            default: comparator = new Metadata.Comparator(DefaultMetaData.ID); break;
        }
        Collections.sort(passages, comparator);

        //search through list to find the currently active list, and set the next verse to be the active verse
        for(int i = 0; i < passages.size(); i++) {
            if(passages.get(i).getMetadata().getInt(DefaultMetaData.ID) == mainPassage.getMetadata().getInt(DefaultMetaData.ID)) {
                if(i == passages.size() - 1) {
					mainPassage = passages.get(0);
                    break;
                }
                else {
					mainPassage = passages.get(i+1);
                    break;
                }
            }
        }

        getApplication().setCurrentPassage(mainPassage);
    }

	public void getPreviousVerse() {
		Pair<Integer, Integer> workingList = MainSettings.getWorkingList(context);
		if(workingList.first == -1) return;

		VerseDB db = new VerseDB(context);

		//get the active list of verses
		ArrayList<Passage> passages;
		if(workingList.first == VerseListFragment.STATE) {
			db.open();
			passages = db.getStateVerses(workingList.second);
			db.close();
		}
		else if(workingList.first == VerseListFragment.TAGS) {
			db.open();
			passages = db.getTaggedVerses(workingList.second);
			db.close();
		}
		else return;

		//sort the verses as chosen by the users
		Metadata.Comparator comparator;
		switch(MetaSettings.getSortBy(context)) {
		case 0: comparator = new Metadata.Comparator(DefaultMetaData.TIME_CREATED); break;
		case 1: comparator = new Metadata.Comparator(Metadata.Comparator.KEY_REFERENCE); break;
		case 2: comparator = new Metadata.Comparator(Metadata.Comparator.KEY_REFERENCE_ALPHABETICAL); break;
		case 3: comparator = new Metadata.Comparator(DefaultMetaData.STATE); break;
		default: comparator = new Metadata.Comparator(DefaultMetaData.ID); break;
		}
		Collections.sort(passages, comparator);

		//search through list to find the currently active list, and set the next verse to be the active verse
		for(int i = 0; i < passages.size(); i++) {
			if(passages.get(i).getMetadata().getInt(DefaultMetaData.ID) == mainPassage.getMetadata().getInt(DefaultMetaData.ID)) {
				if(i == 0) {
					mainPassage = passages.get(passages.size()-1);
					break;
				}
				else {
					mainPassage = passages.get(i-1);
					break;
				}
			}
		}

		getApplication().setCurrentPassage(mainPassage);
	}

	public void getRandomVerse() {
		Pair<Integer, Integer> workingList = MainSettings.getWorkingList(context);
		if(workingList.first == -1) return;

		VerseDB db = new VerseDB(context);

		//get the active list of verses
		ArrayList<Passage> passages;
		if(workingList.first == VerseListFragment.STATE) {
			db.open();
			passages = db.getStateVerses(workingList.second);
			db.close();
		}
		else if(workingList.first == VerseListFragment.TAGS) {
			db.open();
			passages = db.getTaggedVerses(workingList.second);
			db.close();
		}
		else return;

		//sort the verses as chosen by the users
		Metadata.Comparator comparator;
		switch(MetaSettings.getSortBy(context)) {
		case 0: comparator = new Metadata.Comparator(DefaultMetaData.TIME_CREATED); break;
		case 1: comparator = new Metadata.Comparator(Metadata.Comparator.KEY_REFERENCE); break;
		case 2: comparator = new Metadata.Comparator(Metadata.Comparator.KEY_REFERENCE_ALPHABETICAL); break;
		case 3: comparator = new Metadata.Comparator(DefaultMetaData.STATE); break;
		default: comparator = new Metadata.Comparator(DefaultMetaData.ID); break;
		}
		Collections.sort(passages, comparator);

		//search through list to find the currently active list, and set the next verse to be the active verse
		if(passages.size() > 1) {
			Passage randomVerse;
			Random random = new Random(Calendar.getInstance().getTimeInMillis());
			while(true) {
				randomVerse = passages.get(random.nextInt(passages.size()));
				if(randomVerse.getMetadata().getInt(DefaultMetaData.ID) != mainPassage.getMetadata().getInt(DefaultMetaData.ID)) {
					break;
				}
			}
			getApplication().setCurrentPassage(randomVerse);
		}
	}
}
