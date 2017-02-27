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
package org.cricketmsf;

import org.cricketmsf.in.http.HttpAdapter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class Httpd {

    public HttpServer server = null;

    public Httpd(Kernel service) {
        String host = service.getHost();
        int backlog = 0;
        try{
            backlog = Integer.parseInt((String)service.getProperties().getOrDefault("threads","0"));
        }catch(NumberFormatException | ClassCastException e){
            
        }
        if (null != host) {
            if (host.isEmpty() || "0.0.0.0".equals(host) || "*".equals(host)) {
                host = null;
            }
        }
        try {
            if (host == null) {
                server = HttpServer.create(new InetSocketAddress(service.getPort()), backlog);
            } else {
                server = HttpServer.create(new InetSocketAddress(host, service.getPort()), backlog);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpContext context;
        for (Map.Entry<String, Object> adapterEntry : service.getAdaptersMap().entrySet()) {
            if(adapterEntry.getValue() instanceof org.cricketmsf.in.http.HttpAdapter){
                Kernel.getLogger().print("context: "+((HttpAdapter) adapterEntry.getValue()).getContext());
                context = server.createContext(((HttpAdapter) adapterEntry.getValue()).getContext(), (com.sun.net.httpserver.HttpHandler) adapterEntry.getValue());
                context.getFilters().add(new ParameterFilter());
                context.getFilters().add(service.getSecurityFilter());
            }
        }
    }

    public void run() {
        //Create a default executor
        server.setExecutor(null);
        server.start();
    }

}
