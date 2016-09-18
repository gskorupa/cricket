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

import com.sun.net.httpserver.Filter;
import org.cricketmsf.config.AdapterConfiguration;
import org.cricketmsf.config.ConfigSet;
import org.cricketmsf.config.Configuration;
import org.cricketmsf.in.InboundAdapter;
import org.cricketmsf.out.OutboundAdapter;
import java.util.logging.Logger;
import static java.lang.Thread.MIN_PRIORITY;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.logging.Level;
import org.cricketmsf.config.HttpHeader;

/**
 * Microkernel.
 *
 * @author Grzegorz Skorupa
 */
public abstract class Kernel {

    private static final String VERSION = "1.0.0";

    // emergency LOGGER
    private static final Logger LOGGER = Logger.getLogger(org.cricketmsf.Kernel.class.getName());

    // singleton
    private static Object instance = null;

    private UUID uuid;
    private HashMap<String, String> eventHookMethods = new HashMap<>();
    private String id;

    // adapters
    public HashMap<String, Object> adaptersMap = new HashMap<>();

    // user defined properties
    public HashMap<String, String> properties = new HashMap<>();

    public SimpleDateFormat dateFormat = null;

    // http server
    private String host = null;
    private int port = 0;
    private Httpd httpd;
    private boolean httpHandlerLoaded = false;
    private boolean inboundAdaptersLoaded = false;

    private static long eventSeed = System.currentTimeMillis();

    protected ConfigSet configSet = null;

    private Filter securityFilter = new SecurityFilter();
    private ArrayList corsHeaders;

    private long startedAt = 0;

    public Kernel() {
    }

    void setStartedAt(long time) {
        startedAt = time;
    }

    private void addHookMethodNameForEvent(String eventCategory, String hookMethodName) {
        eventHookMethods.put(eventCategory, hookMethodName);
    }

    private void getEventHooks() {
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

    private String getHookMethodNameForEvent(String eventCategory) {
        String result = null;
        result = eventHookMethods.get(eventCategory);
        if (null == result) {
            result = eventHookMethods.get("*");
        }
        return result;
    }

    /**
     * Invokes the service method annotated as dedicated to this event category
     *
     * @param event event object that should be processed
     */
    public void handleEvent(Event event) {
        try {
            Method m = getClass()
                    .getMethod(getHookMethodNameForEvent(event.getCategory()), Event.class);
            m.invoke(this, event);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * Invokes the service method annotated as dedicated to this event category
     *
     * @param event event object that should be processed
     */
    public static void handle(Event event) {
        Kernel.getInstance().handleEvent(event);
    }

    public HashMap<String, Object> getAdaptersMap() {
        return adaptersMap;
    }

    protected Object getRegistered(String adapterName) {
        return adaptersMap.get(adapterName);
    }

    /**
     * Returns next unique identifier for Event.
     *
     * @return next unique identifier
     */
    public static long getEventId() {
        return eventSeed += 1;
    }

    /**
     * Must be used to set adapter variables after instantiating them according
     * to the configuration in cricket.json file. Look at EchoService example.
     */
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
            ((Kernel) instance).setId(config.getId());
            ((Kernel) instance).setProperties(config.getProperties());
            ((Kernel) instance).configureTimeFormat();
            ((Kernel) instance).loadAdapters(config);
        } catch (Exception e) {
            instance = null;
            LOGGER.log(Level.SEVERE, "{0}:{1}", new Object[]{e.getStackTrace()[0].toString(), e.getStackTrace()[1].toString()});
            e.printStackTrace();
        }
        return instance;
    }

    private void configureTimeFormat() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        dateFormat.setTimeZone(TimeZone.getTimeZone(getProperties().getOrDefault("time-zone", "GMT")));
    }

    private void printHeader(String version) {
        System.out.println();
        System.out.println("   __|   \\  |  __|");
        System.out.println("  (     |\\/ |  _|   Cricket Microservices Framework");
        System.out.println(" \\___| _|  _| _|    version " + version);
        System.out.println();
        // big text generated using http://patorjk.com/software/taag
    }

