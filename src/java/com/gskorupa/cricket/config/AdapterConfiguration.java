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
package com.gskorupa.cricket.config;

import java.util.HashMap;

/**
 *
 * @author greg
 */
public class AdapterConfiguration {
    
    private String interfaceName;
    private String classFullName;
    private HashMap<String,String> properties;
    
    public AdapterConfiguration(){
        properties= new <String,String>HashMap();
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
    
}
