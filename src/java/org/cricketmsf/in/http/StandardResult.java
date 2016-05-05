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

import org.cricketmsf.in.http.Result;

/**
 *
 * @author greg
 */
public class StandardResult implements Result {

    private int code;
    private String message;
    private Object data;
    private byte[] payload;
    private String fileExtension;

    public StandardResult() {
        setCode(HttpAdapter.SC_OK);
    }

    public StandardResult(Object data) {
        setCode(HttpAdapter.SC_OK);
        setData(data);
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

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExt) {
        this.fileExtension = fileExtension;
    }

}
