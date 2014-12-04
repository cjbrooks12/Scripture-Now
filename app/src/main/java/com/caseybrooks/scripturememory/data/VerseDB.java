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
import com.caseybrooks.scripturememory.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

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
    public static final String KEY_TAGS_COLOR = "color";

    private static final String CREATE_TABLE_TAGS =
            "CREATE TABLE " + TABLE_TAGS + "(" +
                    KEY_TAGS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_TAGS_TAG + " TEXT NOT NULL unique, " +
                    KEY_TAGS_COLOR + " TEXT NOT NULL);";

    //fields for state table
    private static final String TABLE_STATE = "state";
    public static final String KEY_STATE_ID = "_id";
    public static final String KEY_STATE_STATE = "state";
    public static final String KEY_STATE_COLOR = "color";

    private static final String CREATE_TABLE_STATE =
            "CREATE TABLE " + TABLE_STATE + "(" +
                    KEY_STATE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_STATE_STATE + " TEXT NOT NULL unique, " +
                    KEY_STATE_COLOR + " TEXT NOT NULL);";

    public static final int CURRENT_NONE = 1;
    public static final int CURRENT_SOME = 2;
    public static final int CURRENT_MOST = 3;
    public static final int CURRENT_ALL = 4;
    public static final int MEMORIZED = 5;
    public static final int DELETED = 6;
    public static final int ARCHIVED = 7;
    public static final int CACHED = 8;
    public static final int VOTD = 9;
    public static final int ALL_VERSES = -1;
    public static final int CURRENT = -2;

    //Database helper class
