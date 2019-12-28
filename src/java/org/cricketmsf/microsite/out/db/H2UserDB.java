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
package org.cricketmsf.microsite.out.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cricketmsf.Adapter;
import org.cricketmsf.microsite.user.User;
import org.cricketmsf.out.db.ComparatorIface;
import org.cricketmsf.out.db.H2EmbededDB;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.SqlDBIface;

/**
 *
 * @author greg
 */
public class H2UserDB extends H2EmbededDB implements SqlDBIface, Adapter {

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
    }

    @Override
    public void addTable(String tableName, int maxSize, boolean persistent) throws KeyValueDBException {
        String query;
        StringBuilder sb = new StringBuilder();
        sb.append("create sequence if not exists user_number_seq;");
        sb.append("create table users (")
                .append("uid varchar primary key,")
                .append("type int,")
                .append("email varchar,")
                .append("name varchar,")
                .append("surname varchar,")
                .append("role varchar,")
                .append("secret varchar,")
                .append("password varchar,")
                .append("confirmed boolean,")
                .append("unregisterreq boolean,")
                .append("authstatus int,")
                .append("created timestamp,")
                .append("user_number bigint default user_number_seq.nextval )");
        query = sb.toString();
        try (Connection conn = getConnection()) {
            PreparedStatement pst;
            if (tableName.equals("users")) {
                pst = conn.prepareStatement(query);
            } else {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
            boolean updated = pst.executeUpdate() > 0;
            pst.close();
            conn.close();
            if (!updated) {
                throw new KeyValueDBException(KeyValueDBException.CANNOT_CREATE, "unable to create table " + tableName);
            }
        } catch (SQLException e) {
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public void put(String tableName, String key, Object o) throws KeyValueDBException {
        //System.out.println("PUT USER "+key);
        if (tableName.equals("users")) {
            try {
                putUser(tableName, key, (User) o);
            } catch (ClassCastException e) {
                throw new KeyValueDBException(KeyValueDBException.UNKNOWN, "object is not a User");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new KeyValueDBException(KeyValueDBException.CANNOT_CREATE, "unsupported table " + tableName);
        }
    }

    private void putUser(String tableName, String key, User user) throws KeyValueDBException {
        try (Connection conn = getConnection()) {
            String query = "merge into ?? (uid,type,email,name,surname,role,secret,password,confirmed,unregisterreq,authstatus,created) key (uid) values (?,?,?,?,?,?,?,?,?,?)";
            query = query.replaceFirst("\\?\\?", tableName);
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, user.getUid());
            pstmt.setInt(2, user.getType());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getName());
            pstmt.setString(5, user.getSurname());
            pstmt.setString(6, user.getRole());
            pstmt.setString(7, user.getConfirmString());
            pstmt.setString(8, user.getPassword());
            pstmt.setBoolean(9, user.isConfirmed());
            pstmt.setBoolean(10, user.isUnregisterRequested());
            pstmt.setInt(11, user.getStatus());
            pstmt.setTimestamp(12, new Timestamp(user.getCreatedAt()));
            int updated = pstmt.executeUpdate();
            //check?
        } catch (SQLException e) {
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object get(String tableName, String key) throws KeyValueDBException {
        return get(tableName, key, null);
    }

    @Override
    public Object get(String tableName, String key, Object o) throws KeyValueDBException {
        if (tableName.equals("users")) {
            return getUser(tableName, key, o);
        } else {
            return null;
        }
    }

    @Override
    public Map getAll(String tableName) throws KeyValueDBException {
        HashMap<String, User> map = new HashMap<>();
        //TODO: nie używać, zastąpić konkretnymi search'ami
        if (tableName.equals("users")) {
            String query = "select uid,type,email,name,surname,role,secret,password,confirmed,unregisterreq,authstatus,created,user_number from users";
            try (Connection conn = getConnection()) {
                PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    map.put(rs.getString(1), buildUser(rs));
                }
            } catch (SQLException e) {
                throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
            }
        }
        return map;
    }

    @Override
    public boolean containsKey(String tableName, String key) throws KeyValueDBException {
        String query;
        if (tableName.equals("users")) {
            query = "select uid from " + tableName + " where uid=?";
        } else {
            throw new KeyValueDBException(KeyValueDBException.TABLE_NOT_EXISTS, "unsupported table " + tableName);
        }
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        }
        return false;
    }

    @Override
    public boolean remove(String tableName, String key) throws KeyValueDBException {
        String query = "delete from ?? where uid = ?".replaceFirst("\\?\\?", tableName);
        boolean updated = false;
        if (tableName.equals("users")) {
            //query;
        } else {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        try (Connection conn = getConnection()) {
            PreparedStatement pst;
            pst = conn.prepareStatement(query);
            pst.setString(1, key);
            updated = pst.executeUpdate() > 0;
            pst.close();
            conn.close();
        } catch (SQLException e) {
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        }
        return updated;
    }

    @Override
    public List search(String tableName, String statement, String[] parameters) throws KeyValueDBException {
        //TODO
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List search(String tableName, ComparatorIface ci, Object o) throws KeyValueDBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    User buildUser(ResultSet rs) throws SQLException {
        //uid,type,email,role,secret,password,generalchannel,infochannel,warningchannel,alertchannel,confirmed,unregisterreq,authstatus,created
        User user = new User();
        user.setUid(rs.getString(1));
        user.setType(rs.getInt(2));
        user.setEmail(rs.getString(3));
        user.setName(rs.getString(4));
        user.setSurname(rs.getString(5));
        user.setRole(rs.getString(6));
        user.setConfirmString(rs.getString(7));
        user.setPassword(rs.getString(8));
        user.setConfirmed(rs.getBoolean(9));
        user.setUnregisterRequested(rs.getBoolean(10));
        user.setStatus(rs.getInt(11));
        user.setCreatedAt(rs.getTimestamp(12).getTime());
        user.setNumber(rs.getLong(13));
        return user;
    }

    private Object getUser(String tableName, String key, Object defaultResult) throws KeyValueDBException {
        User user = null;
        try (Connection conn = getConnection()) {
            String query = "select uid,type,email,name,surname,role,secret,password,confirmed,unregisterreq,authstatus,created,user_number from " + tableName + " where uid=?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                user = buildUser(rs);
            }
        } catch (SQLException e) {
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        }
        if (user == null) {
            return defaultResult;
        } else {
            return user;
        }
    }

    @Override
    protected void updateStructureTo(Connection conn, int versionNumber) throws KeyValueDBException {
        String query = "";
        switch (versionNumber) {
            case 2:
                query = "create sequence if not exists user_number_seq; alter table users add user_number bigint default user_number_seq.nextval;";
                break;
        }
        try {
            PreparedStatement pst;
            pst = conn.prepareStatement(query);
            pst.executeUpdate();
            pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        }
    }

}
