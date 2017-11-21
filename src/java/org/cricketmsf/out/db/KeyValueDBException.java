/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cricketmsf.out.db;

/**
 *
 * @author greg
 */
public class KeyValueDBException extends Exception {
    
    public static int UNKNOWN = 999;
    public static int CANNOT_CREATE = 1;
    public static int CANNOT_DELETE = 2;
    public static int TABLE_NOT_EXISTS = 3;
    public static int CANNOT_WRITE = 4;
    
    private int code = UNKNOWN;
    private String message;
    
    public KeyValueDBException(int code){
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
    
    public KeyValueDBException(int code, String message){
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
