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
package com.gskorupa.cricket;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * SimpleService
 *
 * @author greg
 */
public abstract class Kernel {

    // emergency logger
    private static final Logger logger = Logger.getLogger(com.gskorupa.cricket.Kernel.class.getName());

    // singleton
    private static Object instance = null;

    // adapters
    public static Object[] fields = {};
    public static Class[] adapters = {};

    // http server
    private String host = null;
    private int port = 0;
    private Httpd httpd;
    private boolean httpHandlerLoaded = false;

    public Kernel() {
    }

    public abstract void getAdapters();

    public static Kernel getInstance() {
        return (Kernel) instance;
    }

    /*
    public void setInstance(Object instance){
        this.instance=instance;
    }
     */
    public static Object getInstance(Class c, String path) {
        if (instance != null) {
            return instance;
        }
        Properties props = null;
        try {
            InputStream propertyFile = new FileInputStream(new File(path));
            props = new Properties();
            props.load(propertyFile);
            return getInstanceWithProperties(c, props);
        } catch (Exception e) {
            logger.severe("Adapters initialization error. Configuration: " + path);
        }
        return null;
    }

    public static Object getInstanceWithProperties(Class c, Properties props) {
        if (instance != null) {
            return instance;
        }
        try {
            instance = c.getClass().newInstance();
            ((Kernel) instance).loadAdapters(props, fields, adapters);
        } catch (Exception e) {
            instance = null;
            logger.severe(e.getStackTrace()[0].toString() + ":" + e.getStackTrace()[1].toString());
            e.printStackTrace();
        }
        return instance;
    }

    public static Object getInstanceUsingResources(Class c) {
        if (instance != null) {
            return instance;
        }
        try {
            instance = c.newInstance();
            Properties props = ((Kernel) instance).getProperties(c.getSimpleName());
            ((Kernel) instance).loadAdapters(props, fields, adapters);
        } catch (Exception e) {
            e.printStackTrace();
            instance = null;
        }
        return instance;
    }

    private Properties getProperties(String name) {
        Properties props = null;
        try {
            String propsName = name + ".properties";
            InputStream propertyFile = getClass().getClassLoader().getResourceAsStream(propsName);
            System.out.println(propsName);
            props = new Properties();
            props.load(propertyFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

    private void loadAdapters(Properties props, Object[] fields, Class[] adapters) throws Exception {
        setHttpHandlerLoaded(false);
        System.out.println("LOADING ADAPTERS");
        setHost(props.getProperty("http-host"));
        try {
            setPort(Integer.parseInt(props.getProperty("http-port")));
        } catch (Exception e) {
        }
        String adapterInterfaceName = null;
        try {
            for (int i = 0; i < adapters.length; i++) {
                adapterInterfaceName = adapters[i].getSimpleName();
                System.out.println("ADAPTER: " + adapterInterfaceName);
                Class c = Class.forName(props.getProperty(adapterInterfaceName));
                if (adapters[i].isAssignableFrom(c)) {
                    fields[i] = adapters[i].cast(c.newInstance());
                    //if (fields[i] instanceof com.sun.net.httpserver.HttpHandler) {
                    if (fields[i] instanceof com.gskorupa.cricket.in.HttpAdapter) {
                        setHttpHandlerLoaded(true);
                    }
                    java.lang.reflect.Method method = fields[i].getClass().getMethod("loadProperties", Properties.class);
                    method.invoke(fields[i], props);
                    System.out.println("LOADED");
                } else {
                    logger.severe("Adapters initialization error. Configuration for: " + adapterInterfaceName);
                }
            }
        } catch (Exception e) {
            logger.severe("Adapters initialization error. Configuration for: " + adapterInterfaceName);
            throw new Exception(e);
        }

    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the httpd
     */
    public Httpd getHttpd() {
        return httpd;
    }

    /**
     * @param httpd the httpd to set
     */
    public void setHttpd(Httpd httpd) {
        this.httpd = httpd;
    }

    /**
     * @return the httpHandlerLoaded
     */
    public boolean isHttpHandlerLoaded() {
        return httpHandlerLoaded;
    }

    /**
     * @param httpHandlerLoaded the httpHandlerLoaded to set
     */
    public void setHttpHandlerLoaded(boolean httpHandlerLoaded) {
        this.httpHandlerLoaded = httpHandlerLoaded;
    }

    public String getHelp() {
        String content = "Help file not found";
        try{
            content=readHelpFile("/localhelp.txt");
        }catch(Exception e){
            try{
                content=readHelpFile("/help.txt");
            }catch(Exception x){
                e.printStackTrace();
            }
        }
        return content;
    }

    public String readHelpFile(String fileName) throws Exception {
        String content = null;
        BufferedReader reader
                = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(fileName)));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
            out.append("\r\n");
        }
        content = out.toString();
        reader.close();
        return content;
    }
    
    /*
    * This method will be invoked when Kernel is executed without --run option
    */
    public void runOnce(){
        
    }
    
}
