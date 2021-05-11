/*
 * Copyright 2021 Grzegorz Skorupa .
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
package org.cricketmsf.out.db;

/**
 *
 * @author greg
 */
public class DBException extends Exception {
    
    public static int UNKNOWN = 999;
    public static int CANNOT_CREATE = 1;
    public static int CANNOT_DELETE = 2;
    public static int TABLE_NOT_EXISTS = 3;
    public static int CANNOT_WRITE = 4;
    public static int CANNOT_RESTORE = 5;
    public static int NOT_SUPPORTED = 6;
    public static int NOT_IMPLEMENTED = 7;
    
    private int code = UNKNOWN;
    private String message;
    
    public DBException(int code){
        this.code = code;
        switch (code){
            case 1:
                message = "unable to create table";
                break;
            case 2:
                message = "unable to delete key";
                break;
            case 3:
                message = "table does not exists";
                break;
            case 999:
                message = "unknown error";
                break;
        }
    }
    
    public DBException(int code, String message){
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
