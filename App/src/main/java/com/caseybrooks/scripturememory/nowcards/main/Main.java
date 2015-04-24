package com.caseybrooks.scripturememory.nowcards.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.data.Metadata;
import com.caseybrooks.androidbibletools.defaults.DefaultFormatter;
import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
import com.caseybrooks.scripturememory.data.MetaSettings;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.fragments.DashboardFragment;
import com.caseybrooks.scripturememory.fragments.VerseListFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Random;

public class Main {
//SharedPreferences relevant to the main notification
//------------------------------------------------------------------------------
	private static final String settings_file = "my_settings";
	private static final String PREFIX = "MAIN_";

	//the id of the active verse
	public static final String ID = "ID";
	public static int getVerseId(Context context) {
		return context.getSharedPreferences(settings_file, 0).getInt(PREFIX + ID, -1);
	}

	public static void putVerseId(Context context, int value) {
		context.getSharedPreferences(settings_file, 0).edit().putInt(PREFIX + ID, value).commit();
	}

	//the list that the user is working within
	public static final String WORKING_LIST_GROUP = "WORKING_LIST_GROUP";
	public static final String WORKING_LIST_CHILD = "WORKING_LIST_CHILD";
	public static void putWorkingList(Context context, int group, int child) {
		context.getSharedPreferences(settings_file, 0).edit().putInt(PREFIX + WORKING_LIST_GROUP, group).commit();
		context.getSharedPreferences(settings_file, 0).edit().putInt(PREFIX + WORKING_LIST_CHILD, child).commit();
	}
	public static Pair<Integer, Integer> getWorkingList(Context context) {
		return new Pair<Integer, Integer>(
				context.getSharedPreferences(settings_file, 0).getInt(PREFIX + WORKING_LIST_GROUP, -1),
				context.getSharedPreferences(settings_file, 0).getInt(PREFIX + WORKING_LIST_CHILD, -1));
	}

	//whether the user has the main notification actively displayed
	private static final String ACTIVE = "ACTIVE";
	public static boolean isActive(Context context) {
		return context.getSharedPreferences(settings_file, 0).getBoolean(PREFIX + ACTIVE, false);
	}
	public static void setActive(Context context, boolean value) {
        context.getSharedPreferences(settings_file, 0).edit().putBoolean(PREFIX + ACTIVE, value).commit();
	}

	//how to view the main notification
	public static final String DISPLAY_MODE = "DISPLAY_MODE";
	public static int getDisplayMode(Context context) {
		return context.getSharedPreferences(settings_file, 0).getInt(PREFIX + DISPLAY_MODE, 0);
	}
	public static void putDisplayMode(Context context, int value) {
		context.getSharedPreferences(settings_file, 0).edit().putInt(PREFIX + DISPLAY_MODE, value).commit();
	}

	//whether to show the full or formatted text in the notification
	private static final String FULL_TEXT = "FULL_TEXT";
	public static boolean isTextFull(Context context) {
		return context.getSharedPreferences(settings_file, 0).getBoolean(PREFIX + FULL_TEXT, false);
	}
	public static void setTextFull(Context context, boolean value) {
		context.getSharedPreferences(settings_file, 0).edit().putBoolean(PREFIX + FULL_TEXT, value).commit();
	}

	//if the display mode is "Random Words", how random and at what offest
	private static final String RANDOMNESS_LEVEL = "RANDOMNESS_LEVEL";
	private static final String RANDOMNESS_OFFSET = "RANDOMNESS_OFFSET";
	public static Pair<Float, Integer> getRandomness(Context context) {
		return new Pair<>(
				context.getSharedPreferences(settings_file, 0).getFloat(PREFIX + RANDOMNESS_LEVEL, 0.5f),
				context.getSharedPreferences(settings_file, 0).getInt(PREFIX + RANDOMNESS_OFFSET, 0));
	}
	public static void putRandomness(Context context, float level, int offset) {
		if(level >= 0) context.getSharedPreferences(settings_file, 0).edit().putFloat(PREFIX + RANDOMNESS_LEVEL, level).commit();
		if(offset >= 0) context.getSharedPreferences(settings_file, 0).edit().putInt(PREFIX + RANDOMNESS_OFFSET, offset).commit();
	}


//Data Members
//------------------------------------------------------------------------------
    public Passage passage;
    private Context context;

