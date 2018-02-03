/*
 * Copyright 2017 Grzegorz Skorupa <g.skorupa at gmail.com>.
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

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.out.OutboundAdapter;
import org.h2.jdbcx.JdbcConnectionPool;

/**
 *
 * @author greg
 */
public class H2EmbededDB extends OutboundAdapter implements SqlDBIface, Adapter {

    private JdbcConnectionPool cp;
    private String location;
    private String path;
    private String fileName;
    private String testQuery;
    private String userName;
    private String password;
    private String systemVersion;

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        setPath(properties.get("path"));
        Kernel.getLogger().print("\tpath: " + getPath());
        setFileName(properties.get("file"));
        Kernel.getLogger().print("\tfile: " + getFileName());
        setLocation(getPath() + File.separator + getFileName());
        setTestQuery(properties.get("test-query"));
        Kernel.getLogger().print("\ttest-query: " + getTestQuery());
        setSystemVersion(properties.get("version"));
        Kernel.getLogger().print("\tversion: " + getSystemVersion());
        setUserName(properties.get("user"));
        Kernel.getLogger().print("\tuser: " + getUserName());
        setPassword(properties.get("password"));
        Kernel.getLogger().print("\tpassword=" + getPassword());
        try {
            start();
        } catch (KeyValueDBException ex) {
            Kernel.getInstance().dispatchEvent(Event.logSevere(this, ex.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return getFileName();
    }

    @Override
    public void createDatabase(Connection conn, String version) {
        if (conn == null || getTestQuery() == null || getTestQuery().isEmpty()) {
            Kernel.getInstance().dispatchEvent(Event.logSevere(this, "problem connecting to the database"));
            return;
        }
        String createQuery
                = "CREATE TABLE SERVICEVERSION(VERSION VARCHAR);"
                + "INSERT INTO SERVICEVERSION VALUES('" + version + "')";
        try {
            conn.createStatement().execute(createQuery);
            conn.close();
        } catch (SQLException e) {
            Kernel.getInstance().dispatchEvent(Event.logSevere(this, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return cp.getConnection();
    }

    @Override
    public String getVersion() {
        String version = null;
        try {
            Connection conn = getConnection();
            ResultSet rs = conn.createStatement().executeQuery("select * from serviceversion");
            while (rs.next()) {
                version = rs.getString("version");
            }
            conn.close();
        } catch (SQLException ex) {
            Kernel.getInstance().dispatchEvent(Event.logSevere(this, ex.getMessage()));
        }
        return version;
    }

    @Override
    public void start() throws KeyValueDBException {
        cp = JdbcConnectionPool.create("jdbc:h2:" + getLocation(), getUserName(), getPassword());
        Connection conn = null;
        try {
            conn = cp.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(getTestQuery());
            if (rs.next()) {
                Kernel.getLogger().print("\tdatabase " + getFileName() + " version: " + rs.getString("VERSION"));
            }
            conn.close();
        } catch (SQLException e) {
            try {
                createDatabase(conn, getSystemVersion());
            } catch (Exception ex) {
                e.printStackTrace();
                Kernel.getInstance().shutdown();
            }
        }
        String version = getVersion();
        try {
            updateStructure(cp.getConnection(), version, getSystemVersion());
        } catch (SQLException e) {
            throw new KeyValueDBException(KeyValueDBException.CANNOT_WRITE, "cannot update database version information");
        }
    }

    @Override
    public void stop() {
        //nothing todo
    }

    @Override
    public void deleteTable(String tableName) throws KeyValueDBException {
        String query = "drop if exists table ??";
        try (Connection conn = cp.getConnection()) {
            query = query.replaceAll("\\?\\?", tableName);
            PreparedStatement pstmt = conn.prepareStatement(query);
            if (!pstmt.execute()) {
                throw new KeyValueDBException(KeyValueDBException.CANNOT_DELETE, "table " + tableName + " not dropped");
            }
            conn.close();
        } catch (SQLException e) {
            throw new KeyValueDBException(KeyValueDBException.CANNOT_DELETE, "unable to drop " + tableName);
        }
    }

    @Override
    public List<String> getTableNames() throws KeyValueDBException {
        String query = "show tables from public";
        ArrayList list = new ArrayList();
        try (Connection conn = cp.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("TABLE_NAME"));
            }
            conn.close();
        } catch (SQLException e) {
            throw new KeyValueDBException(KeyValueDBException.CANNOT_DELETE, "unable to get table names");
        }
        return list;
    }

    @Override
    public void clear(String tableName) throws KeyValueDBException {
        String query = "delete from ??";
        try (Connection conn = cp.getConnection()) {
            query = query.replaceAll("\\?\\?", tableName);
            PreparedStatement pstmt = conn.prepareStatement(query);
            if (!pstmt.execute()) {
                throw new KeyValueDBException(KeyValueDBException.CANNOT_DELETE, "table " + tableName + " not cleared");
            }
            conn.close();
        } catch (SQLException e) {
            throw new KeyValueDBException(KeyValueDBException.CANNOT_DELETE, "unable to clear table " + tableName);
        }
    }

    @Override
    public void destroy() {
        if (cp != null) {
            cp.dispose();
        }
    }

    @Override
    public void addTable(String string, int i, boolean bln) throws KeyValueDBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void put(String string, String string1, Object o) throws KeyValueDBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object get(String string, String string1) throws KeyValueDBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object get(String string, String string1, Object o) throws KeyValueDBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map getAll(String string) throws KeyValueDBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List search(String string, ComparatorIface ci, Object o) throws KeyValueDBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean containsKey(String string, String string1) throws KeyValueDBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean remove(String tableName, String key) throws KeyValueDBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List search(String tableName, String statement, String[] parameters) throws KeyValueDBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
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
     * @return the testQuery
     */
    public String getTestQuery() {
        return testQuery;
    }

    /**
     * @param testQuery the testQuery to set
     */
    public void setTestQuery(String testQuery) {
        this.testQuery = testQuery;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the systemVersion
     */
    public String getSystemVersion() {
        return systemVersion;
    }

    /**
     * @param systemVersion the systemVersion to set
     */
    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
    }

    @Override
    public void backup(String fileLocation) throws KeyValueDBException {
        String query = "backup to '" + fileLocation + "'";
        try (Connection conn = cp.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.executeUpdate();
            }
            conn.close();
        } catch (SQLException e) {
            throw new KeyValueDBException(KeyValueDBException.CANNOT_DELETE, "backup error - " + e.getMessage());
        }
    }

    @Override
    public String getBackupFileName() {
        return getName() + ".zip";
    }

    public void updateStructure(Connection conn, String from, String to) throws KeyValueDBException {
        String query = "update serviceversion set version='" + to + "' where version='" + from + "'";
        try {
            conn.createStatement().execute(query);
            conn.close();
        } catch (SQLException e) {
            Kernel.getInstance().dispatchEvent(Event.logSevere(this, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
