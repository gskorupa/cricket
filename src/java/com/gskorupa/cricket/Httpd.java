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

import com.gskorupa.cricket.in.HttpAdapter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class Httpd {

    private Kernel service;
    public HttpServer server = null;

    public Httpd(Kernel service) {
        this.service = service;
        String host = service.getHost();
        if (null != host) {
            if (host.isEmpty() || "0.0.0.0".equals(host) || "*".equals(host)) {
                host = null;
            }
        }
        try {
            if (host == null) {
                server = HttpServer.create(new InetSocketAddress(service.getPort()), 0);
            } else {
                server = HttpServer.create(new InetSocketAddress(host, service.getPort()), 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpContext context;
        for (int i = 0; i < service.adapters.length; i++) {
            //if (service.adapters[i] instanceof com.sun.net.httpserver.HttpHandler) {
            if (service.adapters[i] instanceof com.gskorupa.cricket.in.HttpAdapter) {
                System.out.print("creating context: ");
                System.out.println(((HttpAdapter) service.adapters[i]).getContext());
                context = server.createContext(((HttpAdapter) service.adapters[i]).getContext(), (com.sun.net.httpserver.HttpHandler) service.adapters[i]);
                context.getFilters().add(new ParameterFilter());
            }
        }
    }

    public void run() {
        //Create a default executor
        server.setExecutor(null);
        server.start();
    }

}
