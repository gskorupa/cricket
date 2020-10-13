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
import org.cricketmsf.microsite.out.auth.Token;
import org.cricketmsf.out.db.ComparatorIface;
import org.cricketmsf.out.db.H2EmbededDB;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.SqlDBIface;

/**
 *
 * @author greg
 */
public class H2AuthDB extends H2EmbededDB implements SqlDBIface, Adapter {

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
    }

    @Override
    public void addTable(String tableName, int maxSize, boolean persistent) throws KeyValueDBException {
        String query = "create table ?? (token varchar primary key, uid varchar, issuer varchar, payload varchar, tstamp timestamp, eoflife timestamp)";
        query=query.replaceFirst("\\?\\?", tableName);
        try (Connection conn = getConnection()) {
            PreparedStatement pst;
            if (tableName.equals("tokens")||tableName.equals("ptokens")) {
                pst = conn.prepareStatement(query);
            } else {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
            boolean updated = pst.executeUpdate()>0;
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
        if (tableName.equals("tokens")||tableName.equals("ptokens")) {
            try {
                putToken(tableName, key, (Token) o);
            } catch (ClassCastException e) {
                throw new KeyValueDBException(KeyValueDBException.UNKNOWN, "object is not a User");
            } catch (Exception e){
                e.printStackTrace();
            }
        } else {
            throw new KeyValueDBException(KeyValueDBException.CANNOT_CREATE, "unsupported table " + tableName);
        }
    }

    private void putToken(String tableName, String key, Token token) throws KeyValueDBException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(true);
            String query = "merge into ?? (token, uid, issuer, payload, tstamp, eoflife) key (token) values (?,?,?,?,?,?)";
            query = query.replaceFirst("\\?\\?", tableName);
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, token.getToken());
            pstmt.setString(2, token.getUid());
            pstmt.setString(3, token.getIssuer());
            pstmt.setString(4, token.getPayload());
            pstmt.setTimestamp(5, new Timestamp(token.getTimestamp()));
            pstmt.setTimestamp(6, new Timestamp(token.getEofLife()));
            int updated = pstmt.executeUpdate();
            conn.close();
            //TODO: check?
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public Object get(String tableName, String key) throws KeyValueDBException {
        return get(tableName, key, null);
    }

    @Override
    public Object get(String tableName, String key, Object o) throws KeyValueDBException {
        if (tableName.equals("tokens")||tableName.equals("ptokens")) {
            return getToken(tableName, key, o);
        } else {
            return null;
        }
    }

    @Override
    public Map getAll(String tableName) throws KeyValueDBException {
        HashMap<String, Token> map = new HashMap<>();
        //TODO: do not use - replace with dedicated searches
        Token token = null;
        try (Connection conn = getConnection()) {
            String query = "select token, uid, issuer, payload, tstamp, eoflife from " + tableName;
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                token = buildToken(rs, tableName.equals("ptokens"));
                map.put(token.getToken(), token);
            }
        } catch (SQLException e) {
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        }
        return map;
    }

    @Override
    public boolean containsKey(String tableName, String key) throws KeyValueDBException {
        String query;
        if (tableName.equals("tokens")||tableName.equals("ptokens")) {
            query = "select token from " + tableName + " where token=?";
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
        String query = "delete from ?? where token = ?".replaceFirst("\\?\\?", tableName);
        boolean updated = false;
        if (tableName.equals("tokens")||tableName.equals("ptokens")) {
            //query
        } else {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        try (Connection conn = getConnection()) {
            PreparedStatement pst;
            pst = conn.prepareStatement(query);
            pst.setString(1, key);
            updated = pst.executeUpdate()>0;
            pst.close();
            conn.close();
        } catch (SQLException e) {
            //e.printStackTrace();
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        } catch (Exception e){
            //e.printStackTrace();
        }
        return updated;
    }

    @Override
    public List search(String tableName, String statement, Object[] parameters) throws KeyValueDBException {
        //TODO
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List search(String tableName, ComparatorIface ci, Object o) throws KeyValueDBException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        /*
        if (ci instanceof UserComparator) {
            String path = ((Document) o).getPath();
            String query = "select uid,author,type,title,summary,content,tags,language,mimetype,status,createdby,size,commentable,created,modified,published from ?? where path = ?";
            query = query.replaceFirst("\\?\\?", tableName);
            ArrayList list = new ArrayList();
            try (Connection conn = getConnection()) {
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, path);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    list.add(buildDocument(rs));
                }
                conn.close();
            } catch (SQLException | CmsException e) {
                throw new KeyValueDBException(KeyValueDBException.UNKNOWN, e.getMessage());
            }
            return list;
        } else {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        */
    }

    Token buildToken(ResultSet rs, boolean permanent) throws SQLException {
        //token, uid, issuer, payload, tstamp, eoflife
        Token t = new Token(rs.getString(2), 0, permanent);
        t.setToken(rs.getString(1));
        t.setIssuer(rs.getString(3));
        t.setPayload(rs.getString(4));
        t.setTimestamp(rs.getTimestamp(5).getTime());
        t.setEndOfLife(rs.getTimestamp(6).getTime());
        return t;
    }

    private Object getToken(String tableName, String key, Object defaultResult) throws KeyValueDBException {
        Token token = null;
        try (Connection conn = getConnection()) {
            String query = "select token, uid, issuer, payload, tstamp, eoflife from " + tableName + " where token=?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                token = buildToken(rs, tableName.equals("ptokens"));
            }
        } catch (SQLException e) {
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        }
        if (token == null) {
            return defaultResult;
        } else {
            return token;
        }
    }

}
