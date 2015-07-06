package com.caseybrooks.scripturememory.nowcards.votd;

import android.content.Context;
import android.os.AsyncTask;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;
import com.caseybrooks.androidbibletools.providers.votd.VerseOfTheDay;
import com.caseybrooks.scripturememory.data.VerseDB;

import java.io.IOException;

public class VOTD {

	public static Passage getPassage(Context context) {
		if(VOTDSettings.getVOTDId(context) > 0) {
			VerseDB db = new VerseDB(context).open();
			Passage passage = db.getVerse(VOTDSettings.getVOTDId(context));
			db.close();
			return passage;
		}
		else {
			return null;
		}
	}

	public static boolean isSaved(Context context) {
		VerseDB db = new VerseDB(context).open();
		Passage currentPassage = db.getVerse(VOTDSettings.getVOTDId(context));
		db.close();
		if((currentPassage != null) && (currentPassage.getMetadata().getInt(DefaultMetaData.STATE) != VerseDB.VOTD)) {
			return true;
		}
		else {
			return false;
		}
	}

    public static void saveVerse(Context context) {
		VerseDB db = new VerseDB(context).open();
		Passage currentPassage = db.getVerse(VOTDSettings.getVOTDId(context));

		if(currentPassage != null) {
			currentPassage.getMetadata().putInt(DefaultMetaData.STATE, VerseDB.CURRENT_NONE);
			db.updateVerse(currentPassage);
			db.close();
		}
    }

//    public void setAsNotification() {
//        saveVerse();
//        VerseDB db = new VerseDB(context).open();
//        int id = db.getVerseId(currentVerse.getReference());
//        Main.putVerseId(context, id);
//        Main.setActive(context, true);
//        Main.putWorkingList(context, VerseListFragment.TAGS, db.getTag("VOTD").id);
//        db.close();
//
//        updateAll();
//    }

	public static class Redownload extends AsyncTask<Void, Void, Void> {
		Context context;

		public Redownload(Context context) {
			this.context = context;
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				VerseOfTheDay verseOfTheDay = new VerseOfTheDay();

				//try to get today's VOTD
				if(verseOfTheDay.isAvailable()) {
					verseOfTheDay.parseDocument(verseOfTheDay.getDocument());

					//if we have successfully downloaded a new verse...
					Passage newVOTD = verseOfTheDay.getPassage();
					if(newVOTD != null) {
						VerseDB db = new VerseDB(context).open();

						//Delete the old VOTD (assuming it has not been saved)
						Passage currentVOTD = db.getVerse(VOTDSettings.getVOTDId(context));
						if((currentVOTD != null) && (currentVOTD.getMetadata().getInt(DefaultMetaData.STATE) == VerseDB.VOTD)) {
							db.deleteVerse(currentVOTD);
							VOTDSettings.setVOTDId(context, -1);
						}

						//Add the new verse to the database
						newVOTD.getMetadata().putInt(DefaultMetaData.STATE, VerseDB.VOTD);

						int id = db.insertVerse(newVOTD);
						VOTDSettings.setVOTDId(context, id);
					}
				}
			}
			catch(IOException ioe) {
				ioe.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);

			VOTDBroadcasts.updateAll(context);
		}
	}
}
