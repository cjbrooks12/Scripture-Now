package com.caseybrooks.scripturememory.activities;

import android.app.Application;

//We will load the current Bible version at the start of the app because it
//may take a while to parse the file, and we want this to be easily accessible
//whenever we need it. We will also maintain the list of available languages
//and available versions of the selected language so that navigating to
//settings will be quick. We will also hold a reference to the main notification
//verse, the verse that we are currently interacting with, such as
//editing or actively memorizing, and the verse of the day. Everything will be
// loaded asynchronously, so any consumers of the class should be aware of this
//and take measures necessary to safely access these variables
public class SNApplication extends Application {
//	public static final String settings_file = "my_settings";

//	public volatile ABSBible selectedBible;
//	public volatile HashMap<String, String> availableLanguages;
//	public volatile HashMap<String, Bible> availableBibles;

//	private volatile Passage currentPassage;
//	private volatile Passage activePassage;

	@Override
	public void onCreate() {
		super.onCreate();

//		preloadAllData();
	}

//Important Verses
//------------------------------------------------------------------------------



//	public Passage getActivePassage() {
//		Passage passage;
//		VerseDB db = new VerseDB(this).open();
//		passage = (MainSettings.getActiveId(this) > 0) ? db.getVerse(MainSettings.getActiveId(this)) : null;
//		db.close();
//		return passage;
//	}
//
//	public void setActivePassage(Passage passage) {
//
//		//ensure that this verse is up-to-date with the database
//		VerseDB db = new VerseDB(this).open();
//		int id = db.getVerseId(passage.getReference());
//		if(id > 0) {
//			db.updateVerse(passage);
//		}
//		else {
//			id = db.insertVerse(passage);
//		}
//		db.close();
//
//		if(id > 0) {
//			//ensure the current verse has the true id of its row in the database
//			//save that id to the shared preferences.
//			MainSettings.setActiveId(this, id);
//		}
//		else {
//			//on any discrepancy, set the passage to null and remove the id that is
//			//currently saved, so we know that we do not have a verse set
//			MainSettings.setActiveId(this, -1);
//		}
//	}
//
////Preloading data that takes a while to parse, like the current Bible and the
////list of available bibles and languages. Everything is done asynchronously, so
////we need to not just check to see if the data is what we need, but register a
////callback to notify us when it has finished loading.
////------------------------------------------------------------------------------
//	private void preloadAllData() {
//		preloadSelectedBible();
////		preloadAvailableLanguages();
//	}
//
//	public static final String BIBLE_VERSION_ID = "PREF_SELECTED_VERSION_ID";
//	public static final String BIBLE_VERSION_NAME = "PREF_SELECTED_VERSION_NAME";
//	public static final String BIBLE_VERSION_ABBR = "PREF_SELECTED_VERSION_ABBR";
//	private void preloadSelectedBible() {
//		//set selected Bible to default so that a bible is available for use
//		//before complete initialization.
//		selectedBible = new ABSBible(getResources().getString(R.string.bibles_org), null);
//		final Context context = this;
//
//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//		String id = prefs.getString(BIBLE_VERSION_ID, "eng-ESV");
//		String name = prefs.getString(BIBLE_VERSION_NAME, "English Standard Version");
//		String abbr = prefs.getString(BIBLE_VERSION_ABBR, "ESV");
//
//		//load basic Bible data first
//		final ABSBible bible = new ABSBible(getResources().getString(R.string.bibles_org), id);
//		bible.setName(name);
//		bible.setAbbr(abbr);
//
//		//load more complex Bible data asynchronously: books, and their chapters, etc.
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					Document doc = Util.getChachedDocument(context, "selectedVersion.xml");
//
//					if(doc != null) {
//						bible.parseDocument(doc);
//						selectedBible = bible;
//					}
//					else {
//						if(Util.isConnected(context) && bible.isAvailable()) {
//							doc = bible.getDocument();
//							Util.cacheDocument(context, doc, "selectedVersion.xml");
//							bible.parseDocument(doc);
//							selectedBible = bible;
//						}
//					}
//				}
//				catch(IOException ioe) {
//					ioe.printStackTrace();
//				}
//
//				//notify here...
//				VOTDBroadcasts.updateAll(context);
//			}
//		}).start();
//	}
//
//	private void preloadAvailableLanguages() {
//		final Context context = this;
//
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					String languagesFile = "languages.xml";
//
//					Document languageDoc = Util.getChachedDocument(context, languagesFile);
//					if(languageDoc != null) {
//						availableLanguages = ABSBible.getAvailableLanguages(languageDoc);
//					}
//					else if(Util.isConnected(context)) {
//						languageDoc = ABSBible.availableVersionsDoc(
//								context.getResources().getString(R.string.bibles_org),
//								null
//						);
//
//						Util.cacheDocument(context, languageDoc, languagesFile);
//						availableLanguages = ABSBible.getAvailableLanguages(languageDoc);
//					}
//					else {
//						HashMap<String, String> defaultEnglish = new HashMap<>();
//						defaultEnglish.put("English (US)", "eng-us");
//						availableLanguages = defaultEnglish;
//					}
//				}
//				catch(IOException ioe) {
//					ioe.printStackTrace();
//				}
//
//				//notify here...
//			}
//		}).start();
//	}
//
//	private void preloadAvailableBibles(final String langCode) {
//		final Context context = this;
//
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					String versionsFile = "versions";
//					if(langCode != null) versionsFile += ":" + langCode;
//					versionsFile += ".xml";
//
//					Document doc = Util.getChachedDocument(context, versionsFile);
//					if(doc != null) {
//						availableBibles = ABSBible.parseAvailableVersions(doc);
//					}
//					else if(Util.isConnected(context)) {
//						doc = ABSBible.availableVersionsDoc(
//								context.getResources().getString(R.string.bibles_org),
//								langCode
//						);
//						Util.cacheDocument(context, doc, versionsFile);
//						availableBibles = ABSBible.parseAvailableVersions(doc);
//					}
//					else {
//						HashMap<String, Bible> defaultESV = new HashMap<>();
//						defaultESV.put("ESV", new Bible());
//						availableBibles = defaultESV;
//					}
//				}
//				catch(IOException ioe) {
//					ioe.printStackTrace();
//					HashMap<String, Bible> defaultESV = new HashMap<>();
//					defaultESV.put("ESV", new Bible());
//					availableBibles = defaultESV;
//				}
//			}
//		});
//	}




//Simple getters and setters
//------------------------------------------------------------------------------
//	public ABSBible getSelectedBible() {
//		return selectedBible;
//	}
//
//	public void setSelectedBible(ABSBible selectedBible) {
//		this.selectedBible = selectedBible;
//	}
//
//	public HashMap<String, String> getAvailableLanguages() {
//		return availableLanguages;
//	}
//
//	public void setAvailableLanguages(HashMap<String, String> availableLanguages) {
//		this.availableLanguages = availableLanguages;
//	}
//
//	public HashMap<String, Bible> getAvailableBibles() {
//		return availableBibles;
//	}
//
//	public void setAvailableBibles(HashMap<String, Bible> availableBibles) {
//		this.availableBibles = availableBibles;
//	}
}
