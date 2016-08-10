package com.caseybrooks.common.util.clog.loggers;

import android.util.Log;

import com.caseybrooks.common.util.clog.ClogFormatter;
import com.caseybrooks.common.util.clog.ClogLogger;

public class ClogI implements ClogLogger, ClogFormatter {

    @Override
    public int log(String tag, String message) {
        return Log.i(tag, message);
    }

    @Override
    public int log(String tag, String message, Throwable throwable) {
        return Log.i(tag, message, throwable);
    }

    @Override
    public Object format(Object data, Object[] params) {
        Log.i("ClogI", data.toString());
        return data;
    }
}
