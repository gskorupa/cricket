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
package com.gskorupa.cricket;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public abstract class HttpAdapter implements Adapter, HttpHandler {
    
    public final static int SC_OK=200;
    public final static int SC_ACCEPTED=202;
    public final static int SC_CREATED=201;
    
    public final static int SC_NOT_MODIFIED=304;
    
    public final static int SC_BAD_REQUEST=400;
    public final static int SC_FORBIDDEN=403;
    public final static int SC_NOT_FOUND=404;
    public final static int SC_METHOD_NOT_ALLOWED=405;
    public final static int SC_CONFLICT=409;
    
    public final static int SC_INTERNAL_SERVER_ERROR=500;
    public final static int SC_NOT_IMPLEMENTED=501;

    private String context;
    private String hookMethodName = null;
    private HashMap<String, String> hookMethodNames = new HashMap();

    public String getContext() {
        return context;
    }

    public abstract void loadProperties(Properties properties);

    protected void getServiceHooks() {
        AdapterHook ah;
        String requestMethod;
        // for every method of a Service instance (our service class extending Service)
        for (Method m : Service.getInstance().getClass().getMethods()) {
            ah = (AdapterHook) m.getAnnotation(AdapterHook.class);
            // we search for annotated method
            if (ah != null) {
                requestMethod = ah.requestMethod();
                // 'this' is a handler class loaded according to configuration described in propertis
                // file
                // we need to find all names of implemented interfaces because
                // handler class is mapped by the interface name
                for (Class c : this.getClass().getInterfaces()) {
                    //
                    if (ah.handlerClassName().equals(c.getSimpleName())) {
                        //System.out.println(ah.handlerClassName() + " " + c.getSimpleName());
                        //setHookMethodName(m.getName());
                        addHookMethodNameForMethod(requestMethod, m.getName());
                        System.out.println("hook method for " + requestMethod + " : " + m.getName());
                        break;
                    }
                }
            }
        }
    }

    // tu trzeba też przekazać Service
    public void handle(HttpExchange exchange) throws IOException {
        String responseFormat = "text";
        for (String v : exchange.getRequestHeaders().get("Accept")) {
            if ("application/json".equalsIgnoreCase(v)) {
                responseFormat = "json";
            } else if ("text/xml".equalsIgnoreCase(v)) {
                responseFormat = "xml";
            } else {
                responseFormat = "text";
            }
        }

        Result result = createResponse(exchange);

        //set content type and print response to string format as JSON if needed
        Headers headers = exchange.getResponseHeaders();
        String stringResponse;
        //if (result.getCode() == 0) {
            if ("json".equals(responseFormat)) {
                headers.set("Content-Type", "application/json; charset=UTF-8");
                stringResponse = JsonFormatter.getInstance().format(true, result);
            } else if ("xml".equals(responseFormat)) {
                headers.set("Content-Type", "text/xml; charset=UTF-8");
                stringResponse = result.toString();
                //stringResponse=XmlFormatter.getInstance().format(true, result);
            } else {
                headers.set("Content-Type", "text/csv; charset=UTF-8");
                stringResponse = result.toString();
            }
        //} else {
          //  headers.set("Content-Type", "text/html; charset=UTF-8");
            //stringResponse = result.toString();
        //}

        //calculate error code from response object
        int errCode = 200;
        switch (result.getCode()) {
            case 0:
                errCode = 200;
                break;
            default:
                errCode = result.getCode();
                break;
        }

        exchange.sendResponseHeaders(errCode, stringResponse.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(stringResponse.getBytes());
        os.close();
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
        if (null == hookMethodName) {
            hookMethodName = getHookMethodNameForMethod("*");
        }

        try {
            System.out.println("sending request to hook method " + getHookMethodNameForMethod(method));
            Method m = Service.getInstance().getClass().getMethod(getHookMethodNameForMethod(method), RequestObject.class);
            result = (Result) m.invoke(Service.getInstance(), requestObject);
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

}
