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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class HtmlGenAdapter extends HttpAdapter implements HtmlGenAdapterIface, Adapter {

    private boolean useCache = false;
    private boolean processingVariables = false;
    

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
        useCache = properties.getOrDefault("use-cache", "false").equalsIgnoreCase("true");
        System.out.println("use-cache=" + useCache());
        processingVariables= (properties.getOrDefault("page-processor", "false").equalsIgnoreCase("true"));
        System.out.println("page-processor=" + processingVariables);
    }

    @Override
    public boolean useCache() {
        return useCache;
    }

    /**
     * Formats response sent back by this adapter
     *
     * @param type 
     * @param result received back from the service
     * @return the payload field of the result modified with parameters
     */
    @Override
    public byte[] formatResponse(String type, Result result) {
        if (HTML.equalsIgnoreCase(type) && processingVariables) {
            return updateHtml((ParameterMapResult) result);
        } else {
            return result.getPayload();
        }
    }

    @Override
    protected String setResponseType(String oryginalResponseType, String fileExt) {
        if (fileExt != null) {
            switch (fileExt) {
                case ".html":
                case ".htm":
                    return "text/html";
                default:
                    return "";
            }
        } else {
            return oryginalResponseType;
        }
    }

    private byte[] updateHtml(ParameterMapResult result) {

        if (result.getData() != null && result.getPayload()!=null) {
            HashMap map = (HashMap) result.getData();
            if (!map.isEmpty()) {
                //output = result.getPayload();
                // replace using regex
                Pattern p = Pattern.compile("(\\$\\w+)");
                Matcher m = p.matcher(new String(result.getPayload()));

                StringBuffer res = new StringBuffer();
                String paramName;
                String replacement;
                while (m.find()) {
                    paramName = m.group().substring(1);
                    replacement = (String) map.getOrDefault(paramName, m.group());
                    try {
                        m.appendReplacement(res, replacement);
                    } catch (Exception e) {

                    }
                }
                m.appendTail(res);
                return res.toString().getBytes();
            }
        }
        return result.getPayload();
    }

}
