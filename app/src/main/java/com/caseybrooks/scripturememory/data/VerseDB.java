package com.caseybrooks.scripturememory.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.util.Log;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.container.Verses;
import com.caseybrooks.androidbibletools.enumeration.Version;

import java.util.Calendar;

/**
 * Redesigned verses database, designed to replace VersesDatabase class
 * Supports multiple tables with tagging, ranking state of verse memorization,
 * colors, verse version, when the verse was added/modified
 */
public class VerseDB {
//Database Bookkeeping Information
//------------------------------------------------------------------------------
    //global database information
    private static final String DATABASE_NAME = "verses_db";
    private static final int DATABASE_VERSION = 1;
    private DbHelper helper;
    private final Context context;
    private SQLiteDatabase db;

    //fields for main table
    private static final String TABLE_VERSES = "verses";
    public static final String KEY_VERSES_ID = "_id";
    public static final String KEY_VERSES_REFERENCE = "reference";
    public static final String KEY_VERSES_VERSE = "verse";
    public static final String KEY_VERSES_VERSION = "version";
    public static final String KEY_VERSES_DATE_ADDED = "date_added";
    public static final String KEY_VERSES_DATE_MODIFIED = "date_modified";
    public static final String KEY_VERSES_STATE = "state";
    public static final String KEY_VERSES_TAGS = "tags";

    private static final String CREATE_TABLE_VERSES =
            "CREATE TABLE " + TABLE_VERSES + "(" +
                    KEY_VERSES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_VERSES_REFERENCE + " TEXT NOT NULL unique, " +
                    KEY_VERSES_VERSE + " TEXT NOT NULL unique, " +
                    KEY_VERSES_VERSION + " TEXT NOT NULL, " +
                    KEY_VERSES_DATE_ADDED + " INTEGER NOT NULL, " +
                    KEY_VERSES_DATE_MODIFIED + " INTEGER NOT NULL, " +
                    KEY_VERSES_STATE + " INTEGER, " +
                    KEY_VERSES_TAGS + " TEXT);";

    //fields for tags table
    private static final String TABLE_TAGS = "tags";
    public static final String KEY_TAGS_ID = "_id";
    public static final String KEY_TAGS_TAG = "tag";
    public static final String KEY_TAGS_VERSEIDS = "verse_ids";
    public static final String KEY_TAGS_COLOR = "color";

    private static final String CREATE_TABLE_TAGS =
            "CREATE TABLE " + TABLE_TAGS + "(" +
                    KEY_TAGS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_TAGS_TAG + " TEXT NOT NULL unique, " +
                    KEY_TAGS_VERSEIDS + " TEXT, " +
                    KEY_TAGS_COLOR + " TEXT NOT NULL);";

    //fields for state table
    private static final String TABLE_STATE = "state";
    public static final String KEY_STATE_ID = "_id";
    public static final String KEY_STATE_STATE = "state";
    public static final String KEY_STATE_VERSEIDS = "verse_ids";
    public static final String KEY_STATE_COLOR = "color";

    private static final String CREATE_TABLE_STATE =
            "CREATE TABLE " + TABLE_STATE + "(" +
                    KEY_STATE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_STATE_STATE + " TEXT NOT NULL unique, " +
                    KEY_STATE_VERSEIDS + " TEXT, " +
                    KEY_STATE_COLOR + " TEXT NOT NULL);";

//Database helper class
//--------------------------------------------------------------------------
    private static class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // creating required tables
            db.execSQL(CREATE_TABLE_VERSES);
            db.execSQL(CREATE_TABLE_TAGS);
            db.execSQL(CREATE_TABLE_STATE);

            ContentValues current_none = new ContentValues();
            current_none.put(KEY_STATE_STATE, "current_none");
            current_none.put(KEY_STATE_COLOR, "#FF0000");
            db.insert(TABLE_STATE, null, current_none);

            ContentValues current_some = new ContentValues();
            current_some.put(KEY_STATE_STATE, "current_some");
            current_some.put(KEY_STATE_COLOR, "#FF8800");
            db.insert(TABLE_STATE, null, current_some);

