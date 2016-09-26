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
    public SecurityFilterResult checkRequest(HttpExchange exchange) {
        // if we found problems analysing exchange object
        boolean problemDetected = false;
        
        SecurityFilterResult result = new SecurityFilterResult();
        if (problemDetected) {
            result.code = 403; // FORBIDDEN
            result.message = "request blocket by security filter\r\n";
        } else {
            result.code = 200;
            result.message = "";
        }
        return result;
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain)
            throws IOException {
        SecurityFilterResult result = checkRequest(exchange);
        if (result.code != 200) {
            exchange.sendResponseHeaders(result.code, result.message.length());
            exchange.getResponseBody().write(result.message.getBytes());
            exchange.getResponseBody().close();
        } else {
            chain.doFilter(exchange);
        }
    }

}
