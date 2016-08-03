package com.caseybrooks.common.util.clog.formatters;

import com.caseybrooks.common.util.clog.ClogFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ClogDate implements ClogFormatter {
    @Override
    public Object format(Object data, Object[] params) {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar now = Calendar.getInstance();

        return dateFormat.format(now.getTime());
    }
}
