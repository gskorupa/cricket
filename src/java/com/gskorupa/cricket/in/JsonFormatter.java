/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gskorupa.cricket.in;

import com.cedarsoftware.util.io.JsonWriter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class JsonFormatter {

    private static JsonFormatter instance = null;
    private Map args;
    
    public JsonFormatter(){
        args = new HashMap();
    }

    public static JsonFormatter getInstance() {
        if (instance != null) {
            return instance;
        } else {
            instance = new JsonFormatter();
            return instance;
        }
    }

    public String format(boolean prettyPrint, Object o) {
        args.clear();
        args.put(JsonWriter.PRETTY_PRINT, prettyPrint);
        return JsonWriter.objectToJson(o, args)+"\n";
    }

    /*
    public String format(boolean prettyPrint, Object o) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson;
        if (prettyPrint) {
            gson = gsonBuilder.setPrettyPrinting().create();
        } else {
            gson = gsonBuilder.create();
        }
        return gson.toJson(o)+"\n";
    }
     */
}
