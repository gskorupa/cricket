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

import java.util.HashMap;

/**
 *
 * @author greg
 */
public class Configuration {
    
    private String service;
    private String host;
    private String port;
    private int threads;
    private String filter;
    
    private HashMap<String, AdapterConfiguration> adapters;
    
    public Configuration(){
        adapters=new <String, AdapterConfiguration>HashMap();
    }

    public AdapterConfiguration getAdapterConfiguration(String name){
        return adapters.get(name);
    }
    
    public void putAdapterConfiguration(AdapterConfiguration config){
        adapters.put(config.getName(), config);
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

    /**
     * @return the adapters
     */
    public HashMap getAdapters() {
        return adapters;
    }

    /**
     * @param adapters the adapters to set
     */
    public void setAdapters(HashMap adapters) {
        this.adapters = adapters;
    }

    /**
     * @return the service
     */
    public String getService() {
        return service;
    }

    /**
     * @param service the service to set
     */
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
     * @param securityFilter the securityFilter to set
     */
    public void setFilter(String filterName) {
        this.filter = filterName;
    }
    
    
}
