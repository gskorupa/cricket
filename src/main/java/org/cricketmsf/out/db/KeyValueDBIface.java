/*
 * Copyright 2016 Grzegorz Skorupa <g.skorupa at gmail.com>.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.cricketmsf.out.db;

import java.util.List;
import java.util.Map;

/**
 *
 * @author greg
 */
public interface KeyValueDBIface {
    
    /**
     * Returns database name (from adapter configuration)
     * @return name database name
     */
    public String getDbName();
    
    /**
     * Returns default name of the backup file
     * @return name backup file name
     */
    public String getBackupFileName();

    /**
     * Starts the database
     * @throws KeyValueDBException exception
     */
    public void start() throws KeyValueDBException;

    /**
     * Stops the database
     */
    public void stop();
    
    /**
     * Backups all database tables to the specified file on disk
     * 
     * @param fileLocation database file location
     * @throws KeyValueDBException  exception
     */
    public void backup(String fileLocation) throws KeyValueDBException;
    
    /**
     * Restores all database tables from the specified file on disk
     * 
     * @param fileLocation database file location
     * @throws KeyValueDBException exception
     */
    public void restore(String fileLocation) throws KeyValueDBException;

    /**
     * Creates new table within the database
     * @param name table name
     * @param capacity maximal number of table records (objects)
     * @param persistent table persistency 
     * @throws KeyValueDBException exception
     */
    public void addTable(String name, int capacity, boolean persistent) throws KeyValueDBException;

    /**
     * Deletes table and its content from the database
     * @param name table name
     * @throws KeyValueDBException exception
     */
    public void deleteTable(String name) throws KeyValueDBException;
    
    /**
     * Returns list of the database table names
     * @return list of table names
     * @throws KeyValueDBException exception
     */
    public List<String> getTableNames() throws KeyValueDBException;

    /**
     * Adds object to the database table. Overwrites existing object if the give key exists
     * @param tableName the name of the database table
     * @param key object unique key
     * @param value object to put
     * @throws KeyValueDBException exception
     */
    public void put(String tableName, String key, Object value) throws KeyValueDBException;

    /**
     * Returns object stored under given key
     * @param tableName the database table name
     * @param key required key
     * @return object or null
     * @throws KeyValueDBException tableName does not exists
     */
    public Object get(String tableName, String key) throws KeyValueDBException;

    /**
     * Returns object stored under given key
     * @param tableName the database table name
     * @param key required key
     * @param defaultValue default value
     * @return object or default value if the given key is not found
     * @throws KeyValueDBException tableName does not exists
     */
    public Object get(String tableName, String key, Object defaultValue) throws KeyValueDBException;

    /**
     * Returns map of all objects stored in the table
     * @param tableName the table name
     * @return map of stored objects
     * @throws KeyValueDBException exception 
     */
    public Map getAll(String tableName) throws KeyValueDBException;

    /**
     * Returns list of objects found using required pattern
     * 
     * @param tableName database table name
     * @param comparator comparator object
     * @param pattern pattern object used by comparator
     * @return list of objects
     * @throws KeyValueDBException  exception
     */
    public List search(String tableName, ComparatorIface comparator, Object pattern) throws KeyValueDBException;
    
    public List search(String tableName, String statement, Object[] parameters) throws KeyValueDBException;

    /**
     * Returns information if the given key is stored in database table
     * @param tableName database table name
     * @param key a key to search for
     * @return true if the key is present, otherwise false
     * @throws KeyValueDBException exception 
     */
    public boolean containsKey(String tableName, String key) throws KeyValueDBException;

    /**
     * Removes object indexed with the given key
     * @param tableName database table name
     * @param key the key that must be removed
     * @return true in case of successful removal, otherwise false
     * @throws KeyValueDBException exception 
     */
    public boolean remove(String tableName, String key) throws KeyValueDBException;

    /**
     * Removes all object from database table
     * @param tableName database table name
     * @throws KeyValueDBException  exception
     */
    public void clear(String tableName) throws KeyValueDBException;

}
