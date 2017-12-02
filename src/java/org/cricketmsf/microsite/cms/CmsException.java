/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
