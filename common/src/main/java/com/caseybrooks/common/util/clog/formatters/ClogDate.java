package com.caseybrooks.common.util.clog.formatters;

import com.caseybrooks.common.util.clog.ClogFormatter;

public class ClogDate implements ClogFormatter {
    @Override
    public String getKey() {
        return "date";
    }

    @Override
    public String format(Object data) {
        return data.getClass().toString();
    }
}
