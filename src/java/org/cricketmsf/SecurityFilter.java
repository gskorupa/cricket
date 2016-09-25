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

/**
 * This is default filter used to check required request conditions. Does
 * nothing. Could be used as a starting point to implement required filter.
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class SecurityFilter extends Filter {

    private int errorCode = 200;
    private String errorMessage = "";

    @Override
    public String description() {
        return "Default security filter";
    }

    /**
     * Does request analysis
     *
     * @param exchange request object
     * @return
     */
    public boolean isProblemDetected(HttpExchange exchange) {
        // if we found problems analysing exchange object
        boolean problemDetected = false;
        if (problemDetected) {
            errorCode = 403;
            errorMessage = "request blocket by security filter\r\n";
            return true;
        } else {
            errorCode = 200;
            errorMessage = "";
            return false;
        }
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain)
            throws IOException {
        if (isProblemDetected(exchange)) {
            exchange.sendResponseHeaders(errorCode, errorMessage.length());
            exchange.getResponseBody().write(errorMessage.getBytes());
            exchange.getResponseBody().close();
        } else {
            chain.doFilter(exchange);
        }
    }

}
