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

import java.io.BufferedReader;
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
public class OutbondHttpAdapter implements OutbondHttpAdapterIface, Adapter {

    private final String JSON = "application/json";
    private final String CSV = "text/csv";

    private String userAgent = "Mozilla/5.0";
    private String contentType = "application/json";
    private String endpointURL;
    private String requestMethod = "POST";

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        setEndpointURL(properties.get("url"));
        System.out.println("url: " + getEndpointURL());
    }

    private boolean isRequestSuccessful(int code) {
        return code == HttpURLConnection.HTTP_ACCEPTED || code == HttpURLConnection.HTTP_CREATED || code == HttpURLConnection.HTTP_OK;
    }

    public Result send(Object data) {
        String requestData = "";

        StandardResult result = new StandardResult();
        switch (getContentType().toLowerCase()) {
            case JSON:
                requestData = translateToJson(data);
                break;
            case CSV:
                requestData = translateToCsv(data);
                break;
            default:
                System.out.println("unsupported content type: "+getContentType());
        }
        System.out.println(requestData);
        result.setCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
        //result.setContent("");
        //result.setContentLength(0);
        //result.setResponseTime(-1);
        try {

            Kernel.handle(Event.logFine(this.getClass().getSimpleName(), "sending to " + getEndpointURL()));

            long startPoint = System.currentTimeMillis();
            URL obj = new URL(getEndpointURL());
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod(getRequestMethod());
            con.setRequestProperty("User-Agent", getUserAgent());
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", getContentType());
            con.setFixedLengthStreamingMode(requestData.getBytes().length);
            PrintWriter out = new PrintWriter(con.getOutputStream());
            out.print(requestData);
            out.close();
            con.connect();
            result.setCode(con.getResponseCode());
            //System.out.println(result.getCode());
            //result.setResponseTime(System.currentTimeMillis() - startPoint);
            if (isRequestSuccessful(result.getCode())) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                //result.setContentLength(response.length());
                result.setPayload(response.toString().getBytes());
            } else {
                //result.setContent("");
            }
        } catch (Exception e) {
            Kernel.handle(Event.logWarning(this.getClass().getSimpleName(), e.getMessage()));
            result.setCode(500);
        }
        return result;
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
        StringBuffer sb = new StringBuffer();
        if (header != null && !header.isEmpty()) {
            sb.append(header).append("\r\n");
        }
        sb.append(header).append("\r\n");
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
     * @return the userAgent
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * @param userAgent the userAgent to set
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @return the endpointURL
     */
    public String getEndpointURL() {
        return endpointURL;
    }

    /**
     * @param endpointURL the endpointURL to set
     */
    public void setEndpointURL(String endpointURL) {
        this.endpointURL = endpointURL;
    }

    /**
     * @return the requestMethod
     */
    public String getRequestMethod() {
        return requestMethod;
    }

    /**
     * @param requestMethod the requestMethod to set
     */
    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

}
