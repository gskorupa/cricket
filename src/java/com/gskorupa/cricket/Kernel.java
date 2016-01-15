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

import com.gskorupa.cricket.in.InboundAdapter;
import com.gskorupa.cricket.out.OutboundAdapter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Thread.MIN_PRIORITY;
import java.util.logging.Level;

/**
 * SimpleService
 *
 * @author greg
 */
public abstract class Kernel {

    // emergency LOGGER
    private static final Logger LOGGER = Logger.getLogger(com.gskorupa.cricket.Kernel.class.getName());

    // singleton
    private static Object instance = null;

    // adapters
    public static Object[] adapters = {};
    public static Class[] adapterClasses = {};

    // http server
    private String host = null;
    private int port = 0;
    private Httpd httpd;
    private boolean httpHandlerLoaded = false;
    
    private static long eventSeed = System.currentTimeMillis();

    public Kernel() {
    }
    
    public static long getEventId(){
        return eventSeed +=1;
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
        Properties props;
        try {
            InputStream propertyFile = new FileInputStream(new File(path));
            props = new Properties();
            props.load(propertyFile);
            return getInstanceWithProperties(c, props);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Adapters initialization error. Configuration: {0}", path);
        }
        return null;
    }

    public static Object getInstanceWithProperties(Class c, Properties props) {
        if (instance != null) {
            return instance;
        }
        try {
            instance = c.getClass().newInstance();
            ((Kernel) instance).loadAdapters(props, adapters, adapterClasses);
        } catch (Exception e) {
            instance = null;
            LOGGER.log(Level.SEVERE, "{0}:{1}", new Object[]{e.getStackTrace()[0].toString(), e.getStackTrace()[1].toString()});
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
            ((Kernel) instance).loadAdapters(props, adapters, adapterClasses);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "{0}:{1}", new Object[]{e.getStackTrace()[0].toString(), e.getStackTrace()[1].toString()});
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
            LOGGER.log(Level.SEVERE, "{0}:{1}", new Object[]{e.getStackTrace()[0].toString(), e.getStackTrace()[1].toString()});
            e.printStackTrace();
        }
        return props;
    }

    private void loadAdapters(Properties props, Object[] adapters, Class[] adapterClasses) throws Exception {
        setHttpHandlerLoaded(false);
        System.out.println("LOADING SERVICE PROPERTIES");
        setHost(props.getProperty("http-host"));
        System.out.println("http-host=" + getHost());
        try {
            setPort(Integer.parseInt(props.getProperty("http-port")));
        } catch (Exception e) {
        }
        System.out.println("http-port=" + getPort());
        System.out.println("LOADING ADAPTERS");
        String adapterInterfaceName = null;
        try {
            for (int i = 0; i < adapterClasses.length; i++) {
                adapterInterfaceName = adapterClasses[i].getSimpleName();
                System.out.println("ADAPTER: " + adapterInterfaceName);
                Class c = Class.forName(props.getProperty(adapterInterfaceName));
                if (adapterClasses[i].isAssignableFrom(c)) {
                    adapters[i] = adapterClasses[i].cast(c.newInstance());
                    if (adapters[i] instanceof com.gskorupa.cricket.in.HttpAdapter) {
                        setHttpHandlerLoaded(true);
                    }
                    // loading properties
                    java.lang.reflect.Method loadPropsMethod = adapters[i].getClass().getMethod("loadProperties", Properties.class);
                    loadPropsMethod.invoke(adapters[i], props);
                } else {
                    LOGGER.log(Level.SEVERE, "Adapters initialization error. Configuration for: {0}", adapterInterfaceName);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Adapters initialization error. Configuration for: {0}", adapterInterfaceName);
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
        try {
            content = readHelpFile("/localhelp.txt");
        } catch (Exception e) {
            try {
                content = readHelpFile("/help.txt");
            } catch (Exception x) {
                LOGGER.log(Level.SEVERE, "{0}:{1}", new Object[]{x.getStackTrace()[0].toString(), x.getStackTrace()[1].toString()});
                e.printStackTrace();
            }
        }
        return content;
    }

    public String readHelpFile(String fileName) throws Exception {
        String content = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(fileName)))) {
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
                out.append("\r\n");
            }
            content = out.toString();
        }
        return content;
    }

    /*
    * This method will be invoked when Kernel is executed without --run option
     */
    public void runOnce() {
        //LOGGER.warning("Method runOnce should be overriden");
        getAdapters();
    }

    public void start() throws InterruptedException {
        getAdapters();
        if (isHttpHandlerLoaded()) {
            System.out.println("Starting http listener ...");
            Runtime.getRuntime().addShutdownHook(
                    new Thread() {
                public void run() {
                    try {
                        Thread.sleep(200);
                        shutdown();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            setHttpd(new Httpd(this));
            getHttpd().run();
            System.out.println("Started. Press Ctrl-C to stop");
            while (true) {
                Thread.sleep(200);
            }
        } else {
            System.out.println("Couldn't find any http request hook method. Exiting ...");
            System.exit(MIN_PRIORITY);
        }
    }

    public void shutdown() {
        //some cleaning up code could be added here ... if required
        System.out.println("\nShutting down ...");
        getHttpd().server.stop(MIN_PRIORITY);
        //todo: stop adapters
        for(int i=0; i<adapters.length; i++){
            if (adapters[i] instanceof com.gskorupa.cricket.in.InboundAdapter) {
                ((InboundAdapter)adapters[i]).destroy();
            }else if (adapters[i] instanceof com.gskorupa.cricket.out.OutboundAdapter) {
                ((OutboundAdapter)adapters[i]).destroy();
            }
        }
        System.out.println("Kernel stopped");
    }

}
