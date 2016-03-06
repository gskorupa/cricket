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
package org.cricketmsf;

import org.cricketmsf.config.AdapterConfiguration;
import org.cricketmsf.config.ConfigSet;
import org.cricketmsf.config.Configuration;
import org.cricketmsf.in.InboundAdapter;
import org.cricketmsf.out.OutboundAdapter;
import java.util.logging.Logger;
import static java.lang.Thread.MIN_PRIORITY;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

/**
 * SimpleService
 *
 * @author greg
 */
public abstract class Kernel {

    // emergency LOGGER
    private static final Logger LOGGER = Logger.getLogger(org.cricketmsf.Kernel.class.getName());

    // singleton
    private static Object instance = null;
    
    private UUID uuid;
    private HashMap<String, String> eventHookMethods =new HashMap();

    // adapters
    public static ArrayList adapters = new ArrayList();
    public static ArrayList adapterClasses = new ArrayList();

    // http server
    private String host = null;
    private int port = 0;
    private Httpd httpd;
    private boolean httpHandlerLoaded = false;

    private static long eventSeed = System.currentTimeMillis();

    protected ConfigSet configSet = null;

    private long startedAt = 0;
    public Kernel() {
    }
    
    void setStartedAt(long time){
        startedAt=time;
    }
    
    public void addHookMethodNameForEvent(String eventCategory, String hookMethodName) {
        eventHookMethods.put(eventCategory, hookMethodName);
    }

    protected void getEventHooks() {
        EventHook ah;
        String eventCategory;
        System.out.println("REGISTERING EVENT HOOKS");
        // for every method of a Kernel instance (our service class extending Kernel)
        for (Method m : this.getClass().getMethods()) {
            ah = (EventHook) m.getAnnotation(EventHook.class);
            // we search for annotated method
            if (ah != null) {
                eventCategory = ah.eventCategory();
                addHookMethodNameForEvent(eventCategory, m.getName());
                System.out.println("hook method for event category " + eventCategory + " : " + m.getName());
            }
        }
        System.out.println("END REGISTERING EVENT HOOKS");
    }
    
    public String getHookMethodNameForEvent(String eventCategory) {
        String result = null;
        result = eventHookMethods.get(eventCategory);
        if (null == result) {
            result = eventHookMethods.get("*");
        }
        return result;
    }
    
    public void handleEvent(Event event){
        try {
            Method m = getClass()
                    .getMethod(getHookMethodNameForEvent(event.getCategory()),Event.class);
            m.invoke(this, event);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
    
    protected synchronized  void registerAdapter(Object adapter, Class adapterClass){
        adapters.add(adapter);
        adapterClasses.add(adapterClass);
    }
    
    protected Object getRegistered(Class interfaceClass){
        int index = adapterClasses.indexOf(interfaceClass);
        return adapters.get(index);
    }
    
    public Configuration getConfiguration(String serviceName) {
        if (configSet == null) {
            configSet = new ConfigSet();
        }

        return configSet.getConfiguration(serviceName);
    }

    public static long getEventId() {
        return eventSeed += 1;
    }

    public abstract void getAdapters();

    public static Kernel getInstance() {
        return (Kernel) instance;
    }

    public static Object getInstanceWithProperties(Class c, Configuration config) {
        if (instance != null) {
            return instance;
        }
        try {
            instance = c.newInstance();
            ((Kernel) instance).setUuid(UUID.randomUUID());
            ((Kernel) instance).loadAdapters(config, adapters, adapterClasses);
        } catch (Exception e) {
            instance = null;
            LOGGER.log(Level.SEVERE, "{0}:{1}", new Object[]{e.getStackTrace()[0].toString(), e.getStackTrace()[1].toString()});
            e.printStackTrace();
        }
        return instance;
    }

    private void loadAdapters(Configuration config, ArrayList adapters, ArrayList adapterClasses) throws Exception {
        setHttpHandlerLoaded(false);
        System.out.println("LOADING SERVICE PROPERTIES FOR " + config.getService());
        System.out.println("UUID: "+getUuid().toString());
        setHost(config.getHost());
        System.out.println("http-host=" + getHost());
        try {
            setPort(Integer.parseInt(config.getPort()));
        } catch (Exception e) {
        }
        System.out.println("http-port=" + getPort());
        System.out.println("LOADING ADAPTERS");
        String adapterInterfaceName = null;
        AdapterConfiguration ac = null;
        try {
            for (int i = 0; i < adapterClasses.size(); i++) {
                adapterInterfaceName = ((Class)adapterClasses.get(i)).getSimpleName();
                ac = config.getAdapterConfiguration(adapterInterfaceName);
                System.out.println("ADAPTER: " + adapterInterfaceName);
                Class c = Class.forName(ac.getClassFullName());
                if (((Class)adapterClasses.get(i)).isAssignableFrom(c)) {
                    adapters.add(i,((Class)adapterClasses.get(i)).cast(c.newInstance()));
                    if (adapters.get(i) instanceof org.cricketmsf.in.http.HttpAdapter) {
                        setHttpHandlerLoaded(true);
                    }
                    // loading properties
                    java.lang.reflect.Method loadPropsMethod = adapters.get(i).getClass().getMethod("loadProperties", HashMap.class);
                    loadPropsMethod.invoke(adapters.get(i), ac.getProperties());
                } else {
                    LOGGER.log(Level.SEVERE, "Adapters initialization error. Adapter class must implement: {0}", adapterInterfaceName);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Adapters initialization error. Configuration for: {0}", adapterInterfaceName);
            throw new Exception(e);
        }
        System.out.println("END LOADING ADAPTERS");
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

    /*
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
*/
    /*
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
*/
    /*
    * This method will be invoked when Kernel is executed without --run option
     */
    public void runOnce() {
        //LOGGER.warning("Method runOnce should be overriden");
        getEventHooks();
        getAdapters();
    }

    public void start() throws InterruptedException {
        getAdapters();
        getEventHooks();
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
            System.out.println("Running initialization tasks");
            runInitTasks();
            long startedIn = System.currentTimeMillis()-startedAt;
            System.out.println("Started in "+startedIn+"ms. Press Ctrl-C to stop");
            while (true) {
                Thread.sleep(200);
            }
        } else {
            System.out.println("Couldn't find any http request hook method. Exiting ...");
            System.exit(MIN_PRIORITY);
        }
    }
    
    protected void runInitTasks(){
        
    }

    public void shutdown() {

        //some cleaning up code could be added here ... if required
        System.out.println("\nShutting down ...");
        //if (isHttpHandlerLoaded()) {
            //getHttpd().server.stop(MIN_PRIORITY);
        //}
        //todo: stop adapters
        for (int i = 0; i < adapters.size(); i++) {
            if (adapters.get(i) instanceof org.cricketmsf.in.InboundAdapter) {
                ((InboundAdapter) adapters.get(i)).destroy();
            } else if (adapters.get(i) instanceof org.cricketmsf.out.OutboundAdapter) {
                ((OutboundAdapter) adapters.get(i)).destroy();
            }
        }
        System.out.println("Kernel stopped");
    }

    /**
     * @return the configSet
     */
    public ConfigSet getConfigSet() {
        return configSet;
    }

    /**
     * @param configSet the configSet to set
     */
    public void setConfigSet(ConfigSet configSet) {
        this.configSet = configSet;
    }

    /**
     * @return the uuid
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
    
}