    public Main(Context context) {
        this.context = context;

        VerseDB db = new VerseDB(context).open();
        passage = db.getVerse(getVerseId(context));
        db.close();
    }

    public void setPassageNormal() {
		if(passage != null) {
			passage.setFormatter(new DefaultFormatter.Normal());
		}
    }

    public void setPassageFormatted() {
		if(passage != null) {
			switch (getDisplayMode(context)) {
				case 0: passage.setFormatter(new DefaultFormatter.Dashes()); break;
				case 1: passage.setFormatter(new DefaultFormatter.FirstLetters()); break;
				case 2: passage.setFormatter(new DefaultFormatter.DashedLetter()); break;
				case 3: passage.setFormatter(
						new DefaultFormatter.RandomWords(
								getRandomness(context).first,
								getRandomness(context).second)); break;
				default: passage.setFormatter(new DefaultFormatter.Normal()); break;
			}
		}
    }

    private void getNextVerse() {
        Pair<Integer, Integer> workingList = getWorkingList(context);
        if(workingList.first == -1) return;

        int currentVerseId = getVerseId(context);
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
            if(passages.get(i).getMetadata().getInt(DefaultMetaData.ID) == currentVerseId) {
                if(i == passages.size() - 1) {
                    currentVerseId = passages.get(0).getMetadata().getInt(DefaultMetaData.ID);
                    break;
                }
                else {
                    currentVerseId = passages.get(i+1).getMetadata().getInt(DefaultMetaData.ID);
                    break;
                }
            }
        }

        putVerseId(context, currentVerseId);
        db.open();
        passage = db.getVerse(currentVerseId);
        db.close();
    }

	private void getPreviousVerse() {
		Pair<Integer, Integer> workingList = getWorkingList(context);
		if(workingList.first == -1) return;

		int currentVerseId = getVerseId(context);
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
			if(passages.get(i).getMetadata().getInt(DefaultMetaData.ID) == currentVerseId) {
				if(i == 0) {
					currentVerseId = passages.get(passages.size()-1).getMetadata().getInt(DefaultMetaData.ID);
					break;
				}
				else {
					currentVerseId = passages.get(i-1).getMetadata().getInt(DefaultMetaData.ID);
					break;
				}
			}
		}

		putVerseId(context, currentVerseId);
		db.open();
		passage = db.getVerse(currentVerseId);
		db.close();
	}

	private void getRandomVerse() {
		Pair<Integer, Integer> workingList = getWorkingList(context);
		if(workingList.first == -1) return;

		int currentVerseId = getVerseId(context);
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
			int randomVerseId = 0;
			Random random = new Random(Calendar.getInstance().getTimeInMillis());
			while(true) {
				randomVerseId = passages.get(random.nextInt(passages.size())).getMetadata().getInt(DefaultMetaData.ID);
				if(randomVerseId != currentVerseId) {
					break;
				}
			}
			putVerseId(context, randomVerseId);
			db.open();
			passage = db.getVerse(randomVerseId);
			db.close();
		}
	}

    public void updateAll() {
        //update all the dashboard cards
        context.sendBroadcast(new Intent(DashboardFragment.REFRESH));

        //update all the widgets
        context.sendBroadcast(new Intent(context, MainWidget.class));

        //update the notification
        if(isActive(context)) {
			MainNotification.getInstance(context).create().show();
        }
    }

	public static class PreviousVerseReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Main mv = new Main(context);
			mv.getPreviousVerse();
			mv.updateAll();
		}
	}

    public static class NextVerseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Main mv = new Main(context);
            mv.getNextVerse();
            mv.updateAll();
        }
    }

	public static class RandomVerseReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Main mv = new Main(context);
			mv.getRandomVerse();
			mv.updateAll();
		}
	}

    public static class DismissVerseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            setActive(context, false);
			MainNotification.getInstance(context).dismiss();
        }
    }

	public static class TextFullReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Main.setTextFull(context, !Main.isTextFull(context));
			MainNotification.getInstance(context).create().show();
		}
	}

	public static class MainBootReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(isActive(context)) {
				MainNotification.getInstance(context).create().show();
			}
		}
	}
}
