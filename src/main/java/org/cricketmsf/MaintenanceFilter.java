/*
 * Copyright 2016 Grzegorz Skorupa <g.skorupa at gmail.com>.
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

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import org.cricketmsf.in.http.HttpAdapter;

/**
 * This is default filter used to check required request conditions. It does
 * nothing but could be used as a starting point to implement required filter.
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class MaintenanceFilter extends Filter {

    private int errorCode = 200;
    private String errorMessage = "";

    @Override
    public String description() {
        return "Maintenance filter";
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain)
            throws IOException {
        String message;
        switch (Kernel.getInstance().getStatus()) {
            case Kernel.STARTING:
                    message = "System is starting. Try again later.";
                    exchange.sendResponseHeaders(HttpAdapter.SC_UNAVAILABLE, message.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(message.getBytes());
                    }
                    exchange.getResponseBody().close();
                    exchange.close();
                    break;
            case Kernel.SHUTDOWN:
                    message = "System shutdown in progress.";
                    exchange.sendResponseHeaders(HttpAdapter.SC_UNAVAILABLE, message.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(message.getBytes());
                    }
                    exchange.getResponseBody().close();
                    exchange.close();
                    break;
            case Kernel.MAINTENANCE:
                String[] paths = ((String) Kernel.getInstance().getProperties().getOrDefault("maintenance-paths", "")).toLowerCase().split(" ");
                boolean ok = false;
                for (int i = 0; i < paths.length; i++) {
                    if (exchange.getRequestURI().getPath().toLowerCase().startsWith(paths[i])) {
                        ok = true;
                        i=1000;
                    }
                }
                if (ok) {
                    chain.doFilter(exchange);
                } else {
                    message = "System in maintenance mode. Try again later.";
                    exchange.sendResponseHeaders(HttpAdapter.SC_UNAVAILABLE, message.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(message.getBytes());
                    }
                    exchange.getResponseBody().close();
                    exchange.close();
                }
                break;
            default:
                chain.doFilter(exchange);
                break;
        }
    }

}
