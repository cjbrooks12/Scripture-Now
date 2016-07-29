package com.caseybrooks.common.util.clog;

public interface ClogFormatter {
    String getKey();
    String format(Object data);
}
