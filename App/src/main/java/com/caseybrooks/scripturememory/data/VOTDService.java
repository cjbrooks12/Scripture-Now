package com.caseybrooks.scripturememory.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.caseybrooks.androidbibletools.basic.Passage;
import com.caseybrooks.androidbibletools.search.VerseOfTheDay;

import java.util.Calendar;

public class VOTDService {
    public VOTDService() {

    }

    //either returns a verse, verifying that it is today's verse, or it returns
    //null, indicating that we must download today's verse
    public static Passage getCurrentVerse(Context context) {

        //get all verses that are either tagged or in the state of VOTD
        VerseDB db = new VerseDB(context).open();
        Passage mostRecent = db.getMostRecentVOTD();
        db.close();

        //no VOTD exist in database at this time
        if(mostRecent != null) {
            //check the timestamp of the most recent verse against the current time.
            //if the current verse is on the same day as today, then it is current.
            //if the current verse is not today, it needs to get updated
            Calendar today = Calendar.getInstance();
            Calendar current = Calendar.getInstance();
            current.setTimeInMillis(mostRecent.getMillis());

            boolean isCurrent =
                (today.get(Calendar.ERA) == current.get(Calendar.ERA)
                && today.get(Calendar.YEAR) == current.get(Calendar.YEAR)
                && today.get(Calendar.DAY_OF_YEAR) == current.get(Calendar.DAY_OF_YEAR));

            if(isCurrent) {
                return mostRecent;
            }
        }

        return null;
    }

//Asynchronously download verses
//------------------------------------------------------------------------------
    public static class GetVOTD extends AsyncTask<Void, Void, Passage> {
        Context context;
        GetVerseListener listener;

        public GetVOTD(Context context, GetVerseListener listener) {
            this.context = context;
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            listener.onPreDownload();
        }

        @Override
        protected Passage doInBackground(Void... params) {
            try {
                Passage passage = VerseOfTheDay.retrieve(MetaSettings.getBibleVersion(context));

                if(passage != null) {
                    passage.addTag("VOTD");
                    passage.setState(VerseDB.VOTD);

                    VerseDB db = new VerseDB(context).open();
                    db.insertVerse(passage);
                    db.close();

                    return passage;
                }
                else {
                    return null;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                Log.e("GetVOTD.doInBackground()", "Exception thrown while retrieving verse", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Passage passage) {
            super.onPostExecute(passage);
                listener.onVerseDownloaded(passage);
        }
    }

//Callback to return a Verse
//------------------------------------------------------------------------------
    public static interface GetVerseListener {
        public void onPreDownload();
        public void onVerseDownloaded(Passage passage);
    }
}

