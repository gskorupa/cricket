/*
 * Copyright 2017 Grzegorz Skorupa .
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
package org.cricketmsf.microsite.out.auth;

/**
 *
 * @author greg
 */
public class AuthException extends Exception {
    
    public static int ACCESS_DENIED = 403;
    public static int UNAUTHORIZED = 1;
    public static int EXPIRED = 401;
    
    public static int HELPER_NOT_AVAILABLE = 100;
    public static int HELPER_EXCEPTION = 101;
    
    public static int UNKNOWN = 1000;
    
    private String message;
    private int code;
    
    public AuthException(int code){
        this.code = code;
        switch (code){
            case 1000:
            default:
                message = "unknown error";
                break;
        }
    }
    
    public AuthException(int code, String message){
        this.code = code;
        this.message = message;
    }
    
    public String getMessage(){
        return message;
    }
    
    public int getCode(){
        return code;
    }
}
