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
package org.cricketmsf.in.http;

import com.sun.net.httpserver.Headers;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author greg
 */
@XmlRootElement(name = "result")
@XmlAccessorType (XmlAccessType.FIELD)
public class StandardResult implements Result {

    private Object data = null;
    
    private int code;
    private String message = null;
    private byte[] payload = {};
    private String fileExtension = null;
    private Date modificationDate;
    private String modificationDateFormatted;
    private int maxAge;
    private Headers headers;
    private long responseTime = 0;

    public StandardResult() {
        setCode(HttpAdapter.SC_OK);
        setModificationDate(new Date());
        maxAge = 0;
        headers = new Headers();
    }

    public StandardResult(Object data) {
        setCode(HttpAdapter.SC_OK);
        setData(data);
        setModificationDate(new Date());
        maxAge = 0;
        headers = new Headers();
    }

    /**
     * @return the status code
     */
    public int getCode() {
        return code;
    }

    /**
     * @param code the status code to set
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * @return the status message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
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
        this.data = data;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }
    
    public void buildPayload(String payload) {
        this.payload = payload.getBytes();
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public void setModificationDate(Date date) {
        modificationDate = date;
        SimpleDateFormat dt1 = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
        modificationDateFormatted = dt1.format(modificationDate);

    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public String getModificationDateFormatted() {
        return modificationDateFormatted;
    }
    
    @Override
    public int getMaxAge(){
        return maxAge;
    }
    
    @Override
    public void setMaxAge(int maxAge){
        this.maxAge = maxAge;
    }
    
    @Override
    public void setHeader(String name, String value){
        headers.add(name, value);
    }
    
    @Override
    public void setHeader(String name, List values){
        headers.put(name, (ArrayList)values);
    }
    
    @Override
    public Headers getHeaders(){
        return headers;
    }

    @Override
    public void setResponseTime(long time) {
        this.responseTime=time;
    }

    @Override
    public long getResponseTime() {
        return responseTime;
    }
}
