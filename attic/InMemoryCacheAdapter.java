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
package com.gskorupa.cricket.out;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

/**
 *
 * @author greg
 */
public class InMemoryCacheAdapter extends OutboundAdapter implements KeyValueCacheAdapterIface {

    private HashMap cache=null;
    private String storagePath;
    private String envVariable;
    private String fileName;
    
    @Override
    public void start() {
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

    @Override
    public void destroy() {
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
    
    @Override
    public void put(String key, Object value) {
        getCache().put(key, value);
    }

    @Override
    public Object get(String key) {
        return getCache().get(key);
    }
    
    @Override
    public Object get(String key, Object defaultValue) {
        return getCache().containsKey(key) ? getCache().get(key) : defaultValue;
    }
    
    @Override
    public boolean containsKey(String key) {
        return getCache().containsKey(key);
    }
    
    @Override
    public boolean remove(String key){
        return getCache().remove(key)!=null ? true : false;
    }
    
    @Override
    public void clear(){
        getCache().clear();
    }
    
    @Override
    public long getSize(){
        return getCache().size();
    }

    private void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    private String getStoragePath() {
        return storagePath;
    }

    private void setEnvVariable(String envVariable) {
        this.envVariable = envVariable;
    }

    private String getEnvVariable() {
        return envVariable;
    }

    public void loadProperties(HashMap<String,String> properties) {
        setStoragePath(properties.get("path"));
        System.out.println("path: " + getStoragePath());
        setEnvVariable(properties.get("envVariable"));
        System.out.println("envVAriable name: " + getEnvVariable());
        if (System.getenv(getEnvVariable()) != null) {
            setStoragePath(System.getenv(getEnvVariable()));
        }
        // fix to handle '.'
        if(getStoragePath().startsWith(".")){
            setStoragePath(System.getProperty("user.dir")+getStoragePath().substring(1));
        }
        setFileName(properties.get("file"));
        System.out.println("file: " + getFileName());
        String pathSeparator = System.getProperty("file.separator");
        setStoragePath(
                getStoragePath().endsWith(pathSeparator)
                ? getStoragePath() + getFileName()
                : getStoragePath() + pathSeparator + getFileName()
        );
        System.out.println("cache file location: " + getStoragePath());
        start();
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
