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
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public abstract class HttpAdapter implements Adapter, HttpHandler {

    private String context;
    private String hookMethodName = null;

    public String getContext() {
        return context;
    }

    public abstract void loadProperties(Properties properties);

    protected void getServiceHook() {
        AdapterHook ah;
        for (Method m : Service.getInstance().getClass().getMethods()) {
            ah = (AdapterHook) m.getAnnotation(AdapterHook.class);
            if (ah != null) {
                for (Class c : this.getClass().getInterfaces()) {
                    System.out.println("interface "+c.getSimpleName());
                    if (ah.handlerClassName().equals(c.getSimpleName())) {
                        System.out.println(ah.handlerClassName()+" "+c.getSimpleName());
                        setHookMethodName(m.getName());
                        break;
                    }
                }
            }
        }
    }

    // tu trzeba też przekazać Service
    public void handle(HttpExchange exchange) throws IOException {
        boolean useJson=false;
        for(String v: exchange.getRequestHeaders().get("Accept")){
            if("application/json".equalsIgnoreCase(v)){
                useJson=true;
            }
        }
        //Create a response form the request query parameters    
        /*
        Headers h = exchange.getResponseHeaders();
        if(useJson){
            h.set("Content-Type","application/json; charset=UTF-8");
        }else{
            h.set("Content-Type","text/plain; charset=UTF-8");
        }
        */
        Result response = createResponse(exchange);
        
        //set content type and print response to string format as JSON if needed
        Headers headers = exchange.getResponseHeaders();
        String stringResponse;
        if (useJson) {
            headers.set("Content-Type","application/json; charset=UTF-8");
            stringResponse=JsonFormatter.getInstance().format(true, response);
        } else {
            headers.set("Content-Type","text/plain; charset=UTF-8");
            stringResponse = response.toString();
        }

        //calculate error code from response object
        int errCode=200;
        switch(response.getCode()){
            case 0:
                errCode=200;
                break;
            default:
                errCode=response.getCode();
                break;
        }
        
        //Set the response header status and length, write response
        exchange.sendResponseHeaders(errCode, stringResponse.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(stringResponse.getBytes());
        os.close();

    }

    private Result createResponse(HttpExchange exchange) {

        Map<String, Object> parameters=(Map<String, Object>)exchange.getAttribute("parameters");
        String method=exchange.getRequestMethod();
        String adapterContext=exchange.getHttpContext().getPath();
        String pathExt=exchange.getRequestURI().getPath();
        if(null!=pathExt){
            pathExt=pathExt.substring(adapterContext.length());
            if(pathExt.startsWith("/")){
                pathExt=pathExt.substring(1);
            }
        }

        //
        RequestObject requestObject = new RequestObject();
        requestObject.method = method;
        requestObject.parameters=parameters;
        requestObject.pathExt=pathExt;

        Result response = null;
        try {
            Method m = Service.getInstance().getClass().getMethod(getHookMethodName(), RequestObject.class);
            response = (Result) m.invoke(Service.getInstance(), requestObject);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException o) {
            o.printStackTrace();
        } catch (NoSuchMethodException x) {
            x.printStackTrace();
        }
        return response;
    }

    /**
     * @param context the context to set
     */
    public void setContext(String context) {
        this.context = context;
    }

    /**
     * @return the hookMethodName
     */
    public String getHookMethodName() {
        return hookMethodName;
    }

    /**
     * @param hookMethodName the hookMethodName to set
     */
    public void setHookMethodName(String hookMethodName) {
        this.hookMethodName = hookMethodName;
    }

}
