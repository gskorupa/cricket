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

import org.cricketmsf.out.db.KeyValueCacheAdapterIface;
import org.cricketmsf.out.OutboundAdapter;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cricketmsf.Adapter;
import org.cricketmsf.Kernel;
import org.cricketmsf.out.db.ComparatorIface;
import org.cricketmsf.out.db.LimitedMap;

/**
 *
 * @author greg
 */
public class KeyValueStore extends OutboundAdapter implements KeyValueCacheAdapterIface, Adapter {

    private LimitedMap cache = null;
    private String storagePath;
    private int capacity = 0;
    private String envVariable;
    private String fileName;
    private boolean persistent = false;

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

    /**
     * This method is executed while adapter is instantiated during the service
     * start. It's used to configure the adapter according to the configuration.
     *
     * @param properties map of properties readed from the configuration file
     * @param adapterName name of the adapter set in the configuration file (can
     * be different from the interface and class name.
     */
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        setStoragePath(properties.get("path"));
        Kernel.getInstance().getLogger().print("\tpath: " + getStoragePath());
        setEnvVariable(properties.get("envVariable"));
        Kernel.getInstance().getLogger().print("\tenvVAriable name: " + getEnvVariable());
        if (System.getenv(getEnvVariable()) != null) {
            setStoragePath(System.getenv(getEnvVariable()));
        }
        // fix to handle '.'
        if (getStoragePath().startsWith(".")) {
            setStoragePath(System.getProperty("user.dir") + getStoragePath().substring(1));
        }
        setFileName(properties.get("file"));
        Kernel.getInstance().getLogger().print("\tfile: " + getFileName());
        String pathSeparator = System.getProperty("file.separator");
        setStoragePath(
                getStoragePath().endsWith(pathSeparator)
                ? getStoragePath() + getFileName()
                : getStoragePath() + pathSeparator + getFileName()
        );
        Kernel.getInstance().getLogger().print("\tcache file location: " + getStoragePath());
        try {
            setCapacity(Integer.parseInt(properties.get("max-records")));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        Kernel.getInstance().getLogger().print("\tmax-records: " + getCapacity());
        setPersistent(Boolean.parseBoolean(properties.get("persistent")));
        Kernel.getInstance().getLogger().print("\tpersistent: " + isPersistent());
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

    /**
     * Reads serialized database from disk
     */
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

    /**
     * Writes serialized database to disk
     */
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
    public Map getAll() {
        return getCache();
    }

    @Override
    public List search(ComparatorIface comparator, Object pattern) {
        List result = new ArrayList();
        Iterator<String> keySetIterator = getCache().keySet().iterator();
        String key;
        while (keySetIterator.hasNext()) {
            key = keySetIterator.next();
            Object value = cache.get(key);
            if (comparator.compare(value, pattern) == 0) {
                result.add(value);
            }
        }
        return result;
    }

    @Override
    public boolean containsKey(String key) {
        return getCache().containsKey(key);
    }

    @Override
    public boolean remove(String key) {
        return getCache().remove(key) != null;
    }

    @Override
    public void clear() {
        getCache().clear();
    }

    @Override
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
