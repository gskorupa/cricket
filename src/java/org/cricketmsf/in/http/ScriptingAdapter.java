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
public class ScriptingAdapter extends HttpAdapter implements HttpAdapterIface, Adapter {

    private String responseTypeString;
    private int responseType;

    /**
     * This method is executed while adapter is instantiated during the service
     * start. It's used to configure the adapter according to the configuration.
     *
     * @param properties map of properties readed from the configuration file
     * @param adapterName name of the adapter set in the configuration file (can
     * be different from the interface and class name.
     */
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.getServiceHooks(adapterName);
        setContext(properties.get("context"));
        System.out.println("context=" + getContext());
        setResponseType(properties.get("response-type"));
        System.out.println("response-type=" + getResponseTypeString());
    }

    /**
     * Formats response sent back by this adapter
     *
     * @param type ignored
     * @param result received back from the service
     * @return the payload field of the result modified with parameters
     */
    @Override
    public byte[] formatResponse(int type, Result result) {
        return result.getPayload();
    }

    protected int setResponseType(String config) {
        switch (config.toUpperCase()) {
            case "JSON": 
                setResponseTypeString("JSON");
                responseType = JSON;
            case "TEXT": 
                setResponseTypeString("TEXT");
                responseType = TEXT;
            default:
                setResponseTypeString("JSON");
                responseType = JSON;
        }
        return responseType;
    }

    /**
     * @return the responseType
     */
    public int getResponseType() {
        return responseType;
    }


    /**
     * @return the responseTypeString
     */
    public String getResponseTypeString() {
        return responseTypeString;
    }

    /**
     * @param responseTypeString the responseTypeString to set
     */
    public void setResponseTypeString(String responseTypeString) {
        this.responseTypeString = responseTypeString;
    }

}
