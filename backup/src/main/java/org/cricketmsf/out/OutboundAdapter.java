/*
 * Copyright 2015 Grzegorz Skorupa .
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
package org.cricketmsf.out;

import org.cricketmsf.out.dispatcher.DispatcherIface;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Grzegorz Skorupa 
 */
public class OutboundAdapter implements OutboundAdapterIface {

    protected HashMap<String, Object> statusMap = null;
    public HashMap<String, String> properties;
    protected String name;

    public OutboundAdapter() {
    }

    public void destroy() {
    }

    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        this.name = adapterName;
        this.properties = (HashMap<String, String>) properties.clone();
        getStatus(adapterName); //required if we need to overwrite updateStatusItem() method
    }

    @Override
    public Map<String, Object> getStatus(String name) {
        if (statusMap == null) {
            statusMap = new HashMap();
            statusMap.put("name", name);
            statusMap.put("class", getClass().getName());
            statusMap.put("properties", properties);
        }
        return statusMap;
    }

    public void updateStatusItem(String key, String value) {
        statusMap.put(key, value);
    }

    @Override
    public String getProperty(String name) {
        return properties.get(name);
    }
    
    @Override
    public String setProperty(String name, String value){
        return properties.put(name, value);
    }

    public DispatcherIface getDispatcher() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }
}
