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
package org.cricketmsf.microsite.cms;

/**
 *
 * @author greg
 */
public class CmsException extends Exception {
    
    public static int UNSUPPORTED_DB_ADAPTER = 0;
    public static int UNSUPPORTED_FILE_ADAPTER = 1;
    public static int UNSUPPORTED_LOGGER_ADAPTER = 2;
    public static int NOT_INITIALIZED = 3;
    
    public static int UNSUPPORTED_STATUS = 10;
    public static int UNSUPPORTED_LANGUAGE = 11;
    public static int MALFORMED_UID = 12;
    
    public static int HELPER_EXCEPTION = 20;
    
    public static int NOT_FOUND = 404;
    public static int ALREADY_EXISTS = 409;
    
    public static int LANGUAGE_NOT_SUPPORTED = 600;
    public static int TRANSLATION_NOT_POSSIBLE = 601;
    public static int TRANSLATION_NOT_CONFIGURED = 603;
    
    public static int UNKNOWN = 1000;
    
    private String message;
    private int code;
    
    public CmsException(int code){
        this.code = code;
        switch (code){
            case 1000:
            default:
                message = "unknown error";
                break;
        }
    }
    
    public CmsException(int code, String message){
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
