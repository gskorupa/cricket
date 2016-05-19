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
package org.cricketmsf.out;

import java.util.HashMap;
import org.cricketmsf.in.http.Result;

/**
 * HttpClient will be better name
 *
 * @author greg
 */
public interface OutbondHttpAdapterIface {

    public void loadProperties(HashMap<String, String> properties, String adapterName);

    public Result send(Object data);
    
    public String getUserAgent();
    
    public void setUserAgent(String userAgent);
    
    public String getContentType();
    
    public void setContentType(String contentType);
    
    public String getEndpointURL();
    
    public void setEndpointURL(String endpointURL);
    
    public String getRequestMethod();
    
    public void setRequestMethod(String requestMethod);
}
