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
package org.cricketmsf;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 * Many thanks for Leonardo Marcelino https://leonardom.wordpress.com
 */
public class ParameterFilter extends Filter {

    @Override
    public String description() {
        return "Parses the requested URI for parameters";
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain)
            throws IOException {
        String method=exchange.getRequestMethod().toUpperCase();

        switch (exchange.getRequestMethod().toUpperCase()) {
            case "GET":
                parseGetParameters(exchange);
                break;
            case "POST":
            case "PUT":
            case "DELETE":
                parsePostParameters(exchange);
                break;
        }
        chain.doFilter(exchange);
    }

    private void parseGetParameters(HttpExchange exchange)
            throws UnsupportedEncodingException {

        Map<String, Object> parameters = new HashMap<String, Object>();
        URI requestedUri = exchange.getRequestURI();
        String query = requestedUri.getRawQuery();
        parseQuery(query, parameters);
        exchange.setAttribute("parameters", parameters);
    }

    private void parsePostParameters(HttpExchange exchange)
            throws IOException {

        @SuppressWarnings("unchecked")
        Map<String, Object> parameters = new HashMap<String, Object>();
        InputStreamReader isr
                = new InputStreamReader(exchange.getRequestBody(), "utf-8");
        BufferedReader br = new BufferedReader(isr);
        String query = br.readLine();
        parseQuery(query, parameters);
        isr.close();
        exchange.setAttribute("parameters", parameters);
    }

    @SuppressWarnings("unchecked")
    private void parseQuery(String query, Map<String, Object> parameters)
            throws UnsupportedEncodingException {

        if (query != null) {
            String pairs[] = query.split("[&]");

            for (String pair : pairs) {
                String param[] = pair.split("[=]");

                String key = null;
                String value = null;
                if (param.length > 0) {
                    key = URLDecoder.decode(param[0],
                            System.getProperty("file.encoding"));
                }

                if (param.length > 1) {
                    value = URLDecoder.decode(param[1],
                            System.getProperty("file.encoding"));
                }

                if (parameters.containsKey(key)) {
                    Object obj = parameters.get(key);
                    if (obj instanceof List<?>) {
                        List<String> values = (List<String>) obj;
                        values.add(value);
                    } else if (obj instanceof String) {
                        List<String> values = new ArrayList<String>();
                        values.add((String) obj);
                        values.add(value);
                        parameters.put(key, values);
                    }
                } else {
                    parameters.put(key, value);
                }
            }
        }
    }
}
