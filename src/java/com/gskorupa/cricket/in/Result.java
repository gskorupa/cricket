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

/**
 *
 * @author greg
 */
public interface Result {
    
    /**
     * @return the status code
     */
    public int getCode();
    
    /**
     * @param code the status code to set
     */
    public void setCode(int code);
    
    /**
     * @return the status message
     */
    public String getMessage();
    
    /**
     * @param message the message to set
     */
    public void setMessage(String message);
    
    /**
     * @return the data
     */
    public Object getData();
    
    /**
     * @param data the data to set
     */
    public void setData(Object data);
    
    public byte[] getPayload();
    
    public void setPayload(byte[] payload);
    
    public String getFileExtension();
    
    public void setFileExtension(String fileExt);
    
}
