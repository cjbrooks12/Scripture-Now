package com.caseybrooks.scripturememory.nowcards;

import android.content.Context;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.scripturememory.data.VerseDB;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

/** Utility class to determine the next item to be issued as part of the Scripture
 * Now system. This class picks a random verse for me either by weight or by tag,
 * tells me the next card/notification to be issued, and tells me when to issue
 * the notification.
 */
public class ScriptureNow {
	public static float allVersesShare = 0.05f;
	public static float memorizedShare = 0.3f;
	public static float currentShare = 0.35f;
	public static float currentAllShare = 0.45f;
	public static float currentMostShare = 0.6f;
	public static float currentSomeShare = 0.75f;
	public static float currentNoneShare = 1.0f;

	/** Get a random verse based on the memorization state of the verses. States
	 * are weighted to give verses more frequently in the Memorized (to make sure
	 * the user knows the verse) and Current - None (to get them memorizing all
	 * the verses they have added) than in the other states. Rarely will it give
	 * a verse from in any state or from all current verses, but it is possible.
	 */
	public static Passage getNextStateVerse(Context context) {
		Random randomizer = new Random(Calendar.getInstance().getTimeInMillis());

		//get a list of verses from the ic_database
		VerseDB db = new VerseDB(context);
		int count = 10; //give it 5 attemps to get a list that isn't empty
		ArrayList<Passage> selectedPassages;

		while(true) {
			//determine which list to select a verse from
			float weightedList = randomizer.nextFloat();
			int list;

			if(weightedList >= 0 			         && weightedList < allVersesShare)    list = VerseDB.ALL_VERSES;
			else if(weightedList >= allVersesShare   && weightedList < memorizedShare)    list = VerseDB.MEMORIZED;
			else if(weightedList >= memorizedShare   && weightedList < currentShare)      list = VerseDB.CURRENT;
			else if(weightedList >= currentShare     && weightedList < currentAllShare)   list = VerseDB.CURRENT_ALL;
			else if(weightedList >= currentAllShare  && weightedList < currentMostShare)  list = VerseDB.CURRENT_MOST;
			else if(weightedList >= currentMostShare && weightedList < currentSomeShare)  list = VerseDB.CURRENT_SOME;
			else if(weightedList >= currentSomeShare && weightedList <= currentNoneShare) list = VerseDB.CURRENT_NONE;
			else list = VerseDB.ALL_VERSES;

			//if we have exhausted all attempts, just try to get all the verses from the ic_database
			if(count == 0) {
				db.open();
				selectedPassages = db.getAllVerses();
				db.close();
				break;
			}
			//otherwise, get the verses from the randomly selected list
			else {
				db.open();
				if(list == VerseDB.ALL_VERSES) selectedPassages = db.getAllVerses();
				else if(list == VerseDB.CURRENT) selectedPassages = db.getAllCurrentVerses();
				else {
					selectedPassages = db.getStateVerses(list);
				}
				db.close();

				if(selectedPassages.size() == 0) {
					count--;
					continue;
				}
				else {
					break;
				}
			}
		}

		if(selectedPassages.size() == 0) return null;
		else return selectedPassages.get(randomizer.nextInt(selectedPassages.size()));
	}

	/** Gets a random verse from a random tag in the ic_database. Because there is
	 * no way to know which tags exist, all tags are weighted evenly, and selection
	 * is fully random.
	 * */
//	public static Passage getNextTagVerse(Context context) {
//		Random randomizer = new Random(Calendar.getInstance().getTimeInMillis());
//
//		//get a list of verses from the ic_database
//		VerseDB db = new VerseDB(context);
//
//		db.open();
//		String[] tags = db.getAllTagNames();
//		int tagList = randomizer.nextInt(tags.length);
//
//		ArrayList<Passage> selectedPassages = db.getTaggedVerses((int)db.getTagID(tags[tagList]));
//		if(selectedPassages == null || selectedPassages.size() == 0) return null;
//		else {
//			int randomVerse = randomizer.nextInt(selectedPassages.size());
//			return selectedPassages.get(randomVerse);
//		}
//	}

	public static Class getNextCard() {
		return null;
	}

	public static long getNextNotificationTime() {
		return 0;
	}
}