            ContentValues current_most = new ContentValues();
            current_most.put(KEY_STATE_STATE, "current_most");
            current_most.put(KEY_STATE_COLOR, "#FFFF00");
            db.insert(TABLE_STATE, null, current_most);

            ContentValues current_all = new ContentValues();
            current_all.put(KEY_STATE_STATE, "current_all");
            current_all.put(KEY_STATE_COLOR, "#88FF00");
            db.insert(TABLE_STATE, null, current_all);

            ContentValues memorized = new ContentValues();
            memorized.put(KEY_STATE_STATE, "memorized");
            memorized.put(KEY_STATE_COLOR, "#00FF00");
            db.insert(TABLE_STATE, null, memorized);

            ContentValues archived = new ContentValues();
            archived.put(KEY_STATE_STATE, "archived");
            archived.put(KEY_STATE_COLOR, "#8888ff");
            db.insert(TABLE_STATE, null, archived);

            ContentValues deleted = new ContentValues();
            deleted.put(KEY_STATE_STATE, "deleted");
            deleted.put(KEY_STATE_COLOR, "#666688");
            db.insert(TABLE_STATE, null, deleted);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // on upgrade drop older tables (worry about saving data if/when I upgrade this one)
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_VERSES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAGS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATE);

            // create new tables
            onCreate(db);
        }
    }

//Main Database Methods
//------------------------------------------------------------------------------
    public VerseDB(Context context) {
        this.context = context;
    }

    public VerseDB open() throws SQLException {
        helper = new DbHelper(context);
        db = helper.getWritableDatabase();

        return this;
    }

    public VerseDB close() {
        helper.close();
        return this;
    }

    //dumps the entire database
    public void clear() {
        helper = new DbHelper(context);
        db = helper.getWritableDatabase();
        db.delete(TABLE_VERSES, null, null);
        db.delete(TABLE_TAGS, null, null);
        ContentValues state_values = new ContentValues();
        state_values.put(KEY_STATE_VERSEIDS, "");
        db.update(
                TABLE_STATE,
                state_values,
                null,
                null);
        helper.close();
    }

