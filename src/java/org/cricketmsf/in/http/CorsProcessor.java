/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
        boolean withCredentials = "true".equals(requestHeaders.getFirst("Access-Control-Allow-Credentials"));
        HttpHeader h;
        if (!withCredentials) {
            for (int i = 0; i < corsConfig.size(); i++) {
                h = (HttpHeader) corsConfig.get(i);
                responseHeaders.set(h.name, h.value);
            }
        }else{
            for (int i = 0; i < corsConfig.size(); i++) {
                h = (HttpHeader) corsConfig.get(i);
                if("Access-Control-Allow-Origin".equals(h.name)){
                    if("*".equals(h.value)){
                        responseHeaders.set(h.name, origin);
                    }else{
                        responseHeaders.set(h.name, h.value);
                    }
                }else{
                    responseHeaders.set(h.name, h.value);
                }
            }
        }
        return responseHeaders;
    }

}
