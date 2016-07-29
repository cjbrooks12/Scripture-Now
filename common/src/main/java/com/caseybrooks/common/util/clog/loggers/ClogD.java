package com.caseybrooks.common.util.clog.loggers;

import android.util.Log;

import com.caseybrooks.common.util.clog.ClogLogger;

public class ClogD implements ClogLogger {

    @Override
    public void log(String tag, String message) {
        Log.d(tag, message);
    }
}
