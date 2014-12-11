package com.caseybrooks.scripturememory.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.enumeration.Version;
import com.caseybrooks.androidbibletools.search.VerseOfTheDay;

import java.io.IOException;

/* Simple ASyncTask class to retrieve the Verse of the Day. Returns null when
*   something goes wrong
*/
public class VOTDGetTask extends AsyncTask<Void, Void, Passage> {
    Context context;
    Version version;
    private OnTaskCompletedListener listener;

    public VOTDGetTask(Context context, Version version, OnTaskCompletedListener listener) {
        this.context = context;
        this.version = version;
        this.listener = listener;
    }

    @Override
    protected Passage doInBackground(Void... params) {
        try {
            Passage passage = VerseOfTheDay.retrieve(version);

            return passage;
        } catch (IOException ioException) {
            Log.e("VOTDGetTask.doInBackground()", "Exception thrown while retrieving verse", ioException);
            return null;
        } catch (Exception exception) {
            Log.e("VOTDGetTask.doInBackground()", "Unknown exception thrown", exception);
            return null;
        }
    }

    @Override
    protected void onPostExecute(Passage passage) {
        super.onPostExecute(passage);
        listener.onTaskCompleted(passage);
    }
}
