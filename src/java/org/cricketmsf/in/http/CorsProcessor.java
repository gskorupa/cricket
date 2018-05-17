/*
 * Copyright 2017 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package org.cricketmsf.in.http;

import com.sun.net.httpserver.Headers;
import java.util.ArrayList;
import org.cricketmsf.config.HttpHeader;

/**
 *
 * @author greg
 */
public class CorsProcessor {

    public static Headers getResponseHeaders(Headers responseHeaders, Headers requestHeaders, ArrayList corsConfig) {
        String origin = requestHeaders.getFirst("Origin");
        if(origin==null){
            origin = requestHeaders.getFirst("Referer"); //TODO: this is workaround - see HttpAdapter 188
        }
        if(origin==null){
            
        }
        // boolean withCredentials = "true".equals(requestHeaders.getFirst("Access-Control-Allow-Credentials"));
        HttpHeader h;
        //if (!withCredentials) {
        //    for (int i = 0; i < corsConfig.size(); i++) {
        //        h = (HttpHeader) corsConfig.get(i);
        //        responseHeaders.set(h.name, h.value);
        //    }
        //}else{
            for (int i = 0; i < corsConfig.size(); i++) {
                h = (HttpHeader) corsConfig.get(i);
                if("Access-Control-Allow-Origin".equals(h.name)){
                    if("*".equals(h.value) && origin!=null){
                        responseHeaders.set(h.name, origin);
                    }else{
                        responseHeaders.set(h.name, h.value);
                    }
                }else{
                    responseHeaders.set(h.name, h.value);
                }
            }
        //}
        return responseHeaders;
    }

}
