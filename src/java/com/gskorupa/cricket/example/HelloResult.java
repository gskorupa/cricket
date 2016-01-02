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
package com.gskorupa.cricket.example;

import com.gskorupa.cricket.in.Result;

/**
 *
 * @author greg
 */
public class HelloResult implements Result {
    private HelloData data;
    private int code;
    private String message;
    
    public void setCode(int code){
        this.code=code;
    }
    
    public int getCode(){
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
        this.data = (HelloData)data;
    }
    
    public String toString(){
        StringBuilder sb=new StringBuilder();
        if(getCode()>0){
            sb.append("error=");
            sb.append(getCode());
            sb.append("\r\n");
            sb.append(getData().toString());
        }else{
            sb.append(getData().toString());
        }
        sb.append("\r\n");
        return sb.toString();
    }
    
}
