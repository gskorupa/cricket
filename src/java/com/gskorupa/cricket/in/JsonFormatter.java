/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gskorupa.cricket.in;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class JsonFormatter {

    private static JsonFormatter instance = null;

    public static JsonFormatter getInstance() {
        if (instance != null) {
            return instance;
        } else {
            instance = new JsonFormatter();
            return instance;
        }
    }

    public String format(boolean prettyPrint, Object o) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson;
        if (prettyPrint) {
            gson = gsonBuilder.setPrettyPrinting().create();
        } else {
            gson = gsonBuilder.create();
        }
        return gson.toJson(o);
    }
    
}
