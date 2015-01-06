package com.caseybrooks.scripturememory.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.defaults.DefaultMetaData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

public class VersesDatabase {
//Database Information
//------------------------------------------------------------------------------
	public static final String KEY_ROWID = "_id";
	public static final String KEY_REFERENCE = "reference";
	public static final String KEY_VERSE = "verse";
	public static final String KEY_LIST = "list";
	
	private static final String DATABASE_NAME = "bible_verses";
	private static final String DATABASE_TABLE = "verse_list";
	private static final int DATABASE_VERSION = 2;
	
	private DbHelper helper;
	private final Context context;
	private SQLiteDatabase database;

//Database helper class
//------------------------------------------------------------------------------
	private static class DbHelper extends SQLiteOpenHelper {

		public DbHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + DATABASE_TABLE + " (" +
				KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				KEY_REFERENCE + " TEXT NOT NULL unique, " +
				KEY_VERSE + " TEXT NOT NULL unique, " +
				KEY_LIST + " TEXT NOT NULL);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if(oldVersion <= 1) {
				String[] columns = new String[] {KEY_ROWID, KEY_REFERENCE, KEY_VERSE};
				Cursor c = db.query(DATABASE_TABLE, columns, null, null, null, null, null);
				ArrayList<String> refs = new ArrayList<String>();
				ArrayList<String> verses = new ArrayList<String>();
				
				for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {	
					refs.add(c.getString(1));
					verses.add(c.getString(2));
				}
				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
				onCreate(db);
				
				for(int i = 0; i < refs.size(); i++) {
					ContentValues cv = new ContentValues();
					cv.put(KEY_REFERENCE, refs.get(i));
					cv.put(KEY_VERSE, verses.get(i));
					cv.put(KEY_LIST, "current");
					
					db.insert(DATABASE_TABLE, null, cv);
				}
			}
		}	
	}
	
//Public interface constructor and initialization
//------------------------------------------------------------------------------
	public VersesDatabase(Context context) {
		this.context = context;
	}
	
	public VersesDatabase open() throws SQLException {
		helper = new DbHelper(context);
		database = helper.getWritableDatabase();
		return this;
	}
	
	public void close() {
		helper.close();
	}

    public void migrate() {
        VerseDB verseDB = new VerseDB(context);
        verseDB.clear();
        verseDB.open();

        long id = 0;

        Cursor c = database.rawQuery("SELECT * FROM " + DATABASE_TABLE, null);

        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            try {
                Passage passage = new Passage(c.getString(c.getColumnIndex(KEY_REFERENCE)));
                passage.setText(c.getString(c.getColumnIndex(KEY_VERSE)));
                passage.setVersion(MetaSettings.getBibleVersion(context));
                passage.getMetadata().putLong(DefaultMetaData.TIME_CREATED, Calendar.getInstance().getTimeInMillis());
                if (c.getString(c.getColumnIndex(KEY_LIST)).equals("memorized")) {
                    passage.getMetadata().putInt(DefaultMetaData.STATE, 5);
                } else {
                    passage.getMetadata().putInt(DefaultMetaData.STATE, 1 + (int) (Math.random() * 4));
                }

                while (passage.getTags().length < 1 + (int) (Math.random() * 6)) {
                    int rand = 1 + (int) (Math.random() * 8);
                    switch (rand) {
                        case 1:
                            passage.addTag("Tag A");
                            break;
                        case 2:
                            passage.addTag("Tag B");
                            break;
                        case 3:
                            passage.addTag("Tag C");
                            break;
                        case 4:
                            passage.addTag("Tag D");
                            break;
                        case 5:
                            passage.addTag("Tag E");
                            break;
                        case 6:
                            passage.addTag("Tag F");
                            break;
                        case 7:
                            passage.addTag("Tag G");
                            break;
                        case 8:
                            passage.addTag("Tag H");
                            break;
                        default:
                            passage.addTag("Tag Q");
                            break;
                    }
                }

                id = verseDB.insertVerse(passage);
                Log.i("INSERT", id + "");
            }
            catch(ParseException e) {
                e.printStackTrace();
            }
        }

        MetaSettings.putVerseId(context, (int)id);


        verseDB.close();
    }

//Manipulate database entries
//------------------------------------------------------------------------------
	public long createEntry(String reference, String verse, String list) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_REFERENCE, reference);
		cv.put(KEY_VERSE, verse);
		cv.put(KEY_LIST, list);

		return database.insert(DATABASE_TABLE, null, cv);
	}
