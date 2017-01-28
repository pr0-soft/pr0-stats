package com.pr0gramm.statistics.toolbox;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Created by koray on 28/01/2017.
 */
public class SimpleConsoleFormatter extends Formatter {

    //	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    @Override
    public String format(LogRecord record) {
        Date date = new Date(record.getMillis());
        return "[" + sdf.format(date) + " " + record.getLevel() + "] " + record.getMessage() + "\n";
    }
}
