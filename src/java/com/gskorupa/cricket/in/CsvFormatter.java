/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gskorupa.cricket.in;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class CsvFormatter {

    private static CsvFormatter instance = null;

    public static CsvFormatter getInstance() {
        if (instance != null) {
            return instance;
        } else {
            instance = new CsvFormatter();
            return instance;
        }
    }

    public String format(Result r) {
        StringBuilder sb=new StringBuilder("");
        sb.append("csv formatter not implemented");
        return sb.toString();
    }
    
}
