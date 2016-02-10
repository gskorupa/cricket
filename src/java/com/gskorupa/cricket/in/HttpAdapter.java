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

import com.gskorupa.cricket.Event;
import com.gskorupa.cricket.RequestObject;
import com.gskorupa.cricket.Kernel;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import com.gskorupa.cricket.HttpAdapterHook;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class HttpAdapter extends InboundAdapter implements HttpHandler {

    public final static int JSON = 0;
    public final static int XML = 1;
    public final static int CSV = 2;
    public final static int HTML = 3;
    public final static int FILE = 4;
    public final static int TEXT = 5;

    public final static int SC_OK = 200;
    public final static int SC_ACCEPTED = 202;
    public final static int SC_CREATED = 201;

    public final static int SC_NOT_MODIFIED = 304;

    public final static int SC_BAD_REQUEST = 400;
    public final static int SC_FORBIDDEN = 403;
    public final static int SC_NOT_FOUND = 404;
    public final static int SC_METHOD_NOT_ALLOWED = 405;
    public final static int SC_CONFLICT = 409;

    public final static int SC_INTERNAL_SERVER_ERROR = 500;
    public final static int SC_NOT_IMPLEMENTED = 501;

    private String context;

    private HashMap<String, String> hookMethodNames = new HashMap();

    public HttpAdapter() {
        getServiceHooks();
    }

    private void getServiceHooks() {
        HttpAdapterHook ah;
        String requestMethod;
        // for every method of a Kernel instance (our service class extending Kernel)
        for (Method m : Kernel.getInstance().getClass().getMethods()) {
            ah = (HttpAdapterHook) m.getAnnotation(HttpAdapterHook.class);
            // we search for annotated method
            if (ah != null) {
                requestMethod = ah.requestMethod();
                // 'this' is a handler class loaded according to configuration described in propertis
                // file
                // we need to find all names of implemented interfaces because
                // handler class is mapped by the interface name
                for (Class c : this.getClass().getInterfaces()) {
                    if (ah.handlerClassName().equals(c.getSimpleName())) {
                        addHookMethodNameForMethod(requestMethod, m.getName());
                        System.out.println("hook method for http method " + requestMethod + " : " + m.getName());
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        int responseType = JSON;

        for (String v : exchange.getRequestHeaders().get("Accept")) {
            switch (v.toLowerCase()) {
                case "application/json":
                    responseType = JSON;
                    break;
                case "text/xml":
                    responseType = XML;
                    break;
                case "text/html":
                    responseType = HTML;
                    break;
                case "text/csv":
                    responseType = CSV;
                    break;
                default:
                    responseType = JSON;
                    break;
            }

        }

        Result result = createResponse(exchange);

        responseType = setResponseType(responseType, result.getFileExtension());

        //set content type and print response to string format as JSON if needed
        Headers headers = exchange.getResponseHeaders();
        byte[] responseData = {};

        switch (responseType) {
            case JSON:
                headers.set("Content-Type", "application/json; charset=UTF-8");
                responseData=formatResponse(JSON, result);
                break;
            case XML:
                headers.set("Content-Type", "text/xml; charset=UTF-8");
                responseData=formatResponse(XML, result);
                break;
            case HTML:
                headers.set("Content-Type", "text/html; charset=UTF-8");
                responseData=formatResponse(HTML, result);
                break;
            case CSV:
                headers.set("Content-Type", "text/csv; charset=UTF-8");
                responseData=formatResponse(CSV, result);
                break;
            default:
                headers.set("Content-Type", getMimeType(result.getFileExtension()));
                responseData=result.getPayload();
                break;
        }
        //calculate error code from response object
        int errCode = 200;
        switch (result.getCode()) {
            case 0:
                errCode = 200;
                break;
            case 405:
                if (responseData.length==0) {
                    responseData = result.getMessage().getBytes();
                }
                errCode = 405;
                break;
            default:
                errCode = result.getCode();
                break;
        }
        //System.out.println("RESPONSE: "+stringResponse);
        exchange.sendResponseHeaders(errCode, responseData.length);
        sendLogEvent(exchange, responseData.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseData);
        os.close();
        exchange.close();
    }

    private String getMimeType(String fileExt){
        switch (fileExt) {
            case ".jpg":
                return "image/jpg";
            case ".jpeg":
                return "image/jpeg";
            case ".gif":
                return "image/gif";
            case ".png":
                return "image/png";
            case ".css":
                return "text/css";
            case ".js":
                return "text/javascript";
            default:
                return "text/plain";
        }
    }
    
    /**
     *
     */
    protected int setResponseType(int oryginalResponseType, String fileExt) {
        return oryginalResponseType;
    }

    public byte[] formatResponse(int type, Result result) {
        String formattedResponse;
        switch (type) {
            case JSON:
                formattedResponse = JsonFormatter.getInstance().format(true, result);
                break;
            case XML:
                formattedResponse = XmlFormatter.getInstance().format(true, result);
                break;
            case CSV:
                formattedResponse = CsvFormatter.getInstance().format(result);
                break;
            default:
                formattedResponse = JsonFormatter.getInstance().format(true, result);
                break;
        }
        return formattedResponse.getBytes();
    }

    private Result createResponse(HttpExchange exchange) {

        Map<String, Object> parameters = (Map<String, Object>) exchange.getAttribute("parameters");
        String method = exchange.getRequestMethod();
        String adapterContext = exchange.getHttpContext().getPath();
        String pathExt = exchange.getRequestURI().getPath();
        if (null != pathExt) {
            pathExt = pathExt.substring(adapterContext.length());
            if (pathExt.startsWith("/")) {
                pathExt = pathExt.substring(1);
            }
        }

        //
        RequestObject requestObject = new RequestObject();
        requestObject.method = method;
        requestObject.parameters = parameters;
        requestObject.pathExt = pathExt;

        Result result = null;
        String hookMethodName = getHookMethodNameForMethod(method);

        if (hookMethodName == null) {
            sendLogEvent(Event.LOG_WARNING, "hook method is not defined for " + method);
            result = new ParameterMapResult();
            result.setCode(SC_METHOD_NOT_ALLOWED);
            result.setMessage("method " + method + " is not allowed");
            //todo: set "Allow" header
            return result;
        }
        try {
            sendLogEvent(Event.LOG_FINE, "sending request to hook method " + hookMethodName);
            Event event=new Event("HttpAdapter", Event.CATEGORY_GENERIC, "HTTP", null, requestObject);
            event.setPayload(requestObject);
            Method m = Kernel.getInstance().getClass().getMethod(hookMethodName, Event.class);
            result = (Result) m.invoke(Kernel.getInstance(), event);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @param context the context to set
     */
    public void setContext(String context) {
        this.context = context;
    }

    public String getContext() {
        return context;
    }

    public void addHookMethodNameForMethod(String requestMethod, String hookMethodName) {
        hookMethodNames.put(requestMethod, hookMethodName);
    }

    public String getHookMethodNameForMethod(String requestMethod) {
        String result = null;
        result = hookMethodNames.get(requestMethod);
        if (null == result) {
            result = hookMethodNames.get("*");
        }
        return result;
    }

    protected void sendLogEvent(HttpExchange exchange, int length) {
        SimpleDateFormat sdf = new SimpleDateFormat("[dd/MMM/yyyy:kk:mm:ss Z]");
        StringBuilder sb = new StringBuilder();

        sb.append(exchange.getRemoteAddress().getAddress().getHostAddress());
        sb.append(" - ");
        try {
            sb.append(exchange.getPrincipal().getUsername());
        } catch (Exception e) {
            sb.append("-");
        }
        sb.append(" ");
        sb.append(sdf.format(new Date()));
        sb.append(" ");
        sb.append(exchange.getRequestMethod());
        sb.append(" ");
        sb.append(exchange.getProtocol());
        sb.append(" ");
        sb.append(exchange.getRequestURI());
        sb.append(" ");
        sb.append(exchange.getResponseCode());
        sb.append(" ");
        sb.append(length);

        Event event = new Event(
                "HttpAdapter",
                Event.CATEGORY_LOG,
                Event.LOG_INFO,
                null,
                sb.toString());

        try {
            Method m = Kernel.getInstance().getClass().getMethod(getHookMethodNameForEvent("LOG"), Event.class);
            m.invoke(Kernel.getInstance(), event);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    protected void sendLogEvent(String type, String message) {
        Event event = new Event(
                "HttpAdapter",
                Event.CATEGORY_LOG,
                type,
                null,
                message);
        try {
            Method m = Kernel.getInstance().getClass().getMethod(getHookMethodNameForEvent("LOG"), Event.class);
            m.invoke(Kernel.getInstance(), event);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    protected void sendLogEvent(String message) {
        sendLogEvent(Event.LOG_INFO, message);
    }

}
