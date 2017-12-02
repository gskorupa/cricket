/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cricketmsf.microsite.out.user;

/**
 *
 * @author greg
 */
public class UserException extends Exception {
    
    public static int USER_ALREADY_EXISTS = 0;
    public static int UNKNOWN_USER = 1;
    
    public static int HELPER_NOT_AVAILABLE = 100;
    public static int HELPER_EXCEPTION = 101;
    
    public static int UNKNOWN = 1000;
    
    private String message;
    private int code;
    
    public UserException(int code){
        this.code = code;
        switch (code){
            case 1000:
            default:
                message = "unknown error";
                break;
        }
    }
    
    public UserException(int code, String message){
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
