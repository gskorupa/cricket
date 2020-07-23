/*
 * Copyright 2020 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
import org.cricketmsf.out.dispatcher.DispatcherIface;
import org.postgresql.ds.PGConnectionPoolDataSource;

/**
 *
 * @author greg
 */
public class PostgreSqlDB extends OutboundAdapter implements SqlDBIface, Adapter {

    private String host;
    private String[] serverNames;
    private String port;
    private int[] ports;
    protected PGConnectionPoolDataSource cp;
    private String databaseName;
    private String testQuery;
    private String userName;
    private String password;
    private String systemVersion;
    private boolean encrypted;
    protected boolean autocommit;
    protected boolean ignorecase = false;
    private boolean skipUpdate = false;

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        //we cannot use super.loadProperties(properties, adapterName);
        //so we need these 3 lines:
        this.name = adapterName;
        this.properties = (HashMap<String, String>) properties.clone();
        getStatus(adapterName); //required if we need to overwrite updateStatusItem() method
        //
        setHost((String) properties.getOrDefault("hosts", ""));
        setServerNames(getHost().split(","));
        Kernel.getLogger().print("\thosts: " + getHost());
        setPort((String) properties.getOrDefault("ports", "0"));
        setPorts(getPort());
        Kernel.getLogger().print("\tports: " + getPort());
        setDatabaseName(properties.get("database"));
        Kernel.getLogger().print("\tdatabase: " + getDatabaseName());
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
        setAutocommit(properties.getOrDefault("autocommit", "true"));
        Kernel.getLogger().print("\tautocommit=" + autocommit);
        setIgnorecase("true".equalsIgnoreCase(properties.getOrDefault("ignorecase", "false")));
        Kernel.getLogger().print("\tignorecase=" + ignorecase);
        setSkipUpdate("true".equalsIgnoreCase(properties.getOrDefault("skip-update", "false")));
        Kernel.getLogger().print("\tskip-update=" + isSkipUpdate());
        try {
            start();
        } catch (KeyValueDBException ex) {
            Kernel.getInstance().dispatchEvent(Event.logSevere(this, ex.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String getDbName() {
        return getDatabaseName();
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
        cp = new PGConnectionPoolDataSource();
        cp.setServerNames(getServerNames());
        cp.setPortNumbers(getPorts());
        cp.setDatabaseName(getDatabaseName());
        cp.setUser(getUserName());
        cp.setPassword(getPassword());
        cp.setSsl(isEncrypted());

        Connection conn = null;
        try {
            conn = cp.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(getTestQuery());
            if (rs.next()) {
                //Kernel.getLogger().print("\tdatabase " + getFileName() + " version: " + rs.getString("VERSION"));
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
            if (!isSkipUpdate()) {
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
            //cp.setdispose();
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
    public List search(String tableName, String statement, Object[] parameters) throws KeyValueDBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        throw new KeyValueDBException(KeyValueDBException.NOT_SUPPORTED, "backup not supported");
    }

    @Override
    public void restore(String fileLocation) throws KeyValueDBException {
        throw new KeyValueDBException(KeyValueDBException.NOT_SUPPORTED, "restore not supported");
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
        throw new KeyValueDBException(KeyValueDBException.NOT_IMPLEMENTED, "method not implemented");
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
                boolean end = false;
                while (!end) {
                    try {
                        row.add(rsm.getColumnLabel(numberOfColumns));
                        types.add(rsm.getColumnType(numberOfColumns));
                        numberOfColumns++;
                    } catch (Exception e) {
                        end = true;
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

    @Override
    public DispatcherIface getDispatcher() {
        return null;
    }

    /**
     * @return the databaseName
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * @param databaseName the databaseName to set
     */
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * @return the serverNames
     */
    public String[] getServerNames() {
        return serverNames;
    }

    /**
     * @param serverNames the serverNames to set
     */
    public void setServerNames(String[] serverNames) {
        this.serverNames = serverNames;
    }

    /**
     * @return the port
     */
    public String getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * @return the ports
     */
    public int[] getPorts() {
        return ports;
    }

    /**
     * @param ports the ports to set
     */
    public void setPorts(String ports) {
        String[] arr = ports.split(",");
        int[] tmp = new int[serverNames.length];
        for (int i = 0; i < tmp.length; i++) {
            if (i < arr.length) {
                try {
                    tmp[i] = Integer.parseInt(arr[i]);
                } catch (NumberFormatException e) {
                    tmp[i] = 0;
                }
            } else {
                tmp[i] = 0;
            }
        }
        this.ports = tmp;
    }
}
