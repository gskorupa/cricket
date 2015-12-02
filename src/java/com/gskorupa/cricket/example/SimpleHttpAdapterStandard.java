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
package com.gskorupa.cricket.example;

import com.gskorupa.cricket.Adapter;
import com.gskorupa.cricket.AdapterHook;
import com.gskorupa.cricket.RequestObject;
import com.gskorupa.cricket.RequestParameter;
import com.gskorupa.cricket.Service;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Properties;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class SimpleHttpAdapterStandard implements SimpleHttpAdapter, Adapter, HttpHandler {

    String context;
    String hookMethodName = null;

    public String getContext() {
        return context;
    }

    public void loadProperties(Properties properties) {
        context = properties.getProperty("SimpleHttpAdapter-context");
        System.out.println("context=" + context);
        getServiceHook();
        System.out.println("service hook name: " + hookMethodName);
    }

    private void getServiceHook() {
        AdapterHook ah;
        for (Method m : Service.getInstance().getClass().getMethods()) {
            ah = (AdapterHook) m.getAnnotation(AdapterHook.class);
            if (ah != null) {
                for (Class c : this.getClass().getInterfaces()) {
                    System.out.println(c.getSimpleName());
                    if (ah.handlerClassName().equals(c.getSimpleName())) {
                        hookMethodName = m.getName();
                        break;
                    }
                }
            }
        }
    }

    // tu trzeba też przekazać Service
    public void handle(HttpExchange exchange) throws IOException {
        //Create a response form the request query parameters
        URI uri = exchange.getRequestURI();
        String method = exchange.getRequestMethod();
        //
        String response = createResponse(uri, exchange.getHttpContext().getServer());

        //Set the response header status and length
        exchange.sendResponseHeaders(200, response.getBytes().length);
        
        //Write the response string
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();

    }

    private String createResponse(URI uri, HttpServer server) {
        String fName = "";
        String lName = "";
        //Get the request query
        RequestObject requestObject = new RequestObject();
        RequestParameter requestParameter = new RequestParameter();

        String query = uri.getQuery();
        if (query != null) {
            System.out.println("Query: " + query);
            String[] queryParams = query.split("&");
            if (queryParams.length > 0) {
                for (String qParam : queryParams) {
                    String[] param = qParam.split("=");
                    if (param.length > 0) {
                        for (int i = 0; i < param.length; i++) {
                            if ("name".equalsIgnoreCase(param[0])) {
                                fName = param[1];
                                requestParameter.name = "name";
                                requestParameter.value = fName;
                                requestObject.parameters.add(requestParameter);
                            }
                            if ("surename".equalsIgnoreCase(param[0])) {
                                lName = param[1];
                            }
                        }
                    }

                }
            }
        }
        String response = "Hello from " + Service.getInstance().getClass().getName();

        try {
            Method m = Service.getInstance().getClass().getMethod(hookMethodName, RequestObject.class);
            response = (String) m.invoke(Service.getInstance(),requestObject);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException o) {
            o.printStackTrace();
        } catch (NoSuchMethodException x) {
            x.printStackTrace();
        }
        return response;
    }

}
