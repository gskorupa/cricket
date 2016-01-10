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
