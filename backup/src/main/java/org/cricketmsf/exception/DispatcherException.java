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
package org.cricketmsf.exception;


/**
 *
 * @author greg
 */
public class DispatcherException extends Exception {
    
    public static int UNKNOWN = 999;
    public static int NOT_IMPLEMENTED = 998;
    public static int QUEUE_EXCEPTION = 997;
    public static int UNKNOWN_EVENT = 996;
    public static int QUEUE_CLIENT_NOT_DEFINED = 995;

    private int code = NOT_IMPLEMENTED;
    private String message;
    
    public DispatcherException(int code){
        this.code = code;
        switch (code){
            case 998:
                message = "operation not implemented";
                break;
            case 997:
                message = "queue client exception";
                break;
            case 996:
                message = "unknown event";
                break;
            case 995:
                message = "queue client not defined";
                break;
            case 999:
                message = "unknown error";
                break;
        }
    }
    
    public DispatcherException(int code, String message){
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
