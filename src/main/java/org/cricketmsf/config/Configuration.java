/*
 * Copyright 2016 Grzegorz Skorupa .
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
import java.util.Iterator;

/**
 *
 * @author greg
 */
public class Configuration {

    private String id;
    private String description;
    private String service;
    //private String host;
    //private String port;
    //private int threads;
    //private String filter;
    private HashMap<String, Object> properties;
    private HashMap<String, AdapterConfiguration> adapters;
    private AdapterConfiguration[] ports;

    public Configuration() {
        //adapters = new HashMap<>();
        adapters = null;
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
        /*
        adapters.forEach((k, v) -> {
            overwritten.adapters.put(k, v);
        });
         */
        //overwrite ports
        int found;
        ArrayList<AdapterConfiguration> al = new ArrayList<>();
        al.addAll(Arrays.asList(overwritten.ports));
        String active;
        for (AdapterConfiguration adapterConfig : ports) {
            active = adapterConfig.getActive();
            if (null != active && (active.equalsIgnoreCase("false") || active.equalsIgnoreCase("no"))) {
                //System.out.println("NOT ACTIVE " + adapterConfig.getName());
                continue;
            }
            found = -1;
            for (int i = 0; i < al.size(); i++) {
                if (adapterConfig.getName().equals(al.get(i).getName())) {
                    found = i;
                }
            }
            if (found < 0) {
                al.add(adapterConfig);
            } else {
                al.set(found, adapterConfig);
            }
        }
        overwritten.ports = al.toArray(new AdapterConfiguration[al.size()]);
        return overwritten;
    }

    public AdapterConfiguration getAdapterConfiguration(String name) {
        for (int i = 0; i < ports.length; i++) {
            if (name.equals(ports[i].getName())) {
                return ports[i];
            }
        }
        return null;
    }

    public void putAdapterConfiguration(AdapterConfiguration config) {
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

    public String getProperty(String name) {
        return (String) properties.get(name);
    }

    public String getProperty(String name, String defaultValue) {
        return (String) properties.getOrDefault(name, defaultValue);
    }

    public void putProperty(String name, Object value) {
        properties.put(name, value);
    }

    public HashMap getAdapters() {
        if (null == adapters) {
            adapters = new HashMap<>();
            String active;
            for (AdapterConfiguration adapterConfig : ports) {
                adapters.put(adapterConfig.getName(), adapterConfig);
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
        Iterator it = properties.keySet().iterator();
        String key, value;
        while (it.hasNext()) {
            key = (String) it.next();
            value = "" + properties.get(key);
            if (value.startsWith("$")) {
                String tmp = System.getenv(value.substring(1));
                if (null != tmp) {
                    properties.put(key, tmp);
                }
            }
        }
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(HashMap<String, Object> properties) {
        this.properties = properties;
    }

    public void joinProps() {
        for (AdapterConfiguration port1 : ports) {
            port1.joinPropsAndResolveEnvVar();
        }
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

}