    /**
     * Instantiates adapters following configuration in cricket.json
     *
     * @param config Configutation object loaded from cricket.json
     * @throws Exception
     */
    private synchronized void loadAdapters(Configuration config) throws Exception {
        printHeader(VERSION);
        setHttpHandlerLoaded(false);
        setInboundAdaptersLoaded(false);
        System.out.println("LOADING SERVICE PROPERTIES FOR " + config.getService());
        System.out.println("UUID: " + getUuid().toString());
        setHost(config.getHost());
        System.out.println("http-host=" + getHost());
        setSecurityFilter(config.getFilter());
        System.out.println("filter=" + config.getFilter());
        setCorsHeaders(config.getCors());
        System.out.println("CORS=" + config.getCors());
        try {
            setPort(Integer.parseInt(config.getPort()));
        } catch (Exception e) {
        }
        System.out.println("http-port=" + getPort());
        System.out.println("Extended properties: "+getProperties().toString());
        System.out.println("LOADING ADAPTERS");
        String adapterName = null;
        AdapterConfiguration ac = null;
        try {
            HashMap<String, AdapterConfiguration> adcm = config.getAdapters();
            for (Map.Entry<String, AdapterConfiguration> adapterEntry : adcm.entrySet()) {
                adapterName = adapterEntry.getKey();
                ac = adapterEntry.getValue();
                System.out.println("ADAPTER: " + adapterName);
                try {
                    Class c = Class.forName(ac.getClassFullName());
                    adaptersMap.put(adapterName, c.newInstance());
                    if (adaptersMap.get(adapterName) instanceof org.cricketmsf.in.http.HttpAdapter) {
                        setHttpHandlerLoaded(true);
                    } else if (adaptersMap.get(adapterName) instanceof org.cricketmsf.in.InboundAdapter) {
                        setInboundAdaptersLoaded(true);
                    }
                    // loading properties
                    java.lang.reflect.Method loadPropsMethod = c.getMethod("loadProperties", HashMap.class, String.class);
                    loadPropsMethod.invoke(adaptersMap.get(adapterName), ac.getProperties(), adapterName);
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
                    adaptersMap.put(adapterName, null);
                    System.out.println("ERROR: " + adapterName + "configuration: " + ex.getClass().getSimpleName());
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Adapters initialization error. Configuration for: {0}", adapterName);
            throw new Exception(e);
        }
        System.out.println("END LOADING ADAPTERS");
        System.out.println();
    }

    private void setSecurityFilter(String filterName) {
        try {
            Class c = Class.forName(filterName);
            securityFilter = (SecurityFilter) c.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            securityFilter = new SecurityFilter();
        }
    }

    public Filter getSecurityFilter() {
        return securityFilter;
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
    private Httpd getHttpd() {
        return httpd;
    }

    /**
     * @param httpd the httpd to set
     */
    private void setHttpd(Httpd httpd) {
        this.httpd = httpd;
    }

    /**
     * @return the httpHandlerLoaded
     */
    private boolean isHttpHandlerLoaded() {
        return httpHandlerLoaded;
    }

    /**
     * @param httpHandlerLoaded the httpHandlerLoaded to set
     */
    private void setHttpHandlerLoaded(boolean httpHandlerLoaded) {
        this.httpHandlerLoaded = httpHandlerLoaded;
    }

    /**
     * This method will be invoked when Kernel is executed without --run option
     */
    public void runOnce() {
        getEventHooks();
        getAdapters();
    }

    /**
     * Starts the service instance
     *
     * @throws InterruptedException
     */
    public void start() throws InterruptedException {
        getAdapters();
        getEventHooks();
        if (isHttpHandlerLoaded() || isInboundAdaptersLoaded()) {

            Runtime.getRuntime().addShutdownHook(
                    new Thread() {
                public void run() {
                    try {
                        shutdown();
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            System.out.println("Running initialization tasks");
            runInitTasks();

            System.out.println("Starting listeners ...");
            // run listeners for inbound adapters
            runListeners();

            System.out.println("Starting http listener ...");
            setHttpd(new Httpd(this));
            getHttpd().run();

            long startedIn = System.currentTimeMillis() - startedAt;
            printHeader(VERSION);
            System.out.println("# Service " + getId() + " is running");
            System.out.println("#");
            System.out.println("# HTTP listening on port " + getPort());
            System.out.println("#");
            System.out.println("# Started in " + startedIn + "ms. Press Ctrl-C to stop");
            System.out.println();
            runFinalTasks();
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        } else {
            System.out.println("Couldn't find any http request hook method. Exiting ...");
            System.exit(MIN_PRIORITY);
        }
    }

    /**
     * Could be overriden in a service implementation to run required code at
     * the service start. As the last step of the service starting procedure before
     * HTTP service.
     */
    protected void runInitTasks() {
    }
    
    /**
     * Could be overriden in a service implementation to run required code at
     * the service start. As the last step of the service starting procedure after 
     * HTTP service.
     */
    protected void runFinalTasks() {
        
    }

    protected void runListeners() {
        for (Map.Entry<String, Object> adapterEntry : getAdaptersMap().entrySet()) {
            if (adapterEntry.getValue() instanceof org.cricketmsf.in.InboundAdapter) {
                if (! (adapterEntry.getValue() instanceof org.cricketmsf.in.http.HttpAdapter)) {
                    (new Thread((InboundAdapter) adapterEntry.getValue())).start();
                    System.out.println(adapterEntry.getValue().getClass().getSimpleName());
                }
            }
        }
    }

    public void shutdown() {

        System.out.println("\nShutting down ...");
        for (Map.Entry<String, Object> adapterEntry : getAdaptersMap().entrySet()) {
            if (adapterEntry.getValue() instanceof org.cricketmsf.in.InboundAdapter) {
                ((InboundAdapter) adapterEntry.getValue()).destroy();
            } else if (adapterEntry.getValue() instanceof org.cricketmsf.out.OutboundAdapter) {
                ((OutboundAdapter) adapterEntry.getValue()).destroy();
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
     * Return service instance unique identifier
     *
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

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    private void setId(String id) {
        this.id = id;
    }

    /**
     * @return the corsHeaders
     */
    public ArrayList getCorsHeaders() {
        return corsHeaders;
    }

    /**
     * @param corsHeaders the corsHeaders to set
     */
    public void setCorsHeaders(ArrayList corsHeaders) {
        //this.corsHeaders = corsHeaders;
        if (corsHeaders != null) {
            String header;
            this.corsHeaders = new ArrayList<HttpHeader>();
            for (int i = 0; i < corsHeaders.size(); i++) {
                header = (String) corsHeaders.get(i);
                try {
                    this.corsHeaders.add(
                            new HttpHeader(
                                    header.substring(0, header.indexOf(":")).trim(),
                                    header.substring(header.indexOf(":") + 1).trim()
                            )
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            this.corsHeaders = null;
        }
    }

    /**
     * @return the properties
     */
    public HashMap<String, String> getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }

    /**
     * @return the inboundAdaptersLoaded
     */
    public boolean isInboundAdaptersLoaded() {
        return inboundAdaptersLoaded;
    }

    /**
     * @param inboundAdaptersLoaded the inboundAdaptersLoaded to set
     */
    public void setInboundAdaptersLoaded(boolean inboundAdaptersLoaded) {
        this.inboundAdaptersLoaded = inboundAdaptersLoaded;
    }

}
