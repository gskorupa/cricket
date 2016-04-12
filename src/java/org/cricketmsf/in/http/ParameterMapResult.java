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

import java.util.HashMap;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author greg
 */
@XmlRootElement (name = "result")
@XmlAccessorType (XmlAccessType.FIELD)
public class ParameterMapResult implements Result {

    @XmlElement(name = "data")
    private HashMap<String, String> data;
    
    private int code;
    private String message;
    private byte[] payload;
    private String fileExt;

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
        return null!=message ? message : "";
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
        this.data = (HashMap<String, String>) data;
    }

    /*
    public String toJsonString(){
        String jst=
                new JSONStringer()
                        .object()
                            .key("code")
                            .value(getCode())
                            .key("message")
                            .value(getMessage())
                            .key("data")
                            .value(new JSONObject(data))
                        .endObject()
                        .toString()
                +"\n";
        return jst;
    }

    public String toXmlString() {
        return null;
    }

    public String toCsvString() {
        return null;
    }
*/
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

}