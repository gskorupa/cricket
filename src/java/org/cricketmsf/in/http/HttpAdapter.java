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
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Map;
import org.cricketmsf.annotation.HttpAdapterHook;
import org.cricketmsf.in.InboundAdapter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.cricketmsf.Stopwatch;
import org.cricketmsf.in.InboundAdapterIface;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class HttpAdapter extends InboundAdapter implements HttpAdapterIface, HttpHandler {

    public final static String JSON = "application/json";
    public final static String XML = "text/xml";
    public final static String CSV = "text/csv";
    public final static String HTML = "text/html";
    public final static String TEXT = "text/plain";

    public final static int SC_OK = 200;
    public final static int SC_ACCEPTED = 202;
    public final static int SC_CREATED = 201;

    public final static int SC_MOVED_PERMANENTLY = 301;
    public final static int SC_MOVED_TEMPORARY = 302;
    public final static int SC_NOT_MODIFIED = 304;

    public final static int SC_BAD_REQUEST = 400;
    public final static int SC_UNAUTHORIZED = 401;
    public final static int SC_SESSION_EXPIRED = 401;
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
                    //System.out.println("hook method for http method " + requestMethod + " : " + m.getName());
                }
            }
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        new Thread(() -> {
            try {
                doHandle(exchange);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void doHandle(HttpExchange exchange) throws IOException {

        //System.out.println("HANDLE query exchange "+exchange.toString());
        Stopwatch timer = new Stopwatch();
        Event rootEvent = new Event();
        String acceptedResponseType = JSON;
        //System.out.println("doHandle requestHeaders "+exchange.getRequestHeaders());

        try {
            acceptedResponseType
                    = acceptedTypesMap.getOrDefault(exchange.getRequestHeaders().get("Accept").get(0), JSON);

        } catch (Exception e) {
        }

        // cerating Result object
        Result result = createResponse(buildRequestObject(exchange, acceptedResponseType), rootEvent.getId());
        //System.out.println("RESPONSE CREATED ");

        acceptedResponseType = setResponseType(acceptedResponseType, result.getFileExtension());

        //set content type and print response to string format as JSON if needed
        Headers headers = exchange.getResponseHeaders();
        byte[] responseData;

        result.getHeaders().keySet().forEach((key) -> {
            List<String> values = result.getHeaders().get(key);
            for (int i = 0; i < values.size(); i++) {
                headers.set(key, values.get(i));
            }
        });

        if (result.getCode() == SC_MOVED_PERMANENTLY || result.getCode() == SC_MOVED_TEMPORARY) {
            headers.set("Location", result.getMessage());
            responseData = ("moved to " + result.getMessage()).getBytes("UTF-8");
        } else {
            if (!headers.containsKey("Content-type")) {
                if (acceptedTypesMap.containsKey(acceptedResponseType)) {
                    headers.set("Content-type", acceptedResponseType + "; charset=UTF-8");
                    responseData = formatResponse(acceptedResponseType, result);
                } else {
                    headers.set("Content-type", getMimeType(result.getFileExtension()));
                    responseData = result.getPayload();
                }
            }else{
                responseData = result.getPayload();
            }
            headers.set("Last-Modified", result.getModificationDateFormatted());
            //TODO: get max age and no-cache info from the result object
            if (result.getMaxAge() > 0) {
                headers.set("Cache-Control", "max-age=" + result.getMaxAge());  // 1 hour
            } else {
                headers.set("Pragma", "no-cache");
            }

            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                CorsProcessor.getResponseHeaders(headers, exchange.getRequestHeaders(), Kernel.getInstance().getCorsHeaders());
            }else if(exchange.getRequestURI().getPath().startsWith("/api/")){ //TODO: this is workaround
                CorsProcessor.getResponseHeaders(headers, exchange.getRequestHeaders(), Kernel.getInstance().getCorsHeaders());
            }

            if (result.getCode() == 0) {
                result.setCode(SC_OK);
            } else {
                if (responseData.length == 0) {
                    if (result.getMessage() != null) {
                        responseData = result.getMessage().getBytes("UTF-8");
                    }
                }
            }
        }
        /*
        headers.set("Connection", "Keep-Alive");
        headers.set("Server", "Cricket/1.0");
        headers.set("ETag", ""+rootEvent.getId());
         */
        //TODO: format logs to have clear info about root event id
        Kernel.handle(
                Event.logFinest("HttpAdapter", "event " + rootEvent.getId() + " processing takes " + timer.time(TimeUnit.MILLISECONDS) + "ms")
        );

        //exchange.sendResponseHeaders(result.getCode(), responseData.length);
        if (responseData.length > 0) {
            exchange.sendResponseHeaders(result.getCode(), responseData.length);
            try (OutputStream os = exchange.getResponseBody()) {
                //os.write("\r\n".getBytes("UTF-8"));
                os.write(responseData);
                //os.flush();
            }
        } else {
            exchange.sendResponseHeaders(result.getCode(), -1);
        }
        sendLogEvent(exchange, responseData.length);
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
            case ".svg":
                return "image/svg+xml";
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
        byte[] r = {};
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
        formattedResponse = formattedResponse;
        try {
            r = formattedResponse.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            Kernel.handle(Event.logSevere("HttpAdapter", e.getMessage()));
        }
        return r;
    }

    RequestObject buildRequestObject(HttpExchange exchange, String acceptedResponseType) {

        // Remember that "parameters" attribute is created by filter
        Map<String, Object> parameters = (Map<String, Object>) exchange.getAttribute("parameters");
        //System.out.println("queryInRequest=["+parameters.get("query")+"]");
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
        requestObject.body = (String) exchange.getAttribute("body");
        return requestObject;
    }

    protected RequestObject preprocess(RequestObject request) {
        return request;
    }

    private Result createResponse(RequestObject requestObject, long rootEventId) {

        Result result = new StandardResult();
        if (mode == WEBSITE_MODE) {
            //System.out.println("requestObject.uri: "+requestObject.uri);
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

    public DateFormat getDateFormat() {
        return dateFormat;
    }
    
    public static String dumpRequest(RequestObject req){
        StringBuilder sb = new StringBuilder();
        sb.append("************** REQUEST ****************").append("\r\n");
        sb.append("URI:").append(req.uri).append("\r\n");
        sb.append("PATHEXT:").append(req.pathExt).append("\r\n");
        sb.append("METHOD:").append(req.method).append("\r\n");
        sb.append("ACCEPT:").append(req.acceptedResponseType).append("\r\n");
        sb.append("CLIENT IP:").append(req.clientIp).append("\r\n");
        sb.append("***BODY:").append("\r\n");
        sb.append(req.body).append("\r\n");
        sb.append("***BODY.").append("\r\n");
        sb.append("***HEADERS:").append("\r\n");
        req.headers.keySet().forEach(key -> {
            sb.append(key)
                    .append(":")
                    .append(req.headers.getFirst(key))
                    .append("\r\n");
        });
        sb.append("***HEADERS.").append("\r\n");
        sb.append("***PARAMETERS:").append("\r\n");
        req.parameters.keySet().forEach(key -> {
            sb.append(key)
                    .append(":")
                    .append(req.parameters.get(key))
                    .append("\r\n");
        });
        sb.append("***PARAMETERS.").append("\r\n");
        return sb.toString();
    }

}
