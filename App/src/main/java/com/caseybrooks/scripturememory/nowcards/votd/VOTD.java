package com.caseybrooks.scripturememory.nowcards.votd;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.caseybrooks.androidbibletools.basic.AbstractVerse;
import com.caseybrooks.androidbibletools.basic.Bible;
import com.caseybrooks.androidbibletools.basic.Book;
import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.basic.Reference;
import com.caseybrooks.androidbibletools.basic.Tag;
import com.caseybrooks.androidbibletools.io.ABTUtility;
import com.caseybrooks.androidbibletools.providers.abs.ABSPassage;
import com.caseybrooks.androidbibletools.providers.votd.VerseOfTheDay;
import com.caseybrooks.androidbibletools.widget.IReferencePickerListener;
import com.caseybrooks.androidbibletools.widget.IVerseViewListener;
import com.caseybrooks.androidbibletools.widget.LoadState;
import com.caseybrooks.androidbibletools.widget.ReferenceWorker;
import com.caseybrooks.androidbibletools.widget.VerseWorker;
import com.caseybrooks.androidbibletools.widget.WorkerThread;
import com.caseybrooks.scripturememory.R;
import com.caseybrooks.scripturememory.data.VerseDB;
import com.caseybrooks.scripturememory.nowcards.main.MainNotification;
import com.caseybrooks.scripturememory.nowcards.main.MainSettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class VOTD implements IVerseViewListener {
	WorkerThread workerThread;
	VerseWorker verseWorker;
	Context context;

	Bible selectedBible;
	AbstractVerse verse;

	IVerseViewListener listener;

	public VOTD(Context context) {
		this.context = context;

		workerThread = new WorkerThread();
		if(workerThread.getState() == Thread.State.NEW)
			workerThread.start();

		verseWorker = new VerseWorker(context);
		verseWorker.setListener(this);
	}

	public void loadTodaysVerse() {
		verseWorker.loadSelectedBible();
	}

	@Override
	public boolean onBibleLoaded(Bible bible, LoadState loadState) {
		selectedBible = bible;

		workerThread.post(checkCacheRunnable);

		return false;
	}

	@Override
	public boolean onVerseLoaded(AbstractVerse abstractVerse, LoadState loadState) {
		verse = abstractVerse;
		return (listener != null) ? listener.onVerseLoaded(abstractVerse, loadState) : false;
	}

	public IVerseViewListener getListener() {
		return listener;
	}

	public void setListener(IVerseViewListener listener) {
		this.listener = listener;
	}

	public AbstractVerse getVerse() {
		return verse;
	}

	//Background tasks necessary to load the Verse of the Day
//------------------------------------------------------------------------------
	Runnable checkCacheRunnable = new Runnable() {
		@Override
		public void run() {
			String key = VOTDSettings.getKey(context);

			//try to load the VOTD from the cache,
			if(key != null) {
				//see if the verse is from today. If it is not, force a deletion
				//of the file in the cache
				long lastUpdatedMillis = PreferenceManager.getDefaultSharedPreferences(context).getLong(key, 0L);
				Calendar lastUpdated = Calendar.getInstance();
				lastUpdated.setTimeInMillis(lastUpdatedMillis);

				Calendar now = Calendar.getInstance();

				if(now.get(Calendar.DATE) != lastUpdated.get(Calendar.DATE) ||
						now.get(Calendar.MONTH) != lastUpdated.get(Calendar.MONTH) ||
						now.get(Calendar.YEAR) != lastUpdated.get(Calendar.YEAR)) {

					PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(key, 1L);
					ABTUtility.getChachedDocument(context, key, ABTUtility.CacheTimeout.OneDay.millis);

					workerThread.post(getReferenceRunnable);
				}
				else {
					Reference ref = new Reference.Builder()
							.setBible(selectedBible)
							.parseReference(VOTDSettings.getReference(context))
							.create();

					verseWorker.setVerse(new ABSPassage(
							context.getResources().getString(R.string.bibles_org_key),
							ref
					));
					verseWorker.tryCacheOrDownloadText();
				}

				return;
			}
			else {
				workerThread.post(getReferenceRunnable);
			}
		}
	};

	Runnable getReferenceRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				VerseOfTheDay votd = new VerseOfTheDay();
				votd.parseDocument(votd.getDocument());

				ReferenceWorker referenceWorker = new ReferenceWorker(context);
				referenceWorker.setListener(new IReferencePickerListener() {
					@Override
					public boolean onBibleLoaded(Bible bible, LoadState loadState) {
						return false;
					}

					@Override
					public boolean onReferenceParsed(Reference reference, boolean b) {
						if(b) {
							verseWorker.setVerse(new ABSPassage(
									context.getResources().getString(R.string.bibles_org_key),
									reference
							));
							VOTDSettings.setReference(context, reference.toString());
							VOTDSettings.setKey(context, verseWorker.getVerse().getId());
							verseWorker.tryCacheOrDownloadText();
						}
						else {
							workerThread.post(getRandomVerseRunnable);
						}

						return false;
					}
				});
				referenceWorker.loadSelectedBible();
				referenceWorker.checkReference(votd.getPassage().getReference().toString());
			}
			catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}
	};

	Runnable getRandomVerseRunnable = new Runnable() {
		@Override
		public void run() {
			ArrayList<Book> books = selectedBible.getBooks();
			Calendar cal = Calendar.getInstance();

			int todaysRandomSeed = cal.get(Calendar.DATE) *
					cal.get(Calendar.MONTH) *
					cal.get(Calendar.YEAR);

			Random random = new Random(todaysRandomSeed);

			Book randomBook = selectedBible.getBooks().get(random.nextInt(selectedBible.getBooks().size()));
			int randomChapter = random.nextInt(randomBook.numChapters() + 1);
			int randomVerse = random.nextInt(randomBook.numVersesInChapter(randomChapter) + 1);

			Reference reference = new Reference.Builder()
					.setBible(selectedBible)
					.setBook(randomBook)
					.setChapter(randomChapter)
					.setVerses(randomVerse)
					.create();

			verseWorker.setVerse(new ABSPassage(
					context.getResources().getString(R.string.bibles_org_key),
					reference
			));

			VOTDSettings.setReference(context, reference.toString());
			VOTDSettings.setKey(context, verseWorker.getVerse().getId());

			verseWorker.tryCacheOrDownloadText();
		}
	};

//Things to do after the verse has been loaded
//------------------------------------------------------------------------------
	public boolean isVerseSaved() {
		if(verse != null) {
			VerseDB db = new VerseDB(context).open();
			int id = db.getVerseId(verse.getReference());
			db.close();

			return (id != -1);
		}
		else {
			return false;
		}
	}

	public int saveVerse() {
		if(verse != null) {
			verse.addTag(new Tag("VOTD"));
			Log.i("VOTD.save", "verse.reference='" + verse.getReference().toString() + "' + verse.bible='" + verse.getBible().getAbbreviation() + "'");
			VerseDB db = new VerseDB(context).open();
			int id = db.getVerseId(verse.getReference());
			if(id != -1) {
				db.updateVerse((Passage) verse);
				db.close();
				return id;
			}
			else {
				id = db.insertVerse((Passage) verse);
				db.close();
				return id;
			}
		}
		else {
			return -1;
		}
	}

	public void setAsNotification() {
		int id = saveVerse();
		if(id != -1) {
			MainSettings.setMainId(context, id);
			MainSettings.setActive(context, true);
			MainNotification.getInstance(context).create().show();
			VOTDBroadcasts.updateAll(context);
		}
	}
}
