/*
 * Copyright 2015-2020 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.cricketmsf.Stopwatch;
import org.cricketmsf.in.InboundAdapterIface;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class HttpAdapter
        extends InboundAdapter
        implements HttpAdapterIface, HttpHandler, InboundAdapterIface/*, org.eclipse.jetty.server.Handler*/ {

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
    public final static int SC_UNAVAILABLE = 503;

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
    private boolean extendedResponse = false;
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
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
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
                }
            }
        }
    }

    @Override
    //public synchronized void handle(HttpExchange exchange) throws IOException {
        //Kernel.getInstance().getThreadFactory().newThread(() -> {
        //    try {
        //        doHandle(exchange, Kernel.getEventId());
        //    } catch (NullPointerException | IOException e) {
        //        Kernel.getInstance().dispatchEvent(Event.logFinest(this.getClass().getSimpleName() + ".handle()", e.getMessage()));
        //    } catch (Exception e) {
        //        e.printStackTrace();
        //        Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName() + ".handle()", exchange.getRequestURI().getPath()));
        //    }
        //}, this.getName()).start();
    //}
    //public synchronized void doHandle(HttpExchange exchange, long rootEventId) throws IOException {
    public synchronized void handle(HttpExchange exchange) throws IOException {
        long rootEventId=Kernel.getEventId();
        try {
            Stopwatch timer = null;
            if (Kernel.getInstance().isFineLevel()) {
                timer = new Stopwatch();
            }
            String acceptedResponseType = JSON;
            try {
                acceptedResponseType
                        = acceptedTypesMap.getOrDefault(exchange.getRequestHeaders().get("Accept").get(0), JSON);
            } catch (Exception e) {
            }
            // cerating Result object
            Result result = createResponse(buildRequestObject(exchange, acceptedResponseType), rootEventId);
            acceptedResponseType = setResponseType(acceptedResponseType, result.getFileExtension());
            //set content type and print response to string format as JSON if needed
            Headers headers = exchange.getResponseHeaders();
            byte[] responseData;
            Iterator it = result.getHeaders().keySet().iterator();
            String key;
            while (it.hasNext()) {
                key = (String) it.next();
                List<String> values = result.getHeaders().get(key);
                for (int i = 0; i < values.size(); i++) {
                    headers.set(key, values.get(i));
                }
            }
            switch (result.getCode()) {
                case SC_MOVED_PERMANENTLY:
                case SC_MOVED_TEMPORARY:
                    if (!headers.containsKey("Location")) {
                        String newLocation = result.getMessage() != null ? result.getMessage() : "/";
                        headers.set("Location", newLocation);
                        responseData = ("moved to ".concat(newLocation)).getBytes("UTF-8");
                    } else {
                        responseData = "".getBytes();
                    }
                    break;
                case SC_NOT_FOUND:
                    headers.set("Content-type", "text/html");
                    responseData = result.getPayload();
                    break;
                default:
                    if (!headers.containsKey("Content-type")) {
                        if (acceptedTypesMap.containsKey(acceptedResponseType)) {
                            headers.set("Content-type", acceptedResponseType.concat("; charset=UTF-8"));
                            responseData = formatResponse(acceptedResponseType, result);
                        } else {
                            headers.set("Content-type", getMimeType(result.getFileExtension()));
                            responseData = result.getPayload();
                        }
                    } else {
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
                    } else if (exchange.getRequestURI().getPath().startsWith("/api/")) { //TODO: this is workaround
                        CorsProcessor.getResponseHeaders(headers, exchange.getRequestHeaders(), Kernel.getInstance().getCorsHeaders());
                    } else if (exchange.getRequestURI().getPath().endsWith(".tag")) { //TODO: this is workaround
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
                    break;
            }

            //TODO: format logs to have clear info about root event id
            if (Kernel.getInstance().isFineLevel()) {
                Kernel.getInstance().dispatchEvent(
                        Event.logFinest("HttpAdapter", "event " + rootEventId + " processing takes " + timer.time(TimeUnit.MILLISECONDS) + "ms")
                );
            }

            if (responseData.length > 0) {
                exchange.sendResponseHeaders(result.getCode(), responseData.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseData);
                }
            } else {
                exchange.sendResponseHeaders(result.getCode(), -1);
            }
            sendLogEvent(exchange, responseData.length);
            result = null;
        } catch (IOException e) {
            //e.printStackTrace();
            Kernel.getInstance().dispatchEvent(Event.logWarning(this, exchange.getRequestURI().getPath() + " " + e.getMessage()));
        }
        exchange.close();
    }

    private String getMimeType(String fileExt) {
        if(null==fileExt){
            return TEXT;
        }
        switch (fileExt.toLowerCase()) {
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
            case ".csv":
                return "text/csv";
            case ".js":
                return "text/javascript";
            case ".svg":
                return "image/svg+xml";
            case ".htm":
            case ".html":
                return "text/html; charset=utf-8";
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
                formattedResponse = JsonFormatter.getInstance().format(true, isExtendedResponse() ? result : result.getData());
                break;
            case XML:
                //TODO: extended response is not possible because of "java.util.List is an interface, and JAXB can't handle interfaces"
                formattedResponse = XmlFormatter.getInstance().format(true, result.getData());
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

        try {
            r = formattedResponse.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Kernel.getInstance().dispatchEvent(Event.logSevere("HttpAdapter.formatResponse()", e.getClass().getSimpleName() + " " + e.getMessage()));
        }
        return r;
    }

    RequestObject buildRequestObject(HttpExchange exchange, String acceptedResponseType) {
        // Remember that "parameters" attribute is created by filter
        Map<String, Object> parameters = (Map<String, Object>) exchange.getAttribute("parameters");
        String method = exchange.getRequestMethod();
        String pathExt = exchange.getRequestURI().getPath();
        if (null != pathExt) {
            pathExt = pathExt.substring(exchange.getHttpContext().getPath().length());
            while (pathExt.startsWith("/")) {
                pathExt = pathExt.substring(1);
            }
        }

        RequestObject requestObject = new RequestObject();
        requestObject.method = method;
        requestObject.parameters = parameters;
        requestObject.uri = exchange.getRequestURI().toString();
        requestObject.pathExt = pathExt;
        requestObject.headers = exchange.getRequestHeaders();
        requestObject.clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        requestObject.acceptedResponseType = acceptedResponseType;
        requestObject.body = (String) exchange.getAttribute("body");
        if(null==requestObject.body){
            requestObject.body=(String)parameters.getOrDefault("&&&data","");
        }
        return requestObject;
    }

    protected RequestObject preprocess(RequestObject request) {
        return request;
    }

    private Result createResponse(RequestObject requestObject, long rootEventId) {

        if (null != properties.get("dump-request") && "true".equalsIgnoreCase(properties.get("dump-request"))) {
            Kernel.getInstance().getLogger().print(dumpRequest(requestObject));
        }
        String methodName = null;
        Result result = new StandardResult();
        if (mode == WEBSITE_MODE) {
            if (!requestObject.uri.endsWith("/")) {
                if (requestObject.uri.lastIndexOf("/") > requestObject.uri.lastIndexOf(".")) {
                    // redirect to index.file but only if property index.file is not null
                    result.setCode(SC_MOVED_PERMANENTLY);
                    result.setMessage(requestObject.uri.concat("/"));
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
            event.setRootEventId(rootEventId);
            event.setPayload(requestObject);
            Method m = Kernel.getInstance().getClass().getMethod(hookMethodName, Event.class);
            methodName = m.getName();
            result = (Result) m.invoke(Kernel.getInstance(), event);
        } catch (NoSuchMethodException e) {
            sendLogEvent(Event.LOG_SEVERE, "handler method NoSuchMethodException " + hookMethodName + " " + e.getMessage());
            result.setCode(SC_INTERNAL_SERVER_ERROR);
            result.setMessage("handler method error");
            result.setFileExtension(null);
        } catch (IllegalAccessException e) {
            sendLogEvent(Event.LOG_SEVERE, "handler method IllegalAccessException " + hookMethodName + " " + e.getMessage());
            result.setCode(SC_INTERNAL_SERVER_ERROR);
            result.setMessage("handler method error");
            result.setFileExtension(null);
        } catch (InvocationTargetException e) {
            sendLogEvent(Event.LOG_SEVERE, "handler method InvocationTargetException " + hookMethodName + " " + e.getMessage());
            result.setCode(SC_INTERNAL_SERVER_ERROR);
            result.setMessage("handler method error");
            result.setFileExtension(null);
        }
        if (null == result) {
            result = new StandardResult("null result returned by the service");
            result.setCode(HttpAdapter.SC_INTERNAL_SERVER_ERROR);
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
        if ("HEAD".equalsIgnoreCase(requestMethod)) {
            result = hookMethodNames.get("GET");
        } else {
            result = hookMethodNames.get(requestMethod);
        }
        if (null == result) {
            result = hookMethodNames.get("*");
        }
        return result;
    }

    protected void sendLogEvent(HttpExchange exchange, int length) {
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
        Kernel.getInstance().dispatchEvent(event);
    }

    protected void sendLogEvent(String type, String message) {
        Event event = new Event(
                "HttpAdapter",
                Event.CATEGORY_LOG,
                type,
                null,
                message);
        Kernel.getInstance().dispatchEvent(event);
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

    public static String dumpRequest(RequestObject req) {
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

    /*
    @Override
    public void handle(String string, Request rqst, HttpServletRequest hsr, HttpServletResponse hsr1) throws IOException, ServletException {
        Stopwatch timer = null;
        if (Kernel.getInstance().isFineLevel()) {
            timer = new Stopwatch();
        }
        Result result = new StandardResult();
        boolean problemFound = false;
        if (mode == WEBSITE_MODE) {
            if (!rqst.getRequestURI().endsWith("/")) {
                if (rqst.getRequestURI().lastIndexOf("/") > rqst.getPathInfo().lastIndexOf(".")) {
                    // redirect to index.file but only if property index.file is not null
                    hsr1.sendRedirect(rqst.getRequestURI().concat("/"));

                    return;
                }
            }
        }

        String hookMethodName = getHookMethodNameForMethod(rqst.getMethod());
        if (hookMethodName == null) {
            sendLogEvent(Event.LOG_WARNING, "hook method is not defined for " + rqst.getMethod());
            hsr1.sendError(SC_METHOD_NOT_ALLOWED, rqst.getMethod() + " is not allowed");
            return;
        }

        String methodName = null;
        long rootEventID = Kernel.getEventId();
        try {
            sendLogEvent(Event.LOG_FINE, "sending request to hook method " + hookMethodName);
            Event event = new Event("HttpAdapter", new RequestObject(rqst));
            event.setRootEventId(rootEventID);
            event.setPayload(rqst);
            Method m = Kernel.getInstance().getClass().getMethod(hookMethodName, Event.class);
            methodName = m.getName();
            result = (Result) m.invoke(Kernel.getInstance(), event);
        } catch (NoSuchMethodException e) {
            sendLogEvent(Event.LOG_SEVERE, "handler method NoSuchMethodException " + hookMethodName + " " + e.getMessage());
            hsr1.sendError(SC_INTERNAL_SERVER_ERROR, "handler method error");
            return;
        } catch (IllegalAccessException e) {
            sendLogEvent(Event.LOG_SEVERE, "handler method IllegalAccessException " + hookMethodName + " " + e.getMessage());
            hsr1.sendError(SC_INTERNAL_SERVER_ERROR, "handler method error");
            return;
        } catch (InvocationTargetException e) {
            sendLogEvent(Event.LOG_SEVERE, "handler method InvocationTargetException " + hookMethodName + " " + e.getMessage());
            hsr1.sendError(SC_INTERNAL_SERVER_ERROR, "handler method error");
            return;
        }
        if (null == result) {
            hsr1.sendError(SC_INTERNAL_SERVER_ERROR, "null result returned by the service");
            return;
        }
        if (result.getCode() == SC_BAD_REQUEST) {
            hsr1.sendError(SC_BAD_REQUEST, methodName + " " + result.getMessage() + " " + result.getData());
            return;
        }

        //create response
        hsr1.setStatus(result.getCode());
        String acceptedResponseType = rqst.getHeader("Accept");
        if (acceptedTypesMap.containsKey(acceptedResponseType)) {
            hsr1.setContentType(acceptedResponseType.concat("; charset=UTF-8"));
            hsr1.getWriter().println(new String(formatResponse(acceptedResponseType, result)));
        } else {
            hsr1.setContentType(getMimeType(result.getFileExtension()));
            hsr1.getWriter().println(new String(result.getPayload()));
        }
        if (Kernel.getInstance().isFineLevel()) {
            Kernel.getInstance().dispatchEvent(
                    Event.logInfo("HttpAdapter", "event " + rootEventID + " processing takes " + timer.time(TimeUnit.MILLISECONDS) + "ms")
            );
        }
        rqst.setHandled(true);
    }

    @Override
    public void setServer(Server server) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Server getServer() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void start() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void stop() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isRunning() {
        return true;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isStarted() {
        return true;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isStarting() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isStopping() {
        return false;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isStopped() {
        return false;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isFailed() {
        return false;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addLifeCycleListener(Listener ll) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeLifeCycleListener(Listener ll) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
*/
}
