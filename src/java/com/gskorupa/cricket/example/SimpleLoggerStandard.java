/*
 * Copyright 2015 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package com.gskorupa.cricket.example;

import com.gskorupa.cricket.Adapter;
import com.sun.net.httpserver.HttpExchange;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.net.httpserver.HttpHandler;

/**
 *
 * @author grzesk
 */
public class SimpleLoggerStandard implements SimpleLoggerIface, Adapter {

    private static final Logger logger = Logger.getLogger(com.gskorupa.cricket.example.SimpleLoggerStandard.class.getName());

    private String context=null;
    
    public void loadProperties(Properties properties){  
        context=properties.getProperty("SimpleLogger-context");
        System.out.println("context="+context);
    }
    
    public String getContext(){
        return context;
    }
    
    public void log(String level, int errorCode, Object o) {
        log(level, errorCode, o, null);
    }
    public void log(String level, int errorCode, Object o, String message) {
        try {
            if(message==null) message="";
            logger.log(Level.parse(level), o.getClass().getName()+": "+ Integer.toString(errorCode)+" "+message);
        } catch (NullPointerException | IllegalArgumentException e) {
            logger.severe(o.getClass().getName()+":"+e.getStackTrace()[0].toString());
        }
    }

}
