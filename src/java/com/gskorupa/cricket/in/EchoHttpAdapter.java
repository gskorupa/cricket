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

import com.gskorupa.cricket.Adapter;
import java.util.Properties;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class EchoHttpAdapter extends HttpAdapter implements EchoHttpAdapterIface, Adapter {

    public void loadProperties(Properties properties) {
        setContext(properties.getProperty("EchoHttpAdapterIface-context"));
        System.out.println("context=" + getContext());
        getServiceHooks(); 
    }
    
    /**
     * Formats response sent back by this adapter
     * <p>
     * This method should be ommited if standard HttpAdapter.format method is OK.
     * @param type      required response type: HttpAdapter.JSON, HttpAdapter.XML
     *                  or HttpAdapter.CSV
     * @param result    data to send as a response
     * @return          String formatted according to required type
     */
    @Override
    public String formatResponse(int type, Result result){
        String response="";
        switch(type){
            case HttpAdapter.XML:
                response=((EchoResult)result).toXmlString();
                break;
            case HttpAdapter.CSV:
                response=((EchoResult)result).toJsonString();
                break;
            default:
                response=((EchoResult)result).toJsonString();
                break;
        }
        return response;
    }
    
}
