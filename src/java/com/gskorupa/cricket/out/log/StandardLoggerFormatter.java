/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gskorupa.cricket.out.log;

import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author greg
 */
public class StandardLoggerFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        String sdate=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(record.getMillis());
        return record.getLevel() + ":" +sdate+": "+ record.getMessage()+"\n";
    }
}
