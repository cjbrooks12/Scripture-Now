package com.caseybrooks.common.util.clog;

public interface ClogLogger {
    int log(String tag, String message);
    int log(String tag, String message, Throwable throwable);
}
