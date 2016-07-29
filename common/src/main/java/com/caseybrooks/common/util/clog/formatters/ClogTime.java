package com.caseybrooks.common.util.clog.formatters;

import com.caseybrooks.common.util.clog.ClogFormatter;

public class ClogTime implements ClogFormatter {
    @Override
    public String getKey() {
        return "time";
    }

    @Override
    public String format(Object data) {
        return data.getClass().toString();
    }
}
