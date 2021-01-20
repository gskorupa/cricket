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
package org.cricketmsf.api;

import com.sun.net.httpserver.Headers;
import java.util.Date;
import java.util.List;

/**
 *
 * @author greg
 */
public interface ResultIface {
    
    //public String getProcedureName();
    //public void setProcedureName(String procedureName);
    public int getProcedure();
    public void setProcedure(int procedure);
    public int getCode();
    public void setCode(int code);
    public String getMessage();
    public void setMessage(String message);
    public String getContentType();
    public void setContentType(String contentType);    
    public Object getData();
    public void setData(Object data);
    public byte[] getPayload();
    public void setPayload(byte[] payload);
    public String getFileExtension();
    public void setFileExtension(String fileExt);
    public void setModificationDate(Date date);
    public Date getModificationDate();
    public String getModificationDateFormatted();
    public int getMaxAge();
    public void setMaxAge(int maxAge);
    public void setHeader(String name, String value);
    public void setHeader(String name, List values);
    public Headers getHeaders();
    public void setResponseTime(long time);
    public long getResponseTime();
    //public ResultIface procedureName(String procedureName);
    public ResultIface procedure(int procedure);
}
