/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cricketmsf.in.http;

import org.cricketmsf.in.http.Result;

/**
 *
 * @author greg
 */
public class StandardResult implements Result{
    
    private int code;
    private String message;
    private Object data;
    private byte[] payload;
    private String fileExtension;
    /**
     * @return the status code
     */
    public int getCode(){
        return code;
    }
    
    /**
     * @param code the status code to set
     */
    public void setCode(int code){
        this.code=code;
    }
    
    /**
     * @return the status message
     */
    public String getMessage(){
        return message;
    }
    
    /**
     * @param message the message to set
     */
    public void setMessage(String message){
        this.message=message;
    }
    
    /**
     * @return the data
     */
    public Object getData(){
        return data;
    }
    
    /**
     * @param data the data to set
     */
    public void setData(Object data){
        this.data=data;
    }
    
    public byte[] getPayload(){
        return payload;
    }
    
    public void setPayload(byte[] payload){
        this.payload=payload;
    }
    
    public String getFileExtension(){
        return fileExtension;
    }
    
    public void setFileExtension(String fileExt){
        this.fileExtension=fileExtension;
    }
    
}
