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
    
    public static Headers getResponseHeaders(Headers responseHeaders, Headers requestHeaders, ArrayList corsConfig){
        HttpHeader h;
        for(int i=0; i<corsConfig.size(); i++){
            h = (HttpHeader) corsConfig.get(i);
            responseHeaders.set(h.name, h.value);
        }
        return responseHeaders;
    }
    
}
