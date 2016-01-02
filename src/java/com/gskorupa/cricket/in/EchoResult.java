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
package com.gskorupa.cricket.in;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author greg
 */
public class EchoResult implements Result {

    private HashMap<String, String> data;
    private int code;
    private String message;

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setMessage(String message) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }
    
    /**
     * @return the data
     */
    public Object getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(Object data) {
        this.data = (HashMap) data;
    }

    public String toJsonString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"code\": ").append(getCode()).append(",\n");
        sb.append("  \"message\": ").append(getMessage()).append(",\n");
        sb.append("  \"data\": {\n");
        
        //generate data content
        Iterator it = data.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            sb.append("    \"").append(pair.getKey()).append("\": ");
            sb.append("\"").append(pair.getValue()).append("\"");
            if(it.hasNext()){
                sb.append(",");
            }
            sb.append("\n");
        }
        //end of data
        sb.append("  }\n");
        
        // finish
        sb.append("}\n");
        return sb.toString();
    }

    public String toXmlString() {
        return null;
    }

    public String toCsvString() {
        return null;
    }

}
