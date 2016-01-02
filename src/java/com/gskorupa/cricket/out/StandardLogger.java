/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gskorupa.cricket.out;

import com.gskorupa.cricket.Adapter;
import com.gskorupa.cricket.Event;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author greg
 */
public class StandardLogger extends OutboundAdapter implements Adapter, LoggerAdapterIface {

    Logger logger;
    Level level=null;
    private String name;

    public void loadProperties(Properties properties) {
        setName(properties.getProperty("LoggerAdapterIface-name"));
        System.out.println("logger name: " + getName());
        setLoggingLevel(properties.getProperty("LoggerAdapterIface-level"));
        System.out.println("logging level: " + getLoggingLevel());
        logger = Logger.getLogger(getName());
        logger.setLevel(level);
    }

    public void log(Event event) {
        String level=event.getType();
        switch(level){
            case "LOG_INFO":
                logger.log(Level.INFO, event.toString());
                break;
            case "LOG_FINEST":
                logger.log(Level.FINEST, event.toString());
                break;
            case "LOG_WARNING":
                logger.log(Level.WARNING, event.toString());
                break;
            case "LOG_SEVERE":
                logger.log(Level.SEVERE, event.toString());
                break;
            default:
                logger.log(Level.FINEST, event.toString());
                break;
        }
    }

    private void setLoggingLevel(String level) {
        try {
            this.level = Level.parse(level);
        } catch (Exception e) {
            this.level = Level.ALL;
        }
    }

    private Level getLoggingLevel() {
        if(level==null){
            level=Level.ALL;
        }
        return level;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        if (null != name && !name.isEmpty()) {
            this.name = name;
        } else {
            this.name = this.getClass().getName();
        }
    }
}
