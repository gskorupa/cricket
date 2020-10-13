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
package org.cricketmsf.out.log;

import java.io.IOException;
import org.cricketmsf.Adapter;
import org.cricketmsf.event.Event;
import org.cricketmsf.out.OutboundAdapter;
import java.util.HashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cricketmsf.Kernel;

/**
 *
 * @author greg
 */
public class StandardLogger extends OutboundAdapter implements Adapter, LoggerAdapterIface {

    //Logger logger;
    private Level level = null;
    private String name;
    private String fileLocation;
    private boolean muted = false;
    private boolean consoleHandler = true;
    private Logger logger = null;
    protected int maxSize = 0;
    protected int count = 0;

    public StandardLogger getDefault() {
        logger = Logger.getLogger("Kernel");
        logger.setUseParentHandlers(false);
        Handler systemOut = new ConsoleHandler();
        systemOut.setFormatter(new StandardLoggerFormatter());
        logger.addHandler(systemOut);
        return this;
    }

    /**
     * This method is executed while adapter is instantiated during the service
     * start. It's used to configure the adapter according to the configuration.
     *
     * @param properties map of properties readed from the configuration file
     * @param adapterName name of the adapter set in the configuration file (can
     * be different from the interface and class name.
     */
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        setName(properties.get("name"));
        Kernel.getInstance().getLogger().print("\tlogger name: " + getName());
        setFileLocation(properties.get("log-file-name"));
        Kernel.getInstance().getLogger().print("\tlog-file-name: " + getFileLocation());
        setConsoleHandler(properties.getOrDefault("console", "true"));
        Kernel.getInstance().getLogger().print("\tlog to console: " + isConsoleHandler());
        setLoggingLevel(properties.get("level"));
        Handler systemOut = new ConsoleHandler();
        systemOut.setLevel(level);
        systemOut.setFormatter(new StandardLoggerFormatter());
        logger = Logger.getLogger(getName());
        // Prevent logs from processed by default Console handler.
        logger.setUseParentHandlers(false);
        if (isConsoleHandler()) {
            logger.addHandler(systemOut);
        }
        setMaxSize(properties.get("max-size"));
        Kernel.getInstance().getLogger().print("\tmax-size: " + maxSize);
        setCount(properties.get("count"));
        Kernel.getInstance().getLogger().print("\tfile count: " + count);
        
        if (null != getFileLocation() && !getFileLocation().isEmpty()) {
            try {
                
                Handler fileOut;
                fileOut = new FileHandler(getFileLocation(), false);
                if(count>0 && maxSize>0 && getFileLocation().indexOf("%g")>0){
                    fileOut = new FileHandler(getFileLocation(), 1000000, 10, true);
                }
                fileOut.setLevel(level);
                fileOut.setFormatter(new StandardLoggerFormatter());
                logger.addHandler(fileOut);
            } catch (IOException e) {
                Kernel.getInstance().getLogger().print(e.getMessage());
            }
        }
        logger.setLevel(level);
        Kernel.getInstance().getLogger().print("\tlogging level: " + logger.getLevel().getName());
    }

    @Override
    public void log(Event event) {
        if (isMuted()) {
            return;
        }
        
        Kernel.getInstance().getThreadFactory().newThread(() -> {
        //new Thread(() -> {
            Level tmpLevel;
            switch (event.getType()) {
                case "LOG_INFO":
                case "INFO":
                    tmpLevel = Level.INFO;
                    break;
                case "LOG_FINEST":
                case "FINEST":
                    tmpLevel = Level.FINEST;
                    break;
                case "LOG_FINER":
                case "FINER":
                    tmpLevel = Level.FINER;
                    break;
                case "LOG_FINE":
                case "FINE":
                    tmpLevel = Level.FINE;
                    break;
                case "LOG_WARNING":
                case "WARNING":
                    tmpLevel = Level.WARNING;
                    break;
                case "LOG_SEVERE":
                case "SEVERE":
                    tmpLevel = Level.SEVERE;
                    break;
                default:
                    tmpLevel = Level.FINEST;
                    break;
            }
            logger.log(tmpLevel, event.toLogString());
        },"StandardLogger").start();

    }

    @Override
    public void print(String message) {
        if (logger != null) {
            logger.log(Level.INFO, message);
        }
    }
    
    @Override
    public void printIndented(String message) {
        print("    ".concat(message));
    }

    private void setLoggingLevel(String level) {
        try {
            if (level.equalsIgnoreCase("NONE")) {
                this.level = Level.parse("SEVERE");
                setMuted(true);
            } else {
                this.level = Level.parse(level);
            }
        } catch (Exception e) {
            this.level = Level.ALL;
        }
    }

    private Level getLoggingLevel() {
        if (level == null) {
            level = Level.ALL;
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

    /**
     * @return the fileLocation
     */
    public String getFileLocation() {
        return fileLocation;
    }

    /**
     * @param fileLocation the fileLocation to set
     */
    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    /**
     * @return the muted
     */
    public boolean isMuted() {
        return muted;
    }

    /**
     * @param muted the muted to set
     */
    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    /**
     * @return the consoleHandler
     */
    public boolean isConsoleHandler() {
        return consoleHandler;
    }

    /**
     * @param useConsole the consoleHandler to set
     */
    public void setConsoleHandler(String useConsole) {
        this.consoleHandler = useConsole.equalsIgnoreCase("true");
    }

    /**
     * @param maxSize the maxSize to set
     */
    public void setMaxSize(String maxSize) {
        try{
            this.maxSize = Integer.parseInt(maxSize);
        }catch(NumberFormatException e){
            
        }
    }

    /**
     * @param count the count to set
     */
    public void setCount(String count) {
        try{
            this.count = Integer.parseInt(count);
        }catch(NumberFormatException e){
            
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isFineLevel() {
        return level.equals(Level.FINE)||level.equals(Level.FINER)||level.equals(Level.FINEST);
    }

}
