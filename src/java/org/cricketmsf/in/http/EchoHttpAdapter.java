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

import org.cricketmsf.Adapter;
import java.util.HashMap;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class EchoHttpAdapter extends HttpAdapter implements EchoHttpAdapterIface, Adapter {

    @Override
    public void loadProperties(HashMap<String,String> properties) {
        setContext(properties.get("context"));
        System.out.println("context=" + getContext());
    }
    
    /**
     * Formats response sent back by this adapter
     * <p>
     * This method could be ommited if standard HttpAdapter.format method is OK.
     * If you prefer to use HttpAdapter.format then you should use the full Cricket distribution.
     * @param type      required response type: HttpAdapter.JSON, HttpAdapter.XML
     *                  or HttpAdapter.CSV
     * @param result    data to send as a response
     * @return          String formatted according to required type
     */
    
    @Override
    public byte[] formatResponse(int type, Result result){
        return super.formatResponse(type, result);
        /*
        String response="";
        switch(type){
            case HttpAdapter.XML:
                response=((ParameterMapResult)result).toXmlString();
                break;
            case HttpAdapter.CSV:
                response=((ParameterMapResult)result).toJsonString();
                break;
            default:
                response=((ParameterMapResult)result).toJsonString();
                break;
        }
        return response;
        */
    }
    
}
