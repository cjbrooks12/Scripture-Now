package com.caseybrooks.common.util.clog.loggers;

import android.util.Log;

import com.caseybrooks.common.util.clog.ClogFormatter;
import com.caseybrooks.common.util.clog.ClogLogger;

public class ClogE implements ClogLogger, ClogFormatter {

    @Override
    public int log(String tag, String message) {
        return Log.e(tag, message);
    }

    @Override
    public int log(String tag, String message, Throwable throwable) {
        return Log.e(tag, message, throwable);
    }

    @Override
    public Object format(Object data, Object[] params) {
        Log.e("ClogE", data.toString());
        return data;
    }
}
