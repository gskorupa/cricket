/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
