/*
 * Copyright 2016 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package com.gskorupa.cricket.db;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author greg
 */
public class KeyValueStore {

    private HashMap cache=null;
    private String storagePath;
    
    public void read() {
        try {
            try (XMLDecoder decoder = new XMLDecoder(
                    new BufferedInputStream(new FileInputStream(getStoragePath())
                    ))) {
                cache = (HashMap) decoder.readObject();
            }
        } catch (Exception e) {
            cache = new HashMap();
        }
    }

    public void write() {
        try {
            try (XMLEncoder encoder = new XMLEncoder(
                    new BufferedOutputStream(
                            new FileOutputStream(getStoragePath())))) {
                encoder.writeObject(cache);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HashMap getCache(){
        return cache!=null ? cache : new HashMap();
    }
    
    public void put(String key, Object value) {
        getCache().put(key, value);
    }

    public Object get(String key) {
        return getCache().get(key);
    }
    
    public Object get(String key, Object defaultValue) {
        return getCache().containsKey(key) ? getCache().get(key) : defaultValue;
    }
    
    public boolean containsKey(String key) {
        return getCache().containsKey(key);
    }
    
    public boolean remove(String key){
        return getCache().remove(key)!=null ? true : false;
    }
    
    public void clear(){
        getCache().clear();
    }
    
    public long getSize(){
        return getCache().size();
    }

    private void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    private String getStoragePath() {
        return storagePath;
    }

    public KeyValueStore(String storagePath) {
        setStoragePath(storagePath);
        read();
    }
    
    public Set getKeySet(){
        return cache.keySet();
    }
    
}
