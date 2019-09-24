/*
 * Copyright 2019 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
public class EventException extends Exception {
    
    public static final int UNKNOWN = 999;
    public static final int NOT_IMPLEMENTED = 1;
    public static final int CATEGORY_ALREADY_DEFINED = 2;
    public static final int MUST_OVERRIDE_REGISTER = 3;
    public static final int MUST_EXTEND_DECORATOR = 4;

    private int code = NOT_IMPLEMENTED;
    private String message;
    
    public EventException(int code){
        this.code = code;
        switch (code){
            case 1:
                message = "operation not implemented";
                break;
            case 2:
                message = "event category already defined";
                break;
            case 3:
                message = "class does not override required method";
                break;
            case 4:
                message = "class does not extend EventDecoraor";
                break;
            case 999:
                message = "unknown error";
                break;
        }
    }
    
    public EventException(int code, String message){
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