//--------------------------------------------------------------------------
    private static class DbHelper extends SQLiteOpenHelper {
        Context context;

        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            // creating required tables
            db.execSQL(CREATE_TABLE_VERSES);
            db.execSQL(CREATE_TABLE_TAGS);
            db.execSQL(CREATE_TABLE_STATE);

            ContentValues current_none = new ContentValues();
            current_none.put(KEY_STATE_STATE, "Current - None");
            String colorNone = String.format("#%06X", (0xFFFFFF & context.getResources().getColor(R.color.current_none)));
            current_none.put(KEY_STATE_COLOR, colorNone);
            db.insert(TABLE_STATE, null, current_none);

            ContentValues current_some = new ContentValues();
            current_some.put(KEY_STATE_STATE, "Current - Some");
            String colorSome = String.format("#%06X", (0xFFFFFF & context.getResources().getColor(R.color.current_some)));
            current_some.put(KEY_STATE_COLOR, colorSome);
            db.insert(TABLE_STATE, null, current_some);

            ContentValues current_most = new ContentValues();
            current_most.put(KEY_STATE_STATE, "Current - Most");
            String colorMost = String.format("#%06X", (0xFFFFFF & context.getResources().getColor(R.color.current_most)));
            current_most.put(KEY_STATE_COLOR, colorMost);
            db.insert(TABLE_STATE, null, current_most);

            ContentValues current_all = new ContentValues();
            current_all.put(KEY_STATE_STATE, "Current - All");
            String colorAll = String.format("#%06X", (0xFFFFFF & context.getResources().getColor(R.color.current_all)));
            current_all.put(KEY_STATE_COLOR, colorAll);
            db.insert(TABLE_STATE, null, current_all);

            ContentValues memorized = new ContentValues();
            memorized.put(KEY_STATE_STATE, "Memorized");
            String colorMemorized = String.format("#%06X", (0xFFFFFF & context.getResources().getColor(R.color.memorized)));
            memorized.put(KEY_STATE_COLOR, colorMemorized);
            db.insert(TABLE_STATE, null, memorized);

            ContentValues deleted = new ContentValues();
            deleted.put(KEY_STATE_STATE, "Deleted");
            deleted.put(KEY_STATE_COLOR, "#666688");
            db.insert(TABLE_STATE, null, deleted);

            ContentValues archived = new ContentValues();
            archived.put(KEY_STATE_STATE, "Archived");
            archived.put(KEY_STATE_COLOR, "#EDEDA0");
            db.insert(TABLE_STATE, null, archived);

            ContentValues cached = new ContentValues();
            cached.put(KEY_STATE_STATE, "Cached");
            cached.put(KEY_STATE_COLOR, "#666688");
            db.insert(TABLE_STATE, null, cached);

            ContentValues votd = new ContentValues();
            votd.put(KEY_STATE_STATE, "VOTD");
            votd.put(KEY_STATE_COLOR, "#8F0DED");
            db.insert(TABLE_STATE, null, votd);

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

        String commaSeparatedTags = c.getString(c.getColumnIndex(KEY_VERSES_TAGS));

        if(commaSeparatedTags.length() > 1) {
            String[] tagNumbers = commaSeparatedTags.split(",");
            String[] tagNames = new String[tagNumbers.length - 1];
            for (int i = 1; i < tagNumbers.length; i++) {
                if (tagNumbers[i].length() >= 1) {
                    Log.i("Add tag to verse", tagNumbers[i]);

                    tagNames[i-1] = getTagName(Integer.parseInt(tagNumbers[i]));
                }
            }
            passage.setTags(tagNames);
        }

        return passage;
    }

    public int insertVerse(Passage passage) {
        //set values for each field in this verse to be either created or updated
        ContentValues values = new ContentValues();
        values.put(KEY_VERSES_REFERENCE, passage.getReference());
        values.put(KEY_VERSES_VERSE, passage.getText());
        values.put(KEY_VERSES_VERSION, passage.getVersion().getCode());
        values.put(KEY_VERSES_DATE_ADDED, Calendar.getInstance().getTimeInMillis());
        values.put(KEY_VERSES_DATE_MODIFIED, Calendar.getInstance().getTimeInMillis());
        values.put(KEY_VERSES_STATE, passage.getState());

        //ensure tags on this verse are up-to-date
        String[] tags = passage.getTags();
        String tag_string = ",";
        for(String tag : tags) {
            long tagID = getTagID(tag);

            //tag does not yet exist, create it
            if(tagID == -1) {
                tagID = addTag(tag, null);
            }

            tag_string += tagID + ",";
        }
        values.put(KEY_VERSES_TAGS, tag_string);

        return (int) db.insert(TABLE_VERSES, null, values);
    }

    public void updateVerse(Passage passage) {
        //set values for each field in this verse to be either created or updated
        ContentValues values = new ContentValues();
        values.put(KEY_VERSES_REFERENCE, passage.getReference());
        values.put(KEY_VERSES_VERSE, passage.getText());
        values.put(KEY_VERSES_VERSION, passage.getVersion().getCode());
        values.put(KEY_VERSES_DATE_MODIFIED, Calendar.getInstance().getTimeInMillis());
        values.put(KEY_VERSES_STATE, passage.getState());

        //ensure tags on this verse are up-to-date
        String[] tags = passage.getTags();
        String tag_string = ",";
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

        cleanupTags();
    }

