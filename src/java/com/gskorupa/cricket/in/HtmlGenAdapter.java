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
package com.gskorupa.cricket.in;

import com.gskorupa.cricket.Adapter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class HtmlGenAdapter extends HttpAdapter implements HtmlGenAdapterIface, Adapter {

    @Override
    public void loadProperties(HashMap<String, String> properties) {
        setContext(properties.get("context"));
        System.out.println("context=" + getContext());
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
        if (type == FILE) {
            return result.getPayload();
        } else {
            return updateHtml((ParameterMapResult) result);
        }
    }

    @Override
    protected int setResponseType(int oryginalResponseType, String fileExt) {
        switch (fileExt) {
            case ".html":
            case ".htm":
                return HTML;
            default:
                return FILE;
        }
    }

    private byte[] updateHtml(ParameterMapResult result) {

        if (result.getData() != null) {
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
                    m.appendReplacement(res, replacement);
                }
                m.appendTail(res);
                return res.toString().getBytes();
            }
        }
        return result.getPayload();
    }

}
