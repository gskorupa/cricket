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

    private boolean silent = false;

    /**
     * This method is executed while adapter is instantiated during the service start.
     * It's used to configure the adapter according to the configuration.
     * 
     * @param properties    map of properties readed from the configuration file
     * @param adapterName   name of the adapter set in the configuration file (can be different
     *  from the interface and class name.
     */
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.getServiceHooks(adapterName);
        setContext(properties.get("context"));
        System.out.println("context=" + getContext());
        setSilent(properties.get("silent-mode"));
        System.out.println("silent-mode=" + isSilent());
    }

    /**
     * Formats response sent back by this adapter
     * <p>
     * This method could be omited if standard HttpAdapter.format method is OK.
     * If you prefer to use HttpAdapter.format then you should use the full
     * Cricket distribution.
     *
     * @param type required response type: HttpAdapter.JSON, HttpAdapter.XML or
     * HttpAdapter.CSV
     * @param result data to send as a response
     * @return String formatted according to required type
     */
    @Override
    public byte[] formatResponse(int type, Result result) {
        return super.formatResponse(type, result);
    }

    /**
     * @return the silent
     */
    public boolean isSilent() {
        return silent;
    }

    /**
     * @param silent the silent to set
     */
    public void setSilent(String silent) {
        this.silent = Boolean.parseBoolean(silent);
    }

}
