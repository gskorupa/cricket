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

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author greg
 */
public class KeyValueTable {

    protected LimitedMap cache = null;
    private String storagePath;
    protected int capacity = 0;
    protected boolean persistent = false;
    protected String name;

    protected KeyValueTable(String name, int capacity, boolean persistent, String location) {
        this.name = name;
        this.storagePath = location + "." + name + ".db";
        this.capacity = capacity;
        this.persistent = persistent;
        read();
    }

    /**
     * Reads serialized database from disk
     */
    private void read() {
        try {
            try (XMLDecoder decoder = new XMLDecoder(
                    new BufferedInputStream(new FileInputStream(storagePath)
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
                            new FileOutputStream(storagePath)))) {
                encoder.writeObject(cache);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void put(String key, Object value) {
        cache.put(key, value);
    }

    public Object get(String key) {
        return cache.get(key);
    }

    public Object get(String key, Object defaultValue) {
        return cache.containsKey(key) ? cache.get(key) : defaultValue;
    }

    public Map getAll() {
        return (LimitedMap) cache.clone();
    }

    public List search(ComparatorIface comparator, Object pattern) {
        List result = new ArrayList();
        Iterator<String> keySetIterator = cache.keySet().iterator();
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

    public boolean containsKey(String key) {
        return cache.containsKey(key);
    }

    public boolean remove(String key) {
        return cache.remove(key) != null;
    }

    public void clear() {
        cache.clear();
    }
}
