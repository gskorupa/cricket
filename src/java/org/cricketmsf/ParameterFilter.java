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
 * This filter is used to recognize, parse and transform request parameters into
 * the parameters map which could be easily accessible within adapters or
 * service methods.
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 * Many thanks for Leonardo Marcelino https://leonardom.wordpress.com
 */
public class ParameterFilter extends Filter {
    
    private String fileEncoding;
    
    public ParameterFilter(){
        super();
        fileEncoding = System.getProperty("file.encoding");
    }

    @Override
    public String description() {
        return "Parses the requested URI for parameters";
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain)
            throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        //System.out.println("ParameterFilter: doFilter");
        switch (method) {
            case "GET":
                exchange.setAttribute("parameters", parseGetParameters(exchange));
                break;
            case "POST":
            case "PUT":
            case "DELETE":
                Map<String, Object> tmp = parsePostParameters(exchange);
                if(tmp.containsKey("&&&data")){
                    exchange.setAttribute("body", tmp.get("&&&data"));
                    tmp.remove("&&&data");
                }
                exchange.setAttribute("parameters", tmp);
                break;
            default:
                exchange.setAttribute("parameters", parseGetParameters(exchange));
        }
        //System.out.println("ParameterFilter: doFilter ended");
        chain.doFilter(exchange);
    }

    private Map<String, Object> parseGetParameters(HttpExchange exchange)
            throws UnsupportedEncodingException {

        Map<String, Object> parameters = new HashMap<String, Object>();
        URI requestedUri = exchange.getRequestURI();
        String query = requestedUri.getRawQuery();
        ArrayList<RequestParameter> list = parseQuery(query);
        list.forEach((param) -> {
            parameters.put(param.name, param.value);
        });
        return parameters;
    }

    private Map<String, Object> parsePostParameters(HttpExchange exchange)
            throws IOException {

        String contentTypeHeader = exchange.getRequestHeaders().getFirst("Content-Type");
        String contentType = "";
        if (contentTypeHeader != null) {
            if (contentTypeHeader.indexOf(";") > 0) {
                contentType = contentTypeHeader.substring(0, contentTypeHeader.indexOf(";"));
            } else {
                contentType = contentTypeHeader;
            }
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> parameters = new HashMap<>();
        InputStreamReader isr
                = new InputStreamReader(exchange.getRequestBody(), "utf-8");
        BufferedReader br = new BufferedReader(isr);
        String query;
        StringBuilder content = new StringBuilder();
        //System.out.println("ParameterFilter: " + contentType);
        switch (contentType.toLowerCase()) {
            case "multipart/form-data":
                parameters = parseForm(contentTypeHeader.substring(30), br);
                break;
            case "text/plain":
            case "text/csv":
            case "application/json":
            case "text/xml":
                while ((query = br.readLine()) != null) {
                    content.append(query);
                    content.append("\r\n");
                }
                //System.out.println(content.toString());
                parameters.put("data", content.toString()); //TODO: remove "data" parameter
                parameters.put("&&&data", content.toString());
                //exchange.setAttribute("body", content.toString());
                break;
            default:
                ArrayList<RequestParameter> list;
                while ((query = br.readLine()) != null) {
                    list = parseQuery(query);
                    for (RequestParameter param : list) {
                        if (parameters.containsKey(param.name)) {
                            Object obj = parameters.get(param.name);
                            if (obj instanceof List<?>) {
                                List<String> values = (List<String>) obj;
                                values.add(param.value);
                            } else if (obj instanceof String) {
                                List<String> values = new ArrayList<>();
                                values.add((String) obj);
                                values.add(param.value);
                                parameters.put(param.name, values);
                            }
                        } else {
                            parameters.put(param.name, param.value);
                        }
                    }
                }
        }

        isr.close();
        //exchange.setAttribute("parameters", parameters);
        //System.out.println("ParameterFilter: "+parameters.size());
        return parameters;
    }

    private HashMap<String, Object> parseForm(String boundary, BufferedReader br)
            throws IOException {
        //System.out.println("parsing form");
        HashMap<String, Object> parameters = new HashMap<>();
        String line;
        String contentDisposition;
        String paramName;
        String value;
        line = br.readLine();
        //System.out.println(line);
        try {
            do {
                //first line is boundary
                //read next
                contentDisposition = br.readLine();
                paramName = contentDisposition.substring(38, contentDisposition.length() - 1);
                //empty line
                line = br.readLine();
                //System.out.println(line);
                value = "";
                while (!(line = br.readLine()).startsWith("--" + boundary)) {
                    value = value.concat(line);
                }
                parameters.put(paramName, value);
            } while (!line.equals("--" + boundary + "--"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("ParameterFilter.parseForm: "+parameters.size());
        return parameters;
    }

    @SuppressWarnings("unchecked")
    private ArrayList parseQuery(String query)
            throws UnsupportedEncodingException {
        //System.out.println("parseQuery: " + query);
        ArrayList<RequestParameter> list = new ArrayList<>();
        if (query == null || query.isEmpty()) {
            return list;
        }
        
        String pairs[] = query.split("[&]");
        for (String pair : pairs) {
            String param[] = pair.split("[=]");
            String key = null;
            String value = null;
            if (param.length > 0) {
                key = URLDecoder.decode(param[0],
                        fileEncoding); //TODO: static
            }
            if (param.length > 1) {
                value = URLDecoder.decode(param[1],
                        fileEncoding);
            }
            //System.out.println("parseQuery:[" + key + "][" + value + "]");
            list.add(new RequestParameter(key, value));
        }
        return list;
    }
    
    //TODO: remove
    @SuppressWarnings("unchecked")
    private ArrayList parseQuery(String query, Map<String, Object> parameters)
            throws UnsupportedEncodingException {
        //System.out.println("parseQuery: " + query);
        if (query == null || query.isEmpty()) {
            return null;
        }
        ArrayList<RequestParameter> list = new ArrayList<>();
        String pairs[] = query.split("[&]");
        for (String pair : pairs) {
            String param[] = pair.split("[=]");
            String key = null;
            String value = null;
            if (param.length > 0) {
                key = URLDecoder.decode(param[0],
                        fileEncoding); //TODO: static
            }
            if (param.length > 1) {
                value = URLDecoder.decode(param[1],
                        fileEncoding);
            }
            //System.out.println("parseQuery:[" + key + "][" + value + "]" + parameters.containsKey(key));
            list.add(new RequestParameter(key, value));
            /*
            if (parameters.containsKey(key)) {
                Object obj = parameters.get(key);
                if (obj instanceof List<?>) {
                    List<String> values = (List<String>) obj;
                    values.add(value);
                } else if (obj instanceof String) {
                    List<String> values = new ArrayList<>();
                    values.add((String) obj);
                    values.add(value);
                    parameters.put(key, values);
                }
            } else {
                parameters.put(key, value);
            }
             */
        }
        return list;
    }
}
