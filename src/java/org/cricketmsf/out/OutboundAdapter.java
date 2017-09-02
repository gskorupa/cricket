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
package org.cricketmsf.out;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class OutboundAdapter {
    
    protected HashMap<String,String> statusMap=null;
    private HashMap<String, String> properties;
    
    public OutboundAdapter(){
    }
    
    public void destroy(){   
    }
    
    public void loadProperties(HashMap<String,String> properties, String adapterName){
        this.properties = (HashMap<String,String>)properties.clone();        
        getStatus(adapterName); //required if we need to overwrite updateStatusItem() method
    }
    
    public Map<String,String> getStatus(String name){
        if(statusMap==null){
            statusMap = new HashMap();
            statusMap.put("name", name);
            statusMap.put("class", getClass().getName());
        }
        return statusMap;
    }
    
    public void updateStatusItem(String key, String value){
        statusMap.put(key, value);
    }
    
    public String getProperty(String name){
        return properties.get(name);
    }

}
