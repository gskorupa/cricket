/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cricketmsf.out.db;

import java.util.List;
import java.util.Map;

/**
 *
 * @author greg
 */
public interface KeyValueDBIface {

    public void start() throws KeyValueDBException;;

    public void stop();

    public void addTable(String name, int capacity, boolean persistent) throws KeyValueDBException;

    public void deleteTable(String name) throws KeyValueDBException;

    public void put(String tableName, String key, Object value) throws KeyValueDBException;

    public Object get(String tableName, String key) throws KeyValueDBException;

    public Object get(String tableName, String key, Object defaultValue) throws KeyValueDBException;

    public Map getAll(String tableName) throws KeyValueDBException;

    public List search(String tableName, ComparatorIface comparator, Object pattern) throws KeyValueDBException;

    public boolean containsKey(String tableName, String key) throws KeyValueDBException;

    public boolean remove(String tableName, String key) throws KeyValueDBException;

    public void clear(String tableName) throws KeyValueDBException;

}
