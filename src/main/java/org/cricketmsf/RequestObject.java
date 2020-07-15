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

import com.sun.net.httpserver.Headers;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import org.eclipse.jetty.server.Request;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class RequestObject {

    public String clientIp = null;
    public String method = null;
    public String uri = null;
    public String pathExt = null;
    public Headers headers = null;
    public Map<String, Object> parameters = new HashMap<>();
    public String acceptedResponseType = null;
    public String body = null;
    //public Request request = null;

    /*
    public RequestObject(Request request) {
        this.request = request;
        clientIp = request.getRemoteInetSocketAddress().getAddress().getHostAddress();
        method = request.getMethod();
        uri = request.getRequestURI();
        pathExt = request.getPathInfo();
        headers = new Headers();
        List headerNames = Collections.list(request.getHeaderNames());
        String hn;
        for (int i = 0; i < headerNames.size(); i++) {
            hn = (String) headerNames.get(i);
            headers.add(hn, request.getHeader(hn));
        }
        acceptedResponseType = headers.getFirst("Accept");
        List pl = Collections.list(request.getParameterNames());
        parameters = new HashMap<>();
        for (int i = 0; i < pl.size(); i++) {
            parameters.put((String) pl.get(i), request.getParameterValues((String) pl.get(i)));
            System.out.println((String) pl.get(i));
        }
        try {
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            body = buffer.toString();
        } catch (IOException e) {
        }
    }
*/

    public RequestObject() {
    }
}
