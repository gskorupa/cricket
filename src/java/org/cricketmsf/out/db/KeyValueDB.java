/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cricketmsf.out.db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cricketmsf.Adapter;
import org.cricketmsf.Kernel;
import org.cricketmsf.out.OutboundAdapter;

/**
 *
 * @author greg
 */
public class KeyValueDB extends OutboundAdapter implements KeyValueDBIface, Adapter {

    private String storagePath;
    private String dbName;
    private String filePath;
    private HashMap<String, KeyValueTable> tables;

    @Override
    public void loadProperties(HashMap properties, String name) {
        storagePath = (String) properties.getOrDefault("path", "./");
        Kernel.getInstance().getLogger().print("\tpath: " + storagePath);
        // fix to handle '.'
        if (storagePath.startsWith(".")) {
            storagePath = System.getProperty("user.dir") + storagePath.substring(1);
        }
        dbName = (String) properties.get("name");
        Kernel.getInstance().getLogger().print("\tdatabase name: " + dbName);
        String pathSeparator = System.getProperty("file.separator");
        if (!storagePath.endsWith(pathSeparator)) {
            storagePath = storagePath + pathSeparator + dbName;
        }
        filePath
                = storagePath + ".db";
        Kernel.getInstance().getLogger().print("\tdatabase file: " + filePath);
        try {
            start();
        } catch (KeyValueDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() throws KeyValueDBException {
        tables = new HashMap<>();
        //read lines from file with: tableName,capacity
        //table.read
        //taples.put(name,table)
        try {
            FileReader fr = new FileReader(filePath);
            BufferedReader bufr = new BufferedReader(fr);
            int count = 1;
            String line = bufr.readLine();
            String[] params;
            while (line != null) {
                line = line.trim();
                if (!line.startsWith("#")) {
                    params = line.split(",");
                    addTable(params[0], Integer.parseInt(params[1]), Boolean.valueOf(params[2]).booleanValue());
                    line = bufr.readLine();
                }
                line = bufr.readLine();
            }
            bufr.close();
        } catch (IOException e) {

        }
    }

    @Override
    public void stop() {
        try {
            FileWriter fw = new FileWriter(filePath);
            fw.write("# "+this.getClass().getName()+"\r\n");
            fw.write("# DO NOT MODIFY\r\n");
            fw.write("#\r\n");
            tables.keySet().forEach((key) -> {
                if (tables.get(key).persistent) {
                    tables.get(key).write();
                }
                try {
                    fw.write(tables.get(key).name + "," + tables.get(key).capacity + "," + tables.get(key).persistent + "\r\n");
                } catch (IOException e) {
                }
            });
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //write db file
    }

    @Override
    public void destroy() {
        stop();
    }

    @Override
    public void addTable(String name, int capacity, boolean persistent) throws KeyValueDBException {
        if (tables.containsKey(name)) {
            throw new KeyValueDBException(KeyValueDBException.CANNOT_CREATE);
        }
        tables.put(name, new KeyValueTable(name, capacity, persistent, storagePath));
    }

    @Override
    public void deleteTable(String name) throws KeyValueDBException {
        if (!tables.containsKey(name)) {
            throw new KeyValueDBException(KeyValueDBException.TABLE_NOT_EXISTS);
        }
        tables.remove(name);
    }

    @Override
    public void put(String tableName, String key, Object value) throws KeyValueDBException {
        try {
            tables.get(tableName).put(key, value);
        } catch (NullPointerException e) {
            throw new KeyValueDBException(KeyValueDBException.TABLE_NOT_EXISTS);
        }
    }

    @Override
    public Object get(String tableName, String key) throws KeyValueDBException {
        try {
            return tables.get(tableName).get(key);
        } catch (NullPointerException e) {
            throw new KeyValueDBException(KeyValueDBException.TABLE_NOT_EXISTS);
        }
    }

    @Override
    public Object get(String tableName, String key, Object defaultValue) throws KeyValueDBException {
        try {
            return tables.get(tableName).get(key, defaultValue);
        } catch (NullPointerException e) {
            throw new KeyValueDBException(KeyValueDBException.TABLE_NOT_EXISTS);
        }
    }

    @Override
    public Map getAll(String tableName) throws KeyValueDBException {
        //return (LimitedMap) cache.clone();
        try {
            return tables.get(tableName).getAll();
        } catch (NullPointerException e) {
            throw new KeyValueDBException(KeyValueDBException.TABLE_NOT_EXISTS);
        }
    }

    @Override
    public List search(String tableName, ComparatorIface comparator, Object pattern) throws KeyValueDBException {
        try {
            return tables.get(tableName).search(comparator, pattern);
        } catch (NullPointerException e) {
            throw new KeyValueDBException(KeyValueDBException.TABLE_NOT_EXISTS);
        }
    }

    @Override
    public boolean containsKey(String tableName, String key) throws KeyValueDBException {
        try {
            return tables.get(tableName).containsKey(key);
        } catch (NullPointerException e) {
            throw new KeyValueDBException(KeyValueDBException.TABLE_NOT_EXISTS);
        }
    }

    @Override
    public boolean remove(String tableName, String key) throws KeyValueDBException {
        try {
            return tables.get(tableName).remove(key);
        } catch (NullPointerException e) {
            throw new KeyValueDBException(KeyValueDBException.TABLE_NOT_EXISTS);
        }
    }

    @Override
    public void clear(String tableName) throws KeyValueDBException {
        try {
            tables.get(tableName).clear();
        } catch (NullPointerException e) {
            throw new KeyValueDBException(KeyValueDBException.TABLE_NOT_EXISTS);
        }
    }

    @Override
    public List<String> getTableNames() throws KeyValueDBException {
        ArrayList<String> result = new ArrayList<>();
        tables.keySet().forEach(key -> result.add((String)key));
        return result;
    }
}
