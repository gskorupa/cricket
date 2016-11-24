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
package org.cricketmsf.in.http;

import org.cricketmsf.Event;
import org.cricketmsf.RequestObject;
import org.cricketmsf.Kernel;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import org.cricketmsf.annotation.HttpAdapterHook;
import org.cricketmsf.in.InboundAdapter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import org.cricketmsf.config.HttpHeader;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class HttpAdapter extends InboundAdapter implements HttpHandler {

    public final static String JSON = "application/json";
    public final static String XML = "text/xml";
    public final static String CSV = "text/csv";
    public final static String HTML = "text/html";
    public final static String TEXT = "text/plain";

    public final static int SC_OK = 200;
    public final static int SC_ACCEPTED = 202;
    public final static int SC_CREATED = 201;

    public final static int SC_MOVED_PERMANENTLY = 301;
    public final static int SC_NOT_MODIFIED = 304;

    public final static int SC_BAD_REQUEST = 400;
    public final static int SC_FORBIDDEN = 403;
    public final static int SC_NOT_FOUND = 404;
    public final static int SC_METHOD_NOT_ALLOWED = 405;
    public final static int SC_CONFLICT = 409;

    public final static int SC_INTERNAL_SERVER_ERROR = 500;
    public final static int SC_NOT_IMPLEMENTED = 501;

    public final static int SERVICE_MODE = 0;
    public final static int WEBSITE_MODE = 1;

    private final String[] acceptedTypes = {
        "application/json",
        "text/xml",
        "text/html",
        "text/csv",
        "text/plain"
    };
    protected HashMap<String, String> acceptedTypesMap;

    private String context;

    //private HashMap<String, String> hookMethodNames = new HashMap();
    private boolean extendedResponse = true;
    //private String dateFormat = "dd/MMM/yyyy:kk:mm:ss Z";
    SimpleDateFormat dateFormat;

    protected int mode = SERVICE_MODE;

    public HttpAdapter() {
        super();
        acceptedTypesMap = new HashMap<>();
        for (String acceptedType : acceptedTypes) {
            acceptedTypesMap.put(acceptedType, acceptedType);
        }
        dateFormat = Kernel.getInstance().dateFormat;
    }

    @Override
    protected void getServiceHooks(String adapterName) {
        HttpAdapterHook ah;
        String requestMethod;
        // for every method of a Kernel instance (our service class extending Kernel)
        for (Method m : Kernel.getInstance().getClass().getMethods()) {
            ah = (HttpAdapterHook) m.getAnnotation(HttpAdapterHook.class);
            // we search for annotated method
            if (ah != null) {
                requestMethod = ah.requestMethod();
                if (ah.adapterName().equals(adapterName)) {
                    addHookMethodNameForMethod(requestMethod, m.getName());
                    System.out.println("hook method for http method " + requestMethod + " : " + m.getName());
                }
            }
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        //int responseType = JSON;
        String acceptedResponseType = JSON;
        try {
            //acceptedResponseType = exchange.getRequestHeaders().get("Accept").get(0);
            acceptedResponseType
                    = acceptedTypesMap.getOrDefault(exchange.getRequestHeaders().get("Accept").get(0), JSON);
            //if (!acceptedTypesMap.containsKey(acceptedResponseType)) {
            //    acceptedResponseType = JSON;
            //}
        } catch (IndexOutOfBoundsException e) {
        }

        //Result result = createResponse(exchange, acceptedResponseType);
        Result result = createResponse(buildRequestObject(exchange, acceptedResponseType));

        acceptedResponseType = setResponseType(acceptedResponseType, result.getFileExtension());
        //set content type and print response to string format as JSON if needed
        Headers headers = exchange.getResponseHeaders();
        byte[] responseData;

        if (result.getCode() == SC_MOVED_PERMANENTLY) {
            headers.set("Location", result.getMessage());
            responseData = ("moved to " + result.getMessage()).getBytes();
        } else {
            if (acceptedTypesMap.containsKey(acceptedResponseType)) {
                headers.set("Content-Type", acceptedResponseType + "; charset=UTF-8");
                responseData = formatResponse(acceptedResponseType, result);
            } else {
                headers.set("Content-Type", getMimeType(result.getFileExtension()));
                responseData = result.getPayload();
            }
            headers.set("Last-Modified", result.getModificationDateFormatted());

            HttpHeader h;
            for (int i = 0; i < Kernel.getInstance().getCorsHeaders().size(); i++) {
                h = (HttpHeader) Kernel.getInstance().getCorsHeaders().get(i);
                headers.set(h.name, h.value);
            }
            if (result.getCode() == 0) {
                result.setCode(SC_OK);
            } else {
                if (responseData.length == 0) {
                    if (result.getMessage() != null) {
                        responseData = result.getMessage().getBytes();
                    }
                }
            }
        }

        exchange.sendResponseHeaders(result.getCode(), responseData.length);
        sendLogEvent(exchange, responseData.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseData);
            os.flush();
        }
        exchange.close();
    }

    private String getMimeType(String fileExt) {
        switch (fileExt) {
            case ".ico":
                return "image/x-icon";
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
            case ".json":
                return JSON;
            default:
                return TEXT;
        }
    }

    /**
     * Calculates response type based on the file type
     *
     * @param acceptedResponseType
     * @param fileExt
     * @return response type
     */
    protected String setResponseType(String acceptedResponseType, String fileExt) {
        //return fileExt != null ? expectedResponseType : NONE;
        return acceptedResponseType;
    }

    public byte[] formatResponse(String type, Result result) {
        String formattedResponse;
        switch (type) {
            case JSON:
                formattedResponse = JsonFormatter.getInstance().format(true, extendedResponse ? result : result.getData());
                break;
            case XML:
                formattedResponse = XmlFormatter.getInstance().format(true, extendedResponse ? result : result.getData());
                break;
            case CSV:
                // formats only Result.getData() object
                formattedResponse = CsvFormatter.getInstance().format(result);
                break;
            case TEXT:
                // formats only Result.getData() object
                formattedResponse = TxtFormatter.getInstance().format(result);
                break;
            default:
                formattedResponse = JsonFormatter.getInstance().format(true, result);
                break;
        }
        formattedResponse=formattedResponse;
        return formattedResponse.getBytes();
    }

    RequestObject buildRequestObject(HttpExchange exchange, String acceptedResponseType) {
        Map<String, Object> parameters = (Map<String, Object>) exchange.getAttribute("parameters");
        String method = exchange.getRequestMethod();
        //String adapterContext = exchange.getHttpContext().getPath();
        String pathExt = exchange.getRequestURI().getPath();
        if (null != pathExt) {
            pathExt = pathExt.substring(exchange.getHttpContext().getPath().length());
            if (pathExt.startsWith("/")) {
                pathExt = pathExt.substring(1);
            }
        }

        //
        RequestObject requestObject = new RequestObject();
        requestObject.method = method;
        requestObject.parameters = parameters;
        requestObject.uri = exchange.getRequestURI().toString();
        requestObject.pathExt = pathExt;
        requestObject.headers = exchange.getRequestHeaders();
        requestObject.clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        requestObject.acceptedResponseType = acceptedResponseType;

        return requestObject;
    }

    protected RequestObject preprocess(RequestObject request) {
        return request;
    }

    //private Result createResponse(HttpExchange exchange, String acceptedResponseType) {
    private Result createResponse(RequestObject requestObject) {

        Result result = new StandardResult();
        if (mode == WEBSITE_MODE) {
            if (!requestObject.uri.endsWith("/")) {
                if (requestObject.uri.lastIndexOf("/") > requestObject.uri.lastIndexOf(".")) {
                    // redirect to index.file but only if property index.file is not null
                    result.setCode(SC_MOVED_PERMANENTLY);
                    result.setMessage(requestObject.uri + "/");
                    return result;
                }
            }
        }
        String hookMethodName = getHookMethodNameForMethod(requestObject.method);

        if (hookMethodName == null) {
            sendLogEvent(Event.LOG_WARNING, "hook method is not defined for " + requestObject.method);
            result.setCode(SC_METHOD_NOT_ALLOWED);
            result.setMessage("method " + requestObject.method + " is not allowed");
            result.setFileExtension(null);
            //TODO: set "Allow" header
            return result;
        }
        try {
            sendLogEvent(Event.LOG_FINE, "sending request to hook method " + hookMethodName);
            Event event = new Event("HttpAdapter", requestObject);
            event.setPayload(requestObject);
            Method m = Kernel.getInstance().getClass().getMethod(hookMethodName, Event.class);
            result = (Result) m.invoke(Kernel.getInstance(), event);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            //e.printStackTrace();
            sendLogEvent(Event.LOG_SEVERE, e.getMessage());
            result.setCode(SC_INTERNAL_SERVER_ERROR);
            result.setMessage("handler method error");
            result.setFileExtension(null);
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

    @Override
    public void addHookMethodNameForMethod(String requestMethod, String hookMethodName) {
        hookMethodNames.put(requestMethod, hookMethodName);
    }

    @Override
    public String getHookMethodNameForMethod(String requestMethod) {
        String result = null;
        result = hookMethodNames.get(requestMethod);
        if (null == result) {
            result = hookMethodNames.get("*");
        }
        return result;
    }

    protected void sendLogEvent(HttpExchange exchange, int length) {
        //SimpleDateFormat sdf = new SimpleDateFormat("[dd/MMM/yyyy:kk:mm:ss Z]");
        StringBuilder sb = new StringBuilder();

        sb.append(exchange.getRemoteAddress().getAddress().getHostAddress());
        sb.append(" - ");
        try {
            sb.append(exchange.getPrincipal().getUsername());
        } catch (Exception e) {
            sb.append("-");
        }
        sb.append(" ");
        sb.append(dateFormat.format(new Date()));
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
                Event.CATEGORY_HTTP_LOG,
                Event.LOG_INFO,
                null,
                sb.toString());
        Kernel.getInstance().handleEvent(event);
    }

    protected void sendLogEvent(String type, String message) {
        Event event = new Event(
                "HttpAdapter",
                Event.CATEGORY_LOG,
                type,
                null,
                message);
        Kernel.getInstance().handleEvent(event);
    }

    protected void sendLogEvent(String message) {
        sendLogEvent(Event.LOG_INFO, message);
    }

    /**
     * @return the extendedResponse
     */
    public boolean isExtendedResponse() {
        return extendedResponse;
    }

    /**
     * @param paramValue
     */
    public void setExtendedResponse(String paramValue) {
        this.extendedResponse = !("false".equalsIgnoreCase(paramValue));
    }

    /*public String getDateFormat() {
        return dateFormat;
    }*/
    public void setDateFormat(String dateFormat) {
        if (dateFormat != null) {
            this.dateFormat = new SimpleDateFormat(dateFormat);
        }
    }

}
