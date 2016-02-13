/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gskorupa.cricket.out.db;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author greg
 */
public class LimitedMap extends LinkedHashMap{
    
    private int maxSize =0;
    
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return maxSize>0 ? size() > maxSize : false;
     }

    /**
     * @return the maxSize
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * @param maxSize the maxSize to set
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
    
}
