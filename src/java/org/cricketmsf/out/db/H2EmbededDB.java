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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
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

    protected JdbcConnectionPool cp;
    protected String location;
    private String path;
    private String fileName;
    private String testQuery;
    private String userName;
    private String password;
    private String systemVersion;
    private boolean encrypted;
    private String filePassword;
    protected boolean autocommit;
    protected boolean ignorecase = false;
    protected boolean skipUpdate = false;

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
        setEncrypted(properties.get("encrypted"));
        Kernel.getLogger().print("\tencrypted=" + isEncrypted());
        setFilePassword(properties.get("filePassword"));
        Kernel.getLogger().print("\tfilePassword=" + getFilePassword());
        setAutocommit(properties.getOrDefault("autocommit", "true"));
        Kernel.getLogger().print("\tautocommit=" + autocommit);
        setIgnorecase("true".equalsIgnoreCase(properties.getOrDefault("ignorecase", "false")));
        Kernel.getLogger().print("\tignorecase=" + ignorecase);
        setSkipUpdate("true".equalsIgnoreCase(properties.getOrDefault("skip-update", "false")));
        Kernel.getLogger().print("\tskip-update=" + skipUpdate);
        try {
            start();
        } catch (KeyValueDBException ex) {
            Kernel.getInstance().dispatchEvent(Event.logSevere(this, ex.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getDbName() {
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
        Connection c = cp.getConnection();
        c.setAutoCommit(autocommit);
        return c;
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
        String connectString = "jdbc:h2:" + getLocation();
        if (true) {
            connectString = connectString.concat(";IGNORECASE=TRUE");
        }
        if (isEncrypted()) {
            cp = JdbcConnectionPool.create(connectString + ";CIPHER=AES", getUserName(), getFilePassword() + " " + getPassword());
        } else {
            cp = JdbcConnectionPool.create(connectString, getUserName(), getPassword());
        }
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
            if(!isSkipUpdate()) {
                updateStructure(cp.getConnection(), version, getSystemVersion());
            }
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
        //String query = "backup to '" + fileLocation + "'";
        String query = "script to '" + fileLocation + "' compression ZIP";
        try (Connection conn = cp.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.execute();
            }
            conn.close();
        } catch (SQLException e) {
            throw new KeyValueDBException(KeyValueDBException.CANNOT_DELETE, "backup error - " + e.getMessage());
        }
    }

    @Override
    public void restore(String fileLocation) throws KeyValueDBException {
        //String query = "backup to '" + fileLocation + "'";
        String query = "runscript from '" + fileLocation + "'";
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
        return getDbName() + ".zip";
    }

    public final void updateStructure(Connection conn, String from, String to) throws KeyValueDBException {
        int fromVersion = 1;
        int toVersion = -1;
        try {
            fromVersion = Integer.parseInt(from);
        } catch (NumberFormatException | NullPointerException e) {
            e.printStackTrace();
        }
        try {
            toVersion = Integer.parseInt(to);
        } catch (NumberFormatException | NullPointerException e) {
            e.printStackTrace();
            throw new KeyValueDBException(KeyValueDBException.CANNOT_WRITE, "cannot update database structure of " + this.getClass().getSimpleName());
        }
        for (int i = fromVersion; i < toVersion; i++) {
            updateStructureTo(conn, i + 1);
        }

        String query = "update serviceversion set version='" + to + "'";
        try {
            conn.createStatement().execute(query);
            conn.close();
        } catch (SQLException e) {
            Kernel.getInstance().dispatchEvent(Event.logSevere(this, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void updateStructureTo(Connection conn, int versionNumber) throws KeyValueDBException {
        throw new KeyValueDBException(KeyValueDBException.UNKNOWN, "method not implemented");
    }

    /**
     * @return the encrypted
     */
    public boolean isEncrypted() {
        return encrypted;
    }

    /**
     * @param encrypted the encrypted to set
     */
    public void setEncrypted(String encrypted) {
        this.encrypted = Boolean.parseBoolean(encrypted);
    }

    /**
     * @return the filePassword
     */
    public String getFilePassword() {
        return filePassword;
    }

    /**
     * @param filePassword the filePassword to set
     */
    public void setFilePassword(String filePassword) {
        this.filePassword = filePassword;
    }

    /**
     * @param autocommit the autocommit to set
     */
    public void setAutocommit(String value) {
        this.autocommit = Boolean.parseBoolean(value);
    }

    /**
     * @param ignorecase the ignorecase to set
     */
    public void setIgnorecase(boolean ignorecase) {
        this.ignorecase = ignorecase;
    }

    @Override
    public List execute(String query) throws SQLException {
        ArrayList<List> result = new ArrayList<>();
        try (Connection conn = cp.getConnection()) {
            ArrayList row;
            Statement stmt = conn.createStatement();
            boolean browsable = stmt.execute(query);
            if (browsable) {
                ArrayList<Integer> types = new ArrayList<>();
                ResultSet rs = stmt.getResultSet();
                ResultSetMetaData rsm = rs.getMetaData();
                int numberOfColumns = 1;
                row = new ArrayList();
                boolean end =false;
                while (!end) {
                    try {
                        row.add(rsm.getColumnLabel(numberOfColumns));
                        types.add(rsm.getColumnType(numberOfColumns));
                        numberOfColumns++;
                    } catch (Exception e) {
                        end=true;
                    }
                }
                result.add(row);
                numberOfColumns--;
                while (rs.next()) {
                    row = new ArrayList();
                    for (int i = 1; i <= numberOfColumns; i++) {
                        row.add("" + rs.getObject(i));
                    }
                    result.add(row);
                }
            } else {
                row = new ArrayList();
                row.add("Count: " + stmt.getUpdateCount());
                result.add(row);
            }
            return result;
        }
    }

    /**
     * @return the skipUpdate
     */
    public boolean isSkipUpdate() {
        return skipUpdate;
    }

    /**
     * @param skipUpdate the skipUpdate to set
     */
    public void setSkipUpdate(boolean skipUpdate) {
        this.skipUpdate = skipUpdate;
    }
}
