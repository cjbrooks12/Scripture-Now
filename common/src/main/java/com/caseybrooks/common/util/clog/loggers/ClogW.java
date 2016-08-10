package com.caseybrooks.common.util.clog.loggers;

import android.util.Log;

import com.caseybrooks.common.util.clog.ClogFormatter;
import com.caseybrooks.common.util.clog.ClogLogger;

public class ClogW implements ClogLogger, ClogFormatter {

    @Override
    public int log(String tag, String message) {
        return Log.w(tag, message);
    }

    @Override
    public int log(String tag, String message, Throwable throwable) {
        return Log.w(tag, message, throwable);
    }

    @Override
    public Object format(Object data, Object[] params) {
        Log.w("ClogW", data.toString());
        return data;
    }
}