//public getter functions
//------------------------------------------------------------------------------
    public Passage getVerse(long verse_id) {
        String selectQuery =
                "SELECT *" +
                " FROM " + TABLE_VERSES +
                " WHERE " + KEY_VERSES_ID + " = " + verse_id;

        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.getCount() > 0) c.moveToFirst();
        else return null;

        Passage passage = new Passage(c.getString(c.getColumnIndex(KEY_VERSES_REFERENCE)));
        passage.setId(c.getInt(c.getColumnIndex(KEY_VERSES_ID)));
        passage.setText(c.getString(c.getColumnIndex(KEY_VERSES_VERSE)));
        passage.setVersion(Version.fromString(c.getString(c.getColumnIndex(KEY_VERSES_VERSION))));
        passage.setMillis(c.getLong(c.getColumnIndex(KEY_VERSES_DATE_ADDED)));
        passage.setState(c.getInt(c.getColumnIndex(KEY_VERSES_STATE)));

        String[] tags = c.getString(c.getColumnIndex(KEY_VERSES_TAGS)).split(",");
        for(int i = 0; i < tags.length; i++) {
            tags[i] = getTagName(Integer.parseInt(tags[i]));
        }
        passage.setTags(tags);

        return passage;
    }

    public int insertVerse(Passage passage) {
        //set values for each field in this verse to be either created or updated
        ContentValues values = new ContentValues();
        values.put(KEY_VERSES_REFERENCE, passage.getReference());
        values.put(KEY_VERSES_VERSE, passage.getText());
        values.put(KEY_VERSES_VERSION, passage.getVersion().getCode());
        values.put(KEY_VERSES_DATE_ADDED, Calendar.getInstance().getTimeInMillis());
        //values.put(KEY_VERSES_DATE_MODIFIED, Calendar.getInstance().getTimeInMillis());
        values.put(KEY_VERSES_STATE, passage.getState());

        //ensure tags on this verse are up-to-date
        String[] tags = passage.getTags();
        String tag_string = "";
        for(String tag : tags) {
            long tagID = getTagID(tag);

            //tag does not yet exist, create it
            if(tagID == -1) {
                tagID = addTag(tag, null);
            }

            tag_string += tagID + ",";
        }
        values.put(KEY_VERSES_TAGS, tag_string);

        int newVerseID = (int) db.insert(TABLE_VERSES, null, values);

        //add new verse's id to each of its tags
        for(String tag : tags) {
            addVerseToTag(getTagID(tag), newVerseID);
        }

        //add new verse's id to state
        addVerseToState(passage.getState(), newVerseID);

        return newVerseID;
    }

    public void updateVerse(Passage passage) {
        //set values for each field in this verse to be either created or updated
        ContentValues values = new ContentValues();
        values.put(KEY_VERSES_REFERENCE, passage.getReference());
        values.put(KEY_VERSES_VERSE, passage.getText());
        values.put(KEY_VERSES_VERSION, passage.getVersion().getCode());
        //values.put(KEY_VERSES_DATE_MODIFIED, passage.getMillis());
        values.put(KEY_VERSES_STATE, passage.getState());

        //ensure tags on this verse are up-to-date
        String[] tags = passage.getTags();
        String tag_string = "";
        for(String tag : tags) {
            long tagID = getTagID(tag);

            //tag does not yet exist, create it
            if(tagID == -1) {
                tagID = addTag(tag, null);
            }

            tag_string += tagID + ",";
        }
        values.put(KEY_VERSES_TAGS, tag_string);

        db.update(TABLE_VERSES, values, KEY_VERSES_ID + "=" + passage.getId(), null);


//        //ensure verse isn't listed in any other tags
//        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_TAGS, null);
//        if(c != null && c.getCount() > 0) {
//            c.moveToFirst();
//            do {
//                long tagId = c.getLong(c.getColumnIndex(KEY_TAGS_ID));
//                removeVerseFromTag(tagId, passage.getId());
//            }
//            while(c.moveToNext());
//        }
//
//        //add new verse's id to each tag
//        for(String tag : tags) {
//            addVerseToTag(getTagID(tag), passage.getId());
//        }
//
//        //ensure verse isn't listed in any other tags
//        c = db.rawQuery("SELECT * FROM " + TABLE_STATE, null);
//        if(c != null && c.getCount() > 0) {
//            c.moveToFirst();
//            do {
//                long stateId = c.getLong(c.getColumnIndex(KEY_STATE_ID));
//                removeVerseFromState(stateId, passage.getId());
//            }
//            while(c.moveToNext());
//        }
//
//        //add new verse's id to state
//        addVerseToState(passage.getState(), passage.getId());
    }

    //Get information about tags
    public String getTagName(long tag_id) {
        String selectQuery =
                "SELECT *" +
                " FROM " + TABLE_TAGS +
                " WHERE " + KEY_TAGS_ID + " = " + tag_id;

        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.getCount() > 0) c.moveToFirst();
        else return "";

        return c.getString(c.getColumnIndex(KEY_TAGS_TAG));
    }

    public long getTagID(String tag) {
        String selectQuery =
                "SELECT *" +
                " FROM " + TABLE_TAGS +
                " WHERE " + KEY_TAGS_TAG + " LIKE \"" + tag + "\"";

        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.getCount() > 0) c.moveToFirst();
        else return -1;

        return c.getInt(c.getColumnIndex(KEY_TAGS_ID));
    }

