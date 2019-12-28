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
package org.cricketmsf.in.http;

import com.sun.net.httpserver.Headers;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author greg
 */
public class FileResult implements Result {

    private Object data;
    private String message;
    private int code;
    private byte[] payload;
    private String fileExt;
    private Date modificationDate;
    private String modificationDateFormatted;
    private int maxAge = 0;
    private Headers headers;
    private long responseTime = 0;

    public FileResult() {
        headers = new Headers();
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

    /**
     * @return the message
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
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * @return the payload
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * @param payload the payload to set
     */
    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    /**
     * @return the fileExt
     */
    public String getFileExtension() {
        return fileExt;
    }

    /**
     * @param fileExt the fileExt to set
     */
    public void setFileExtension(String fileExt) {
        this.fileExt = fileExt;
    }

    public void setModificationDate(Date date) {
        modificationDate = date;
        SimpleDateFormat dt1 = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z",Locale.ENGLISH);
        modificationDateFormatted = dt1.format(modificationDate);

    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public String getModificationDateFormatted() {
        return modificationDateFormatted;
    }

    @Override
    public int getMaxAge() {
        return maxAge;
    }

    @Override
    public Headers getHeaders() {
        return headers;
    }

    @Override
    public void setHeader(String name, String value) {
        headers.add(name, value);
    }

    @Override
    public void setHeader(String name, List values) {
        headers.put(name, values);
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    @Override
    public void setResponseTime(long time) {
        this.responseTime = time;
    }

    @Override
    public long getResponseTime() {
        return responseTime;
    }
}
