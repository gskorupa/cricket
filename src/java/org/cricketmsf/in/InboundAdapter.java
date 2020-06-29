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
package org.cricketmsf.in;

import java.util.HashMap;
import java.util.Map;
import org.cricketmsf.event.EventMaster;
import org.cricketmsf.exception.EventException;
import org.cricketmsf.out.dispatcher.DispatcherIface;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class InboundAdapter implements Runnable, InboundAdapterIface {

    protected HashMap<String, String> hookMethodNames;
    public HashMap<String, String> properties = null;
    protected HashMap<String, String> statusMap = null;
    protected String name;

    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        this.name = adapterName;
        if(properties == null){
            this.properties = new HashMap<>();
        }else{
            this.properties = (HashMap<String, String>) properties.clone();
        }
        getStatus(adapterName); //required if we need to overwrite updateStatusItem() method
    }

    @Override
    public String getProperty(String name) {
        return properties.get(name);
    }
    
    @Override
    public String setProperty(String name, String value){
        return properties.put(name, value);
    }

    public InboundAdapter() {
        hookMethodNames = new HashMap<>();
    }

    public void destroy() {
    }

    @Override
    public void run() {
    }
    
    @Override
    public Object handleInput(Object input){
        return null;
    }

    protected Result handle(String method, String payload) {
        return null;
    }

    protected void getServiceHooks(String adapterName) {
    }

    public void addHookMethodNameForMethod(String requestMethod, String hookMethodName) {
        //hookMethodNames.put(requestMethod, hookMethodName);
    }

    public String getHookMethodNameForMethod(String requestMethod) {
        return null;
    }

    @Override
    public Map<String, String> getStatus(String name) {
        if (statusMap == null) {
            statusMap = new HashMap();
            statusMap.put("name", name);
            statusMap.put("class", getClass().getName());
        }
        return statusMap;
    }

    public void updateStatusItem(String key, String value) {
        statusMap.put(key, value);
    }

    public DispatcherIface getDispatcher() {
        return null;
    }

    public String getName() {
        return name;
    }

    public void registerEventCategory(String category, String eventClassName) {
        try {
            String[] categories = {category};
            EventMaster.registerEventCategories(categories, eventClassName);
        } catch (EventException ex) {
            ex.printStackTrace();
        }
    }
}