//I may need to clean up below this line
//------------------------------------------------------------------------------

    public Passage getMostRecentVOTD() {
        //SELECT 'all verses that are in VOTD state or have VOTD tag' ORDER BY 'date added in descending order'
        String selectQuery =
                "SELECT *" +
                " FROM " + TABLE_VERSES +
                " WHERE " + KEY_VERSES_STATE + " = " + VOTD +
                " OR " + KEY_VERSES_TAGS + " LIKE '%," + getTagID("VOTD") + ",%'" +
                " ORDER BY " + KEY_VERSES_DATE_ADDED + " DESC ";

        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.getCount() > 0) {
            //in descending order, the most recent verse is the first one
            c.moveToFirst();
            return getVerse(c.getInt(c.getColumnIndex(KEY_VERSES_ID)));
        }
        else return null;
    }

    public void updateTag(int id, String name, String hexColor) {
        //check to see if the name to change the tag to already exists
        long existingTagId = getTagID(name);

        if(existingTagId == -1) {
            ContentValues values = new ContentValues();
            if (name != null && name.length() > 0) {
                values.put(KEY_TAGS_TAG, name);
            }

            if (hexColor != null && hexColor.length() > 0 && hexColor.matches("#\\d{6}")) {
                values.put(KEY_TAGS_COLOR, hexColor);
            }

            db.update(TABLE_TAGS, values, KEY_TAGS_ID + "=" + id, null);
        }
    }

    public int[] getAllTagIds() {
        String selectQuery =
                "SELECT *" +
                " FROM " + TABLE_TAGS;

        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.getCount() > 0) c.moveToFirst();
        else return null;

        ArrayList<String> tagNames = new ArrayList<String>();
        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            int tagCount = c.getInt(c.getColumnIndex(KEY_TAGS_ID));

            if(getTagCount(tagCount) > 0) {
                tagNames.add(c.getString(c.getColumnIndex(KEY_TAGS_TAG)));
            }
        }

        Collections.sort(tagNames);

        int[] tagIds = new int[tagNames.size()];
        for(int i = 0; i < tagNames.size(); i++) {
            tagIds[i] = (int)getTagID(tagNames.get(i));
        }

        return tagIds;
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
                "SELECT " + KEY_TAGS_ID +
                " FROM " + TABLE_TAGS +
                " WHERE " + KEY_TAGS_TAG + " LIKE \"" + tag + "\"";

        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.getCount() > 0) c.moveToFirst();
        else return -1;

        return c.getInt(c.getColumnIndex(KEY_TAGS_ID));
    }

    public int getTagColor(String tag) {
        String selectQuery =
                "SELECT *" +
                " FROM " + TABLE_TAGS +
                " WHERE " + KEY_TAGS_TAG + " LIKE \"" + tag + "\"";

        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.getCount() > 0) c.moveToFirst();
        else return Color.parseColor("#508A4C");

        return Color.parseColor(c.getString(c.getColumnIndex(KEY_TAGS_COLOR)));
    }

    public Verses<Passage> getTaggedVerses(int tagId) {
        String selectQuery =
                "SELECT *" +
                    " FROM " + TABLE_VERSES +
                    " WHERE " + KEY_VERSES_TAGS + " LIKE '%," + tagId + ",%'" +
                    " AND " + KEY_VERSES_STATE + " < 6";


        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.getCount() > 0) c.moveToFirst();
        else return new Verses<Passage>();

        Verses<Passage> verses = new Verses<Passage>();
        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            verses.add(getVerse(c.getInt(c.getColumnIndex(KEY_VERSES_ID))));
        }

        return verses;
    }

    public int getTagCount(int tagId) {
        String selectQuery =
                "SELECT *" +
                " FROM " + TABLE_VERSES +
                " WHERE " + KEY_VERSES_TAGS + " LIKE '%," + tagId + ",%'" +
                " AND " + KEY_VERSES_STATE + " < 6";


        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.getCount() > 0) return c.getCount();
        else return 0;
    }

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

    private void cleanupTags() {
        String selectQuery =
                "SELECT *" +
                " FROM " + TABLE_TAGS;

        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.getCount() > 0) c.moveToFirst();
        else return;

        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            int tagId = c.getInt(c.getColumnIndex(KEY_TAGS_ID));
            if(getTagCount(tagId) == 0) {
                deleteTag(tagId);
            }
        }
    }

    public void deleteTag(int id) {
        //TODO: change to just iterate over a cursor and update the tag fields directly
        Verses<Passage> verses = getTaggedVerses(id);
        for(int i = 0; i < verses.size(); i++) {
            String[] tags = verses.get(i).getTags();
            ArrayList<String> tagsList = new ArrayList<String>();
            Collections.addAll(tagsList, tags);
            tagsList.remove(getTagName(id));
            verses.get(i).removeAllTags();
            verses.get(i).setTags(tagsList.toArray(new String[tagsList.size()]));
            updateVerse(verses.get(i));
        }
        db.delete(TABLE_TAGS, KEY_TAGS_ID + "=" + id, null);
    }

    //get information about the state of a verses
    public String getStateName(int id) {
        if(id == ALL_VERSES) return "All Verses";
        else if(id == CURRENT) return "Current";
        else {
            String selectQuery =
                    "SELECT *" +
                            " FROM " + TABLE_STATE +
                            " WHERE " + KEY_STATE_ID + " = " + id;

            Cursor c = db.rawQuery(selectQuery, null);
            if (c != null && c.getCount() > 0) c.moveToFirst();
            else return "";

            return c.getString(c.getColumnIndex(KEY_STATE_STATE));
        }
    }

    public int getStateColor(int id) {
        if(id == ALL_VERSES) return context.getResources().getColor(R.color.all_verses);
        else if(id == CURRENT) return context.getResources().getColor(R.color.all_current_verses);
        else {
            String selectQuery =
                    "SELECT *" +
                            " FROM " + TABLE_STATE +
                            " WHERE " + KEY_STATE_ID + " = " + id;

            Cursor c = db.rawQuery(selectQuery, null);
            if (c != null && c.getCount() > 0) c.moveToFirst();
            else return Color.parseColor("#508A4C");

            return Color.parseColor(c.getString(c.getColumnIndex(KEY_STATE_COLOR)));
        }
    }

    public int getStateCount(int id) {
        if(id == ALL_VERSES) return getAllVerses().size();
        else if(id == CURRENT) return getAllCurrentVerses().size();
        else {
            String selectQuery =
                    "SELECT *" +
                            " FROM " + TABLE_VERSES +
                            " WHERE " + KEY_VERSES_STATE + " = " + id;


            Cursor c = db.rawQuery(selectQuery, null);
            if (c != null && c.getCount() > 0) return c.getCount();
            else return 0;
        }
    }

    public Verses<Passage> getAllVerses() {
        String selectQuery =
                "SELECT *" +
                " FROM " + TABLE_VERSES +
                " WHERE " + KEY_VERSES_STATE + " < 6";

        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.getCount() > 0) c.moveToFirst();
        else return new Verses<Passage>();

        Verses<Passage> verses = new Verses<Passage>();
        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            verses.add(getVerse(c.getInt(c.getColumnIndex(KEY_VERSES_ID))));
        }

        return verses;
    }

    public Verses<Passage> getAllCurrentVerses() {
        String selectQuery =
                "SELECT *" +
                " FROM " + TABLE_VERSES +
                " WHERE " + KEY_VERSES_STATE + " <= 4";

        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.getCount() > 0) c.moveToFirst();
        else return new Verses<Passage>();

        Verses<Passage> verses = new Verses<Passage>();
        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            verses.add(getVerse(c.getInt(c.getColumnIndex(KEY_VERSES_ID))));
        }

        return verses;
    }

    public Verses<Passage> getStateVerses(int id) {
        if(id == ALL_VERSES) return getAllVerses();
        else if(id == CURRENT) return getAllCurrentVerses();
        else {
            String selectQuery =
                    "SELECT *" +
                    " FROM " + TABLE_VERSES +
                    " WHERE " + KEY_VERSES_STATE + " = " + id;

            Cursor c = db.rawQuery(selectQuery, null);
            if (c != null && c.getCount() > 0) c.moveToFirst();
            else return new Verses<Passage>();

            Verses<Passage> verses = new Verses<Passage>();
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                verses.add(getVerse(c.getInt(c.getColumnIndex(KEY_VERSES_ID))));
            }

            return verses;
        }
    }

    public void updateState(int id, String color) {
        ContentValues values = new ContentValues();
        values.put(KEY_STATE_COLOR, color);

        db.update(TABLE_STATE, values, KEY_STATE_ID + "=" + id, null);
    }


}
