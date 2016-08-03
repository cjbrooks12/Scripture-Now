package com.caseybrooks.common.features.votd;

import com.caseybrooks.androidbibletools.data.OnResponseListener;
import com.caseybrooks.androidbibletools.providers.votd.VerseOfTheDay;

public class VerseOfTheDayService {

    OnResponseListener listener;
    VerseOfTheDay verseOfTheDay;

    public void run(OnResponseListener listener) {
        this.listener = listener;

        verseOfTheDay = new VerseOfTheDay();
        verseOfTheDay.download(initialVerseListener);
    }

    OnResponseListener initialVerseListener = new OnResponseListener() {
        @Override
        public void responseFinished(boolean b) {
            listener.responseFinished(b);
        }
    };

    public VerseOfTheDay getVerseOfTheDay() {
        return verseOfTheDay;
    }
}
