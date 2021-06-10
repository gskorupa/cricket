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

/**
 *
 * @author greg
 */
public class ConfigSet {

    //private String id = "new";
    String description = "This is sample configuration";
    ArrayList<Configuration> services;
    private String kernelVersion;
    private String serviceVersion;
    private boolean builtIn  = true;

    public ConfigSet() {
        services = new ArrayList<>();
    }

    public void addConfiguration(Configuration c) {
        int index = getIndexOf(c.getId());
        if (index > -1) {
            services.set(index, c);
        } else {
            services.add(c);
        }
    }

    public Configuration getDefault() {
        return services.get(0);
    }

    private int getIndexOf(String serviceId) {
        Configuration c;
        for (int i = 0; i < services.size(); i++) {
            c = services.get(i);
            if (c.getId().equalsIgnoreCase(serviceId)) {
                return i;
            }
        }
        return -1;
    }

    public Configuration getConfiguration(String serviceName) {
        Configuration c;
        for (int i = 0; i < services.size(); i++) {
            c = services.get(i);
            if (serviceName.equals(c.getService())) {
                return c;
            }
        }
        return null;
    }

    public Configuration getConfigurationById(String id) {
        if (null == id) {
            return null;
        }
        Configuration c;
        for (int i = 0; i < services.size(); i++) {
            c = services.get(i);
            if (id.equals(c.getId())) {
                return c;
            }
        }
        return null;
    }

    /**
     * @return the kernelVersion
     */
    public String getKernelVersion() {
        return kernelVersion;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    /**
     * @param kernelVersion the kernelVersion to set
     */
    public void setKernelVersion(String kernelVersion) {
        this.kernelVersion = kernelVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public void forceProperty(String definition) {
        // service^property=value^adapter^property=value
        // service^^adapter^property=value
        String serviceId = null;
        String serviceProperty = null;
        String servicePropertyValue = null;
        String adapter = null;
        String adapterProperty = null;
        String adapterPropertyValue = null;
        String[] tmp;

        try {
            if (definition.isEmpty()) {
                return;
            }
        } catch (NullPointerException e) {
            return;
        }

        String[] elements = definition.split("\\^");
        serviceId = elements[0];
        if (elements.length > 1) {
            tmp = elements[1].split("\\=");
            serviceProperty = tmp[0];
            servicePropertyValue = tmp.length > 1 ? tmp[1] : null;
        }
        if (elements.length > 2) {
            adapter = elements[2];
        }
        if (elements.length > 3) {
            tmp = elements[3].split("\\=");
            adapterProperty = tmp[0];
            adapterPropertyValue = tmp.length > 1 ? tmp[1] : null;
        }

        Configuration config = getConfigurationById(serviceId);
        if (servicePropertyValue != null) {
            config.putProperty(serviceProperty, servicePropertyValue);
        }
        if (adapterPropertyValue != null) {
            AdapterConfiguration ac = config.getAdapterConfiguration(adapter);
            ac.putProperty(adapterProperty, adapterPropertyValue);
            config.putAdapterConfiguration(ac);
        }
        addConfiguration(config);
    }

    public void joinProps() {
        for (int i = 0; i < services.size(); i++) {
            services.get(i).joinProps();
        }

    }

    public ArrayList<Configuration> getServices() {
        return services;
    }

    /**
     * @return the builtIn
     */
    public boolean isBuiltIn() {
        return builtIn;
    }

    /**
     * @param builtIn the builtIn to set
     */
    public void setBuiltIn(boolean builtIn) {
        this.builtIn = builtIn;
    }
}
