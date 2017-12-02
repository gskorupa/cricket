/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cricketmsf.microsite.user;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;

/**
 *
 * @author grzesk
 */


public class HashMaker {
    
    public static void main(String args[]){
        System.out.println(HashMaker.md5Java(args[0]));
    }

    public static String md5Java(String message) {
        String digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(message.getBytes("UTF-8"));
            //converting byte array to Hexadecimal String 
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            digest = sb.toString();
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
            Kernel.handle(Event.logSevere(HashMaker.class.getSimpleName(), ex.getMessage()));
        }
        return digest;

    }

}