//    public int getTagColor(long id) {
//        String selectQuery =
//                "SELECT *" +
//                " FROM " + TABLE_TAGS +
//                " WHERE " + KEY_TAGS_ID + " = " + id;
//
//        Cursor c = db.rawQuery(selectQuery, null);
//        if (c != null && c.getCount() > 0) c.moveToFirst();
//        else Color.parseColor("#508A4C");
//
//        return Color.parseColor(c.getString(c.getColumnIndex(KEY_TAGS_COLOR)));
//    }

    public int getTagColor(String tag) {
        String selectQuery =
                "SELECT " + KEY_TAGS_COLOR +
                " FROM " + TABLE_TAGS +
                " WHERE " + KEY_TAGS_TAG + " LIKE \"" + tag + "\"";

        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.getCount() > 0) c.moveToFirst();
        else Color.parseColor("#508A4C");

        return Color.parseColor(c.getString(c.getColumnIndex(KEY_TAGS_COLOR)));
    }

//    public Verses<Passage> getTaggedVerses(int id) {
//        String selectQuery =
//                "SELECT *" +
//                " FROM " + TABLE_TAGS +
//                " WHERE " + KEY_TAGS_ID + " = " + id;
//
//        Cursor c = db.rawQuery(selectQuery, null);
//        if (c != null && c.getCount() > 0) c.moveToFirst();
//        else return new Verses<Passage>();
//
//        Verses<Passage> verses = new Verses<Passage>();
//        String[] verse_ids = c.getString(c.getColumnIndex(KEY_TAGS_VERSEIDS)).split(",");
//        for(int i = 0; i < verse_ids.length; i++) {
//            verses.add(getVerse(Integer.parseInt(verse_ids[i])));
//        }
//
//        return verses;
//    }

    public Verses<Passage> getTaggedVerses(String tag) {
        String selectQuery =
                "SELECT " + KEY_TAGS_COLOR +
                " FROM " + TABLE_TAGS +
                " WHERE " + KEY_TAGS_TAG + " LIKE \"" + tag + "\"";

        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.getCount() > 0) c.moveToFirst();
        else return new Verses<Passage>();

        Verses<Passage> verses = new Verses<Passage>();
        String[] verse_ids = c.getString(c.getColumnIndex(KEY_TAGS_VERSEIDS)).split(",");
        for(int i = 0; i < verse_ids.length; i++) {
            verses.add(getVerse(Integer.parseInt(verse_ids[i])));
        }

        return verses;
    }

    public long[] getTaggedVersesIDs(String tag) {
        String selectQuery =
                "SELECT " + KEY_TAGS_COLOR +
                " FROM " + TABLE_TAGS +
                " WHERE " + KEY_TAGS_TAG + " LIKE \"" + tag + "\"";

        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.getCount() > 0) c.moveToFirst();
        else return new long[0];

        String[] verseIDsString = c.getString(c.getColumnIndex(KEY_TAGS_VERSEIDS)).split(",");
        long[] verseIDs = new long[verseIDsString.length];
        for(int i = 0; i < verseIDsString.length; i++) {
            verseIDs[i] = Integer.parseInt(verseIDsString[i]);
        }

        return verseIDs;
    }

