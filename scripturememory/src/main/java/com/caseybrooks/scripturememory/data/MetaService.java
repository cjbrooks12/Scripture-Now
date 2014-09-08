package com.caseybrooks.scripturememory.data;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MetaService {
    //Caches the Verse of the Day
    public class VOTDService extends Service {
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            return Service.START_NOT_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }

    public class SearchService extends Service {
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            //TODO do something useful
            return Service.START_NOT_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}
