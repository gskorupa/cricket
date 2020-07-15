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
package org.cricketmsf.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author greg
 */
public class Configuration {

    private String id;
    private String service;
    private String host;
    private String port;
    private int threads;
    private String filter;
    //private ArrayList<HttpHeader> cors;
    private HashMap<String, Object> properties;
    private HashMap<String, AdapterConfiguration> adapters;
    private AdapterConfiguration[] ports;

    public Configuration() {
        adapters = new HashMap<>();
        properties = new HashMap<>();
        ports = null;
    }

    public Configuration join(Configuration overwritten) {
        //simply replace
        if (null == overwritten) {
            System.out.println("NULL DEFAULT CONFIG");
            return this;
        }
        //overwrite properties
        properties.forEach((k, v) -> {
            overwritten.properties.put(k, v);
        });
        //overwrite adapters
        adapters.forEach((k, v) -> {
            overwritten.adapters.put(k, v);
        });

        //overwrite ports
        AdapterConfiguration ac;
        int found;
        ArrayList<AdapterConfiguration> al = new ArrayList<>();
        for (int i = 0; i < overwritten.ports.length; i++) {
            al.add(overwritten.ports[i]);
        }
        for (int j = 0; j < ports.length; j++) {
            found = -1;
            for (int i = 0; i < al.size(); i++) {
                if (ports[j].getName().equals(al.get(i).getName())) {
                    found = i;
                }
            }
            if (found<0) {
                al.add(ports[j]);
            }else{
                al.set(found, ports[j]);
            }
        }
        overwritten.ports = al.toArray(new AdapterConfiguration[al.size()]);
        return overwritten;
    }

    public AdapterConfiguration getAdapterConfiguration(String name) {
        if (null == ports || ports.length == 0) {
            return adapters.get(name);
        } else {
            for (int i = 0; i < ports.length; i++) {
                if (name.equals(ports[i].getName())) {
                    return ports[i];
                }
            }
            return null;
        }
    }

    public void putAdapterConfiguration(AdapterConfiguration config) {
        if (null == ports || ports.length == 0) {
            adapters.put(config.getName(), config);
        } else {
            boolean found = false;
            for (int i = 0; i < ports.length; i++) {
                if (config.getName().equals(ports[i].getName())) {
                    ports[i] = config;
                    found = true;
                    break;
                }
            }
            if (!found) {
                ports = Arrays.copyOf(ports, ports.length + 1);
                ports[ports.length - 1] = config;
            }
        }
    }

    public String getProperty(String name) {
        return (String) properties.get(name);
    }

    public String getProperty(String name, String defaultValue) {
        return (String) properties.getOrDefault(name, defaultValue);
    }

    public void putProperty(String name, Object value) {
        properties.put(name, value);
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
    public String getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * @return the threads
     */
    public int getThreads() {
        return threads;
    }

    /**
     * @param threads the threads to set
     */
    public void setThreads(int threads) {
        this.threads = threads;
    }

    public HashMap getAdapters() {
        if (null != ports && ports.length > 0) {
            adapters = new HashMap<>();
            for (int i = 0; i < ports.length; i++) {
                adapters.put(ports[i].getName(), ports[i]);
            }
        }
        return adapters;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    /**
     * @return the securityFilter
     */
    public String getFilter() {
        return filter;
    }

    /**
     * @param filterName the securityFilter to set
     */
    public void setFilter(String filterName) {
        this.filter = filterName;
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
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the properties
     */
    public HashMap<String, Object> getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(HashMap<String, Object> properties) {
        this.properties = properties;
    }

    public void joinProps() {
        if (null == ports || ports.length == 0) {
            adapters.forEach((k, v) -> v.joinProps());
        } else {
            for (int i = 0; i < ports.length; i++) {
                ports[i].joinProps();
            }
        }
    }

}
