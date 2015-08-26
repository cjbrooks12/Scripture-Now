package com.caseybrooks.scripturememory.nowcards.main;

import android.content.Context;
import android.util.Pair;

import com.caseybrooks.androidbibletools.basic.Metadata;
import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.data.Formatter;
import com.caseybrooks.androidbibletools.defaults.DefaultFormatter;
import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.fragments.VerseListFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Random;

/**
 * Main is the verse that the user is currently displaying as a notification. This
 * verse is global to the app, and any changes that may happen with the verse
 * should be updated everywhere (including the Dashboard, widget, and notification).
 *
 * Take care to ensure that everything in the UI is being updated when the underlying
 * data changes, so that everything remains synced across the app. Do this by trying
 * to avoid making any changes to the verse outside of these classes: let these
 * classes do all the hard work, and simply tell these classes what you want
 * done.
 *
 * In addition, note that every time the Main class is loaded the verse is pulled
 * from the database, not every time the verse is accessed, for performance reasons.
 * This means that there is a possibility of data not being synced corrected if
 * the database updates if you don't create a new instance of Main at the correct
 * time. It is fine to have one instance for one method, for example, but don't
 * keep a single instance of Main as a member of a class, because it's scope is
 * too large and would make keeping it up to date difficult.
 */
public class Main {
//Data Members
//------------------------------------------------------------------------------
    private Context context;
	private Passage mainPassage;

//Constructors
//------------------------------------------------------------------------------
    public Main(Context context) {
        this.context = context;

		VerseDB db = new VerseDB(context).open();
		mainPassage = db.getVerse(MainSettings.getMainId(context));
		db.close();
    }

	public Passage getMainPassage() {
		return mainPassage;
	}

	public void setMainPassage(Passage passage) {
		MainSettings.setMainId(context, passage.getMetadata().getInt(DefaultMetaData.ID));

		VerseDB db = new VerseDB(context).open();
		mainPassage = db.getVerse(MainSettings.getMainId(context));
		db.close();
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
				default: formatter = new DefaultFormatter.Dashes(); break;
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

    public static void getNextVerse(Context context) {
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
            case 1: //comparator = new Metadata.Comparator(Metadata.Comparator.KEY_REFERENCE); break;
            case 2: comparator = new Metadata.Comparator(Metadata.Comparator.KEY_REFERENCE_ALPHABETICAL); break;
            case 3: comparator = new Metadata.Comparator(DefaultMetaData.STATE); break;
            default: comparator = new Metadata.Comparator(DefaultMetaData.ID); break;
        }
        Collections.sort(passages, comparator);

        //search through list to find the currently active list, and set the next verse to be the active verse
		final int currentId = MainSettings.getMainId(context);
		int newId = passages.get(0).getMetadata().getInt(DefaultMetaData.ID);

		for(int i = 0; i < passages.size(); i++) {
			newId = passages.get(i).getMetadata().getInt(DefaultMetaData.ID);


            if(newId == currentId) {
                if(i == passages.size() - 1) {
					newId = passages.get(0).getMetadata().getInt(DefaultMetaData.ID);
                    break;
                }
                else {
					newId = passages.get(i+1).getMetadata().getInt(DefaultMetaData.ID);
                    break;
                }
            }
        }
		MainSettings.setMainId(context, newId);
    }

	public static void getPreviousVerse(Context context) {
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
		case 1: //comparator = new Metadata.Comparator(Metadata.Comparator.KEY_REFERENCE); break;
		case 2: comparator = new Metadata.Comparator(Metadata.Comparator.KEY_REFERENCE_ALPHABETICAL); break;
		case 3: comparator = new Metadata.Comparator(DefaultMetaData.STATE); break;
		default: comparator = new Metadata.Comparator(DefaultMetaData.ID); break;
		}
		Collections.sort(passages, comparator);

		//search through list to find the currently active list, and set the next verse to be the active verse
		final int currentId = MainSettings.getMainId(context);
		int newId = passages.get(0).getMetadata().getInt(DefaultMetaData.ID);

		for(int i = 0; i < passages.size(); i++) {
			newId = passages.get(i).getMetadata().getInt(DefaultMetaData.ID);
			if(newId == currentId) {
				if(i == 0) {
					newId = passages.get(passages.size()-1).getMetadata().getInt(DefaultMetaData.ID);
					break;
				}
				else {
					newId = passages.get(i-1).getMetadata().getInt(DefaultMetaData.ID);
					break;
				}
			}
		}
		MainSettings.setMainId(context, newId);
	}

	public static void getRandomVerse(Context context) {
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
//		case 1: comparator = new Metadata.Comparator(Metadata.Comparator.KEY_REFERENCE); break;
		case 2: comparator = new Metadata.Comparator(Metadata.Comparator.KEY_REFERENCE_ALPHABETICAL); break;
		case 3: comparator = new Metadata.Comparator(DefaultMetaData.STATE); break;
		default: comparator = new Metadata.Comparator(DefaultMetaData.ID); break;
		}
		Collections.sort(passages, comparator);

		Random random = new Random(Calendar.getInstance().getTimeInMillis());
		final int currentId = MainSettings.getMainId(context);
		int newId = passages.get(0).getMetadata().getInt(DefaultMetaData.ID);

		//search through list to find the currently active list, and set a random verse to be the main verse
		while(true) {
			if(passages.size() > 1) {
				int randomIndex = random.nextInt(passages.size());
				newId = passages.get(randomIndex).getMetadata().getInt(DefaultMetaData.ID);
				if(newId != currentId) {
					break;
				}
				else {
					passages.remove(randomIndex);
					continue;
				}
			}
			else {
				break;
			}
		}

		MainSettings.setMainId(context, newId);
	}
}
