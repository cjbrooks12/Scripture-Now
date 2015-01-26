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
import java.util.Collections;

public class MainVerse {
    public Passage passage;
    private Context context;

    public MainVerse(Context context) {
        this.context = context;

        VerseDB db = new VerseDB(context).open();
        passage = db.getVerse(MetaSettings.getVerseId(context));
        db.close();
    }

    public void setPassageNormal() {
		if(passage != null) {
			passage.setFormatter(new DefaultFormatter.Normal());
		}
    }

    public void setPassageFormatted() {
		if(passage != null) {
			switch (MetaSettings.getVerseDisplayMode(context)) {
				case 0: passage.setFormatter(new DefaultFormatter.Normal()); break;
				case 1: passage.setFormatter(new DefaultFormatter.Dashes()); break;
				case 2: passage.setFormatter(new DefaultFormatter.FirstLetters()); break;
				case 3: passage.setFormatter(new DefaultFormatter.DashedLetter()); break;
				case 4: passage.setFormatter(
						new DefaultFormatter.RandomWords(
								MetaSettings.getRandomnessLevel(context),
								MetaSettings.getRandomSeedOffset(context))); break;
				default: passage.setFormatter(new DefaultFormatter.Normal()); break;
			}
		}
    }

    private void getNextVerse() {
        Pair<Integer, Integer> activeList = MetaSettings.getActiveList(context);
        if(activeList.first == -1) return;

        int currentVerseId = MetaSettings.getVerseId(context);
        VerseDB db = new VerseDB(context);

        //get the active list of verses
        ArrayList<Passage> passages;
        if(activeList.first == VerseListFragment.STATE) {
            db.open();
            passages = db.getStateVerses(activeList.second);
            db.close();
        }
        else if(activeList.first == VerseListFragment.TAGS) {
            db.open();
            passages = db.getTaggedVerses(activeList.second);
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
                    currentVerseId = passages.get(i+1).getMetadata().getInt(DefaultMetaData.ID);                    MetaSettings.putVerseId(context, currentVerseId);
                    break;
                }
            }
        }

        MetaSettings.putVerseId(context, currentVerseId);
        db.open();
        passage = db.getVerse(currentVerseId);
        db.close();
    }

    public void updateAll() {
        //update all the dashboard cards
        context.sendBroadcast(new Intent(DashboardFragment.REFRESH));

        //update all the widgets
        context.sendBroadcast(new Intent(context, MainWidget.class));

        //update the notification
        if(MetaSettings.getNotificationActive(context)) {
			MainNotification.getInstance(context).create().show();
        }
    }

    public static class NextVerseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MainVerse mv = new MainVerse(context);
            mv.getNextVerse();
            mv.updateAll();
        }
    }

    public static class DismissVerseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MetaSettings.putNotificationActive(context, false);
			MainNotification.getInstance(context).dismiss();
        }
    }

	public static class TextFullReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			MetaSettings.putTextIsFull(context, !MetaSettings.getTextIsFull(context));
			MainNotification.getInstance(context).create().show();
		}
	}

	public static class MainBootReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(MetaSettings.getNotificationActive(context)) {
				MainNotification.getInstance(context).create().show();
			}
		}
	}
}
