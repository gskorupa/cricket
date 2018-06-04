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
    public String url;
    public String query;
    public String method;
    public Object data;
    
    public Request(){
        method = "GET";
        properties = new HashMap<>();
        properties.put("User-Agent", "Mozilla/5.0");
        properties.put("Content-Type", "text/html");
        data = null;
        url=null;
        query=null;
    }
    
    public Request setProperty(String key, String value){
        properties.put(key, value);
        return this;
    }
    
    public Request setMethod(String method){
        this.method = method.toUpperCase();
        return this;
    }
    
    public Request setData(Object data){
        this.data = data;
        return this;
    }
    
    public Request setUrl(String url){
        this.url=url;
        return this;
    }
    
    public Request setQuery(String query){
        this.query=query;
        return this;
    }
    
    public String getUrl(){
        if(query!=null&&!query.isEmpty()){
            /*if(!url.endsWith("/")){
                url=url.concat("/");
            }
            if(query.startsWith("/")){
                query=query.substring(1);
            }*/
            if(!query.startsWith("?")){
                query="?"+query;
            }
            return url.concat(query);
        }else{
            return url;
        }
    }
    
}
