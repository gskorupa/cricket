/*
 * Copyright 2016 Grzegorz Skorupa <g.skorupa at gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        //System.out.println("logger level set to: " + logger.getLevel().getName());
    }

    public void log(Event event) {
        String level=event.getType();
        switch(level){
            case "LOG_INFO":
            case "INFO":
                logger.log(Level.INFO, event.toString());
                break;
            case "LOG_FINEST":
            case "FINEST":
                logger.log(Level.FINEST, event.toString());
                break;
            case "LOG_WARNING":
            case "WARNING":
                logger.log(Level.WARNING, event.toString());
                break;
            case "LOG_SEVERE":
            case "SEVERE":
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
