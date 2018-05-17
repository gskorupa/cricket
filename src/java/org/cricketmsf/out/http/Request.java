/*
 * Copyright 2017 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package org.cricketmsf.out.http;

import java.util.HashMap;

/**
 *
 * @author greg
 */
public class Request {
    public HashMap<String, String> properties;
    public String method;
    public Object data;
    
    public Request(){
        method = "GET";
        properties = new HashMap<>();
        properties.put("User-Agent", "Mozilla/5.0");
        properties.put("Content-Type", "text/html");
        data = null;
    }
    
    public void setProperty(String key, String value){
        properties.put(key, value);
    }
    
    public void setMethod(String method){
        method = method.toUpperCase();
    }
    
    public void setData(Object data){
        this.data = data;
    }
    
}
