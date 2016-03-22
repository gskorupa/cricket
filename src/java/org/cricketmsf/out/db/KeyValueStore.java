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
package org.cricketmsf.out.db;

import org.cricketmsf.out.OutboundAdapter;
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
public class KeyValueStore extends OutboundAdapter implements KeyValueCacheAdapterIface {

    private LimitedMap cache = null;
    private String storagePath;
    private int capacity = 0;
    private String envVariable;
    private String fileName;
    private boolean persistent=false;

    @Override
    public void start() {
        read();
    }

    @Override
    public void destroy() {
        if (isPersistent()) {
            write();
        }
    }

    public void loadProperties(HashMap<String, String> properties) {
        setStoragePath(properties.get("path"));
        System.out.println("path: " + getStoragePath());
        setEnvVariable(properties.get("envVariable"));
        System.out.println("envVAriable name: " + getEnvVariable());
        if (System.getenv(getEnvVariable()) != null) {
            setStoragePath(System.getenv(getEnvVariable()));
        }
        // fix to handle '.'
        if (getStoragePath().startsWith(".")) {
            setStoragePath(System.getProperty("user.dir") + getStoragePath().substring(1));
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
        try {
            setCapacity(Integer.parseInt(properties.get("max-records")));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        System.out.println("max-records: " + getCapacity());
        setPersistent(Boolean.parseBoolean("persistent"));
        System.out.println("persistent: " + isPersistent());
        start();
    }

    private void setEnvVariable(String envVariable) {
        this.envVariable = envVariable;
    }

    private String getEnvVariable() {
        return envVariable;
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

    public void read() {
        try {
            try (XMLDecoder decoder = new XMLDecoder(
                    new BufferedInputStream(new FileInputStream(getStoragePath())
                    ))) {
                cache = (LimitedMap) decoder.readObject();
            }
        } catch (Exception e) {
            cache = new LimitedMap();
        }
        cache.setMaxSize(capacity);
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

    private LimitedMap getCache() {
        if (cache != null) {
            return cache;
        } else {
            cache = new LimitedMap();
            cache.setMaxSize(capacity);
            return cache;
        }
    }

    public synchronized void put(String key, Object value) {
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

    public synchronized boolean remove(String key) {
        return getCache().remove(key) != null ? true : false;
    }

    public synchronized void clear() {
        getCache().clear();
    }

    public long getSize() {
        return getCache().size();
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    private String getStoragePath() {
        return storagePath;
    }

    public Set getKeySet() {
        return cache.keySet();
    }

    /**
     * @return the capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * @param capacity the capacity to set
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * @return the persistent
     */
    public boolean isPersistent() {
        return persistent;
    }

    /**
     * @param persistent the persistent to set
     */
    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

}
