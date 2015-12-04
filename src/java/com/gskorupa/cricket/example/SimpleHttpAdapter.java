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
import com.gskorupa.cricket.HttpAdapter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class SimpleHttpAdapter extends HttpAdapter implements SimpleHttpAdapterIface, Adapter, HttpHandler {

    public void loadProperties(Properties properties) {
        setContext(properties.getProperty("SimpleHttpAdapterIface-context"));
        System.out.println("context=" + getContext());
        getServiceHook();
        System.out.println("service hook name: " + getHookMethodName());
    }
    
    /*
    String context;
    String hookMethodName = null;

    public void loadProperties(Properties properties) {
        context = properties.getProperty("SimpleHttpAdapterIface-context");
        System.out.println("context=" + context);
        super.getServiceHook();
        hookMethodName=super.getHookMethodName();
        //context=super.getContext();
        System.out.println("service hook name: " + hookMethodName);
    }

    // 
    public void handle(HttpExchange exchange) throws IOException {
        super.handle(exchange);
    }
    
    public String getContext(){
        return context;
    }
*/
    
}
