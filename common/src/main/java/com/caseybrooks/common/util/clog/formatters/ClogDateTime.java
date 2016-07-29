package com.caseybrooks.common.util.clog.formatters;

import com.caseybrooks.common.util.clog.ClogFormatter;

public class ClogDateTime implements ClogFormatter {
    @Override
    public String getKey() {
        return "datetime";
    }

    @Override
    public String format(Object data) {
        return data.getClass().toString();
    }
}
