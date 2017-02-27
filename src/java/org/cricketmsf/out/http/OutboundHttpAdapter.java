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
package org.cricketmsf.out.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.in.http.Result;
import org.cricketmsf.in.http.StandardResult;

/**
 * HttpClient will be better name
 *
 * @author greg
 */
public class OutboundHttpAdapter implements OutboundHttpAdapterIface, Adapter {

    private final String JSON = "application/json";
    private final String CSV = "text/csv";
    private final String HTML = "text/html";
    private final String TEXT = "text/plain";
    private final String XML = "text/xml";

    private String endpointURL;
    protected int timeout = 0;

    public HashMap<String, String> properties = new HashMap<>();

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        endpointURL = properties.get("url");
        properties.put("url", endpointURL);
        Kernel.getInstance().getLogger().print("\turl: " + endpointURL);
        try {
            properties.put("timeout", properties.getOrDefault("timeout", "120000"));
            timeout = Integer.parseInt(properties.getOrDefault("timeout", "120000"));
        } catch (NumberFormatException e) {

        }
        Kernel.getInstance().getLogger().print("\ttimeout: " + timeout);

    }

    private boolean isRequestSuccessful(int code) {
        return code == HttpURLConnection.HTTP_ACCEPTED || code == HttpURLConnection.HTTP_CREATED || code == HttpURLConnection.HTTP_OK;
    }

    @Override
    public Result send(Object data) {
        return send(endpointURL, null, data, true);
    }

    @Override
    public Result send(String url, Request request, Object data) {
        return send(url, request, data, true);
    }

    @Override
    public Result send(Object data, boolean transform) {
        return send(endpointURL, null, data, transform);
    }

    @Override
    public Result send(Request request, boolean transform) {
        return send(endpointURL, request, null, transform);
    }

    @Override
    public Result send(String url, Request request, Object data, boolean transform) {

        if (request == null) {
            request = new Request();
        }
        if (data != null) {
            request.setData(data);
        }
        String requestData = "";

        StandardResult result = new StandardResult();
        if (transform) {
            switch (request.properties.get("Content-Type")) {
                case JSON:
                    requestData = translateToJson(request.data);
                    break;
                case CSV:
                    requestData = translateToCsv(request.data);
                    break;
                case TEXT:
                    requestData = translateToText(request.data);
                    break;
                case HTML:
                    requestData = translateToHtml(request.data);
                    break;
                default:
                    Kernel.handle(
                            Event.logSevere(this.getClass().getSimpleName(),
                                    "unsupported content type: " + request.properties.get("Content-Type"))
                    );
            }
        } else {
            if (request.data != null) {
                if (requestData instanceof String) {
                    requestData = (String) request.data;
                } else {
                    requestData = request.data.toString();
                }
            }
        }
        result.setCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
        try {

            Kernel.handle(Event.logFine(this.getClass().getSimpleName(), "sending to " + url));

            long startPoint = System.currentTimeMillis();
            URL urlObj = new URL(url);
            HttpURLConnection con;
            // TODO: Proxy proxy = new Proxy(Proxy.Type.HTTP, sa);
            con = (HttpURLConnection) urlObj.openConnection();
            con.setReadTimeout(timeout);
            con.setConnectTimeout(timeout);
            //TODO: this adapter can block entire service waiting for timeout
            con.setRequestMethod(request.method);
            for (String key : request.properties.keySet()) {
                con.setRequestProperty(key, request.properties.get(key));
            }

            if (requestData.length() > 0) {
                con.setDoOutput(true);
                con.setFixedLengthStreamingMode(requestData.getBytes().length);
                try (PrintWriter out = new PrintWriter(con.getOutputStream())) {
                    out.print(requestData);
                }
            }
            con.connect();
            result.setCode(con.getResponseCode());
            //System.out.println(result.getCode());
            //result.setResponseTime(System.currentTimeMillis() - startPoint);
            if (isRequestSuccessful(result.getCode())) {
                StringBuilder response;
                try ( // success
                        BufferedReader in = new BufferedReader(new InputStreamReader(
                                con.getInputStream()))) {
                    String inputLine;
                    response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                }
                //result.setContentLength(response.length());
                result.setPayload(response.toString().getBytes());
            } else {
                //result.setContent("");
            }
        } catch (IOException e) {
            String message = e.getMessage();
            Kernel.handle(Event.logWarning(this.getClass().getSimpleName(), message));
            result.setCode(500);
            result.setMessage(message);
        }
        return result;
    }

    protected String translateToHtml(Object data) {
        return translateToText(data);
    }

    protected String translateToText(Object data) {
        if (data != null) {
            return data.toString();
        }
        return "";
    }

    protected String translateToCsv(Object data) {
        return translateToCsv(data, null);
    }

    protected String translateToCsv(Object data, String header) {
        List list;
        if (data instanceof List) {
            list = (List) data;
        } else {
            list = new ArrayList();
            list.add(data);
        }
        StringBuilder sb = new StringBuilder();
        if (header != null && !header.isEmpty()) {
            sb.append(header).append("\r\n");
        }
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i).toString());
            sb.append("\r\n");
        }
        return sb.toString();
    }

    protected String translateToJson(Object data) {
        return null;
    }

    /**
     * @return the properties
     */
    public HashMap<String, String> getProperties() {
        return properties;
    }

}
