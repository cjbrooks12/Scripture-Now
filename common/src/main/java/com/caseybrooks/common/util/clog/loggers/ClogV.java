package com.caseybrooks.common.util.clog.loggers;

import android.util.Log;

import com.caseybrooks.common.util.clog.ClogFormatter;
import com.caseybrooks.common.util.clog.ClogLogger;

public class ClogV implements ClogLogger, ClogFormatter {

    @Override
    public int log(String tag, String message) {
        return Log.v(tag, message);
    }

    @Override
    public int log(String tag, String message, Throwable throwable) {
        return Log.v(tag, message, throwable);
    }

    @Override
    public Object format(Object data, Object[] params) {
        Log.v("ClogV", data.toString());
        return data;
    }
}
