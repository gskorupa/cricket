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
 * This is default filter used to check required request conditions. 
 * Does nothing.
 * Could be used as a starting point to implement required filter.
 * 
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class SecurityFilter extends Filter {
    
    private int errorCode=200;
    private String errorMessage="";

    @Override
    public String description() {
        return "Default security filter";
    }
    
    public String getErrorMessage(){
        return errorMessage;
    }
    
    public int getErrorCode(){
        return errorCode;
    }
    
    /**
     * Does request analysis 
     * 
     * @param exchange  request object
     * @return 
     */
    public boolean isProblemDetected(HttpExchange exchange){
        // if we found problems analysing exchange object 
        if(true){
            setErrorCode(500);
            setErrorMessage("");
            return true;
        }else{
            setErrorCode(200);
            setErrorMessage("");
            return false;
        }
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain)
            throws IOException {
        //System.out.println(this.getClass().getSimpleName());
        if (isProblemDetected(exchange)) {
            String message=getErrorMessage();
            exchange.sendResponseHeaders(getErrorCode(), message.length());
            exchange.getResponseBody().write(message.getBytes());
            exchange.getResponseBody().close();
        } else {
            chain.doFilter(exchange);
        }
    }

    /**
     * @param errorCode the errorCode to set
     */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