//    public long[] getTaggedVersesIDs(long id) {
//        String selectQuery =
//                "SELECT *" +
//                " FROM " + TABLE_TAGS +
//                " WHERE " + KEY_TAGS_ID + " = " + id;
//
//        Cursor c = db.rawQuery(selectQuery, null);
//        if (c != null && c.getCount() > 0) c.moveToFirst();
//        else return new long[0];
//
//        String[] verseIDsString = c.getString(c.getColumnIndex(KEY_TAGS_VERSEIDS)).split(",");
//        long[] verseIDs = new long[verseIDsString.length];
//        for(int i = 0; i < verseIDsString.length; i++) {
//            verseIDs[i] = Integer.parseInt(verseIDsString[i]);
//        }
//
//        return verseIDs;
//    }

    public long addTag(String tagName, String hexColor) {
        ContentValues tag_values = new ContentValues();
        tag_values.put(KEY_TAGS_TAG, tagName);

        if(hexColor == null || hexColor.length() == 0 || !hexColor.matches("#\\d{6}")) {
            int red = (int) (Math.random() * 255);
            int green = (int) (Math.random() * 255);
            int blue = (int) (Math.random() * 255);
            String s = String.format("#%02X%02X%02X", red, green, blue);
            tag_values.put(KEY_TAGS_COLOR, s);
        }
        else {
            tag_values.put(KEY_TAGS_COLOR, hexColor);
        }

        return db.insert(TABLE_TAGS, null, tag_values);
    }

    private void addVerseToTag(long tag, long verseID) {
        String selectQuery =
                "SELECT *" +
                " FROM " + TABLE_TAGS +
                " WHERE " + KEY_TAGS_ID + " = " + tag;

        Cursor c = db.rawQuery(selectQuery, null);
        if(c != null && c.getCount() > 0) {
            String tagVerseIds = c.getString(c.getColumnIndex(KEY_TAGS_VERSEIDS));

            if(tagVerseIds == null || tagVerseIds.length() == 0) {
                tagVerseIds = verseID + ",";
                ContentValues tag_values = new ContentValues();
                tag_values.put(KEY_STATE_VERSEIDS, tagVerseIds);
                db.update(
                        TABLE_TAGS,
                        tag_values,
                        KEY_TAGS_ID + "=" + tag,
                        null);
            }
            //tag exists in table, but it does not contain this verse's id
            else {
                String[] ids = tagVerseIds.split(",");

                boolean found = false;
                for(String id : ids) {
                    if(Long.parseLong(id) == verseID) {
                        found = true;
                        break;
                    }
                }

                if(!found) {
                    //rebuild string for verses in state
                    tagVerseIds = "";
                    for(String id : ids) {
                        tagVerseIds += id + ",";
                    }
                    //add this verse to this state
                    tagVerseIds += verseID + ",";

                    //update table
                    ContentValues tag_values = new ContentValues();
                    tag_values.put(KEY_STATE_VERSEIDS, tagVerseIds);
                    db.update(
                            TABLE_STATE,
                            tag_values,
                            KEY_TAGS_ID + "=" + tag,
                            null);
                }
            }
        }
    }

    private void removeVerseFromTag(long tag, long verseID) {
        String selectQuery =
                "SELECT *" +
                " FROM " + TABLE_TAGS +
                " WHERE " + KEY_TAGS_ID + " = " + tag;

        Cursor c = db.rawQuery(selectQuery, null);
        if(c != null && c.getCount() > 0) {
            String tag_verse_ids = c.getString(c.getColumnIndex(KEY_TAGS_VERSEIDS));

            //state has no tags associated with it. How did we get here...?
            if(tag_verse_ids == null || tag_verse_ids.length() == 0) {
                Log.wtf("RemoveVerseFromTag", "ID bookkeeping messed up...");
            }
            else {
                String[] ids = tag_verse_ids.split(",");
                tag_verse_ids = "";

                for(String id : ids) {
                    //only add in the tags that are not this one
                    if(Long.parseLong(id) != verseID) {
                        tag_verse_ids += id + ",";
                    }
                }

                //update table
                ContentValues tag_values = new ContentValues();
                tag_values.put(KEY_TAGS_VERSEIDS, tag_verse_ids);
                db.update(
                        TABLE_TAGS,
                        tag_values,
                        KEY_TAGS_ID + "=" + tag,
                        null);
            }
        }
    }

    //get information about the state of a verses
    public String getStateName(int id) {
        String selectQuery =
                "SELECT *" +
                " FROM " + TABLE_STATE +
                " WHERE " + KEY_STATE_ID + " = " + id;

        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.getCount() > 0) c.moveToFirst();
        else return "";

        return c.getString(c.getColumnIndex(KEY_STATE_STATE));
    }

    public int getStateColor(int id) {
        String selectQuery =
                "SELECT *" +
                " FROM " + TABLE_STATE +
                " WHERE " + KEY_STATE_ID + " = " + id;

        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.getCount() > 0) c.moveToFirst();
        else Color.parseColor("#508A4C");

        return Color.parseColor(c.getString(c.getColumnIndex(KEY_STATE_COLOR)));
    }

    public Verses<Passage> getAllCurrentVerses() {
        String selectQuery =
                "SELECT *" +
                " FROM " + TABLE_STATE +
                " WHERE " + KEY_STATE_ID + " <= 4";

        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.getCount() > 0) c.moveToFirst();
        else return new Verses<Passage>();

        Verses<Passage> verses = new Verses<Passage>();
        String[] verse_ids = c.getString(c.getColumnIndex(KEY_STATE_VERSEIDS)).split(",");
        for(int i = 0; i < verse_ids.length; i++) {
            verses.add(getVerse(Integer.parseInt(verse_ids[i])));
        }

        return verses;
    }

    public Verses<Passage> getStateVerses(int id) {
        String selectQuery =
                "SELECT *" +
                " FROM " + TABLE_STATE +
                " WHERE " + KEY_STATE_ID + " = " + id;

        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.getCount() > 0) c.moveToFirst();
        else return new Verses<Passage>();

        Verses<Passage> verses = new Verses<Passage>();
        String[] verse_ids = c.getString(c.getColumnIndex(KEY_STATE_VERSEIDS)).split(",");
        for(int i = 0; i < verse_ids.length; i++) {
            verses.add(getVerse(Integer.parseInt(verse_ids[i])));
        }

        return verses;
    }

    private void addVerseToState(int state, long verseID) {
        String selectQuery =
                "SELECT *" +
                " FROM " + TABLE_STATE +
                " WHERE " + KEY_STATE_ID + " = " + state;

        Cursor c = db.rawQuery(selectQuery, null);
        if(c != null && c.getCount() > 0) {
            String state_verse_ids = c.getString(c.getColumnIndex(KEY_STATE_VERSEIDS));

            if(state_verse_ids == null || state_verse_ids.length() == 0) {
                state_verse_ids = verseID + ",";
                ContentValues state_values = new ContentValues();
                state_values.put(KEY_STATE_VERSEIDS, state_verse_ids);
                db.update(
                        TABLE_STATE,
                        state_values,
                        KEY_STATE_ID + "=" + state,
                        null);
            }
            //state exists in table, but it does not contain this verse's id
            else {
                String[] ids = state_verse_ids.split(",");

                boolean found = false;
                for(String id : ids) {
                    if(Long.parseLong(id) == verseID) {
                        found = true;
                        break;
                    }
                }

                if(!found) {
                    //rebuild string for verses in state
                    state_verse_ids = "";
                    for(String id : ids) {
                        state_verse_ids += id + ",";
                    }
                    //add this verse to this state
                    state_verse_ids += verseID + ",";

                    //update table
                    ContentValues state_values = new ContentValues();
                    state_values.put(KEY_STATE_VERSEIDS, state_verse_ids);
                    db.update(
                            TABLE_STATE,
                            state_values,
                            KEY_TAGS_ID + "=" + state,
                            null);
                }
            }
        }
    }

    private void removeVerseFromState(long state, long verseID) {
        String selectQuery =
                "SELECT *" +
                " FROM " + TABLE_STATE +
                " WHERE " + KEY_STATE_ID + " = " + state;

        Cursor c = db.rawQuery(selectQuery, null);
        if(c != null && c.getCount() > 0) {
            String state_verse_ids = c.getString(c.getColumnIndex(KEY_STATE_VERSEIDS));

            //state has no tags associated with it. How did we get here...?
            if(state_verse_ids == null || state_verse_ids.length() == 0) {
                Log.wtf("RemoveVerseFromState", "ID bookkeeping messed up...");
            }
            else {
                String[] ids = state_verse_ids.split(",");
                state_verse_ids = "";

                for(String id : ids) {
                    //only add in the tags that are not this one
                    if(Long.parseLong(id) != verseID) {
                        state_verse_ids += id + ",";
                    }
                }

                //update table
                ContentValues state_values = new ContentValues();
                state_values.put(KEY_STATE_VERSEIDS, state_verse_ids);
                db.update(
                        TABLE_STATE,
                        state_values,
                        KEY_TAGS_ID + "=" + state,
                        null);
            }
        }
    }
}