//
//	//Can set arguments to null to specify that they will not change
//	public void updateEntry(int id, String reference, String verse, String list) {
//		ContentValues cvUpdate = new ContentValues();
//		if(reference != null) cvUpdate.put(KEY_REFERENCE, reference);
//		if(verse != null) cvUpdate.put(KEY_VERSE, verse);
//		if(list != null) cvUpdate.put(KEY_LIST, list);
//
//		database.update(DATABASE_TABLE, cvUpdate, KEY_ROWID + "=" + id, null);
//	}
//
//	//TODO: Make deleted entries tagged as "deleted" rather than actually removed
//	//	Let user view "recycle bin" of deleted verses. From here, can actually hard delete
//	public void deleteEntry(int id) {
//        database.delete(DATABASE_TABLE, KEY_ROWID + "=" + id, null);
//        Toast.makeText(context, "Verse Deleted", Toast.LENGTH_SHORT).show();
//	}
//
//Get data from database
//------------------------------------------------------------------------------
//	public Verses<Passage> getVerseList(String tag) {
//		String[] columns = new String[] {KEY_ROWID, KEY_REFERENCE, KEY_VERSE, KEY_LIST};
//		Cursor c = database.query(DATABASE_TABLE, columns, null, null, null, null, null);
//
//        Verses<Passage> verses = new Verses<Passage>();
//
//		int iRow = c.getColumnIndex(KEY_ROWID);
//		int iReference = c.getColumnIndex(KEY_REFERENCE);
//		int iVerse = c.getColumnIndex(KEY_VERSE);
//		int iList = c.getColumnIndex(KEY_LIST);
//
//		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
//			if(c.getString(iList).equals(tag)) {
//                try {
//					Passage verse = new Passage(c.getString(iReference), c.getString(iVerse));
//
//					verse.addTag(c.getString(iList));
//					verse.setId(Integer.parseInt(c.getString(iRow)));
//					verses.add(verse);
//				}
//				catch(IllegalArgumentException e1) {
//                    e1.printStackTrace();
//                }
//				catch(Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}
//
//		switch(MetaSettings.getSortBy(context)) {
//			case 0:
//				verses.sortByID();
//				break;
//			case 1:
//				verses.sort();
//				break;
//			case 2:
//				verses.sortAlphabetical();
//				break;
//		}
//
//		return verses;
//	}
//
//	public Passage getEntryAt(long id) throws SQLException {
//        String[] columns = new String[]{KEY_ROWID, KEY_REFERENCE, KEY_VERSE, KEY_LIST};
//        Cursor c = database.query(DATABASE_TABLE, columns, KEY_ROWID + "=" + id, null, null, null, null);
//        int iRow = c.getColumnIndex(KEY_ROWID);
//        int iReference = c.getColumnIndex(KEY_REFERENCE);
//        int iVerse = c.getColumnIndex(KEY_VERSE);
//
//        return null;
//    }
//
//	public Passage getEntryAfter(long id, String list) throws SQLException {
//		Verses<Passage> verses = getVerseList(list);
//
//		if(verses.size() > 0) {
//			for(int i = 0; i < verses.size(); i++) {
//   				if(verses.get(i).getId() == id) {
//				    if(i == verses.size() - 1) {
//                        //found item is last, so return the first
//                        return getFirstEntry(list);
//                    }
//                    else {
//                        return verses.get(i + 1);
//                    }
//				}
//			}
//            //never found the item, return null
//            throw new SQLException("(getEntryAfter) Verse with id '" + id + "' not in list '" + list + "'");
//        }
//		else {
//            throw new SQLException("(getEntryAfter) No entries in list '" + list + "'");
//		}
//    }
//
//	public Passage getFirstEntry(String list) throws SQLException {
//		Verses<Passage> verses = getVerseList(list);
//
//        if(verses.size() > 0) return verses.get(0);
//		else throw new SQLException("(getFirstEntry) No entries in list '" + list + "'");
//	}
//
//Import and export entire database
//------------------------------------------------------------------------------
	public void exportToCSV(File filename) throws IOException {
		String text = "";

		String[] columns = new String[] {KEY_ROWID, KEY_REFERENCE, KEY_VERSE, KEY_LIST};
		Cursor c = database.query(DATABASE_TABLE, columns, null, null, null, null, null);

		int iRow = c.getColumnIndex(KEY_ROWID);
		int iReference = c.getColumnIndex(KEY_REFERENCE);
		int iVerse = c.getColumnIndex(KEY_VERSE);
		int iList = c.getColumnIndex(KEY_LIST);

		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			text = text + c.getString(iRow) + "\t" +
					c.getString(iReference) + "\t" +
					c.getString(iVerse) + "\t" +
					c.getString(iList) + "\n";
		}

		FileOutputStream fos = new FileOutputStream(filename);
		fos.write(text.getBytes());
		fos.close();
	}

	public void importFromCSV(File filename) throws SQLException, IOException {
		database.delete(DATABASE_TABLE, null, null);

//        VerseDB verseDB = new VerseDB(context);
//        verseDB.clear();
//        verseDB.open();

        int id = 0;

		BufferedReader buffer = new BufferedReader(new FileReader(filename));
		String line = "";

		while ((line = buffer.readLine()) != null) {
		    String[] str = line.split("\t");

            try {
                Passage passage = new Passage(str[1]);
                passage.setText(str[2]);
                if (str[3].equals("memorized"))
                    passage.getMetadata().putInt(DefaultMetaData.STATE, 4);
                else
                    passage.getMetadata().putInt(DefaultMetaData.STATE, 1);

//            id = verseDB.insertVerse(passage);
                id = (int) createEntry(str[1], str[2], str[3]);
            }
            catch (ParseException e) {
                e.printStackTrace();
            }
		}
		buffer.close();
//        MetaSettings.putVerseId(context, id);
	}
}









