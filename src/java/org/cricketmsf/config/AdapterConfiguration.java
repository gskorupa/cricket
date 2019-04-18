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
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author greg
 */
public class AdapterConfiguration {
    
    private String name;
    private String interfaceName;
    private String classFullName;
    private HashMap<String,String> properties;
    
    public AdapterConfiguration(){
        properties= new HashMap<>();
    }
    
    public String getProperty(String name){
        return properties.get(name);
    }
    
    public void putProperty(String name, String value){
        properties.put(name, value);
    }

    /**
     * @return the interfaceName
     */
    public String getInterfaceName() {
        return interfaceName;
    }

    /**
     * @param interfaceName the interfaceName to set
     */
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    /**
     * @return the classFullName
     */
    public String getClassFullName() {
        return classFullName;
    }

    /**
     * @param classFullName the classFullName to set
     */
    public void setClassFullName(String classFullName) {
        this.classFullName = classFullName;
    }

    /**
     * @return the properties
     */
    public HashMap<String,String> getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(HashMap<String,String> properties) {
        this.properties = properties;
    }

    /**
     * @return the name
     */
    public String getName() {
        if(name==null||name.isEmpty()){
            return getInterfaceName();
        }else{
            return name;
        }
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    public void joinProps(){
        HashMap<String,String> newProperties = new HashMap<>();
        Iterator<Map.Entry<String, String>> it = properties.entrySet().iterator();
        String key;
        String value;
        String tmpValue;
        String tmpKey;
        int i;
        while (it.hasNext()) {
            Map.Entry<String, String> pair = it.next();
            tmpKey=pair.getKey();
            if(tmpKey.endsWith(".0")){
                value = properties.get(tmpKey);
                key = tmpKey.substring(0,tmpKey.length()-2);
                i=1;
                tmpValue=properties.get(key+"."+i);
                while(tmpValue != null){
                    value=value.concat(tmpValue);
                    i++;
                    tmpValue=properties.get(key+"."+i);
                }
                newProperties.put(key, value);
            }else if(tmpKey.indexOf(".")>0){
                // do nothing
            }else if(tmpKey.indexOf(".")==0){
                // do nothing - property name starting from "."
            }else{
                newProperties.put(tmpKey, properties.get(tmpKey));
            }
        }
        properties = newProperties;
    }
}
