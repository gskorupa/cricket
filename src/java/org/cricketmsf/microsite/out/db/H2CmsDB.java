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
import java.time.Instant;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.microsite.cms.CmsException;
import org.cricketmsf.microsite.cms.Document;
import org.cricketmsf.microsite.cms.DocumentPathAndTagComparator;
import org.cricketmsf.out.db.ComparatorIface;
import org.cricketmsf.out.db.H2EmbededDB;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.SqlDBIface;

/**
 *
 * @author greg
 */
public class H2CmsDB extends H2EmbededDB implements SqlDBIface, Adapter {

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
    }

    @Override
    public void addTable(String tableName, int maxSize, boolean persistent) throws KeyValueDBException {
        String docQuery;
        StringBuilder sb = new StringBuilder();
        sb.append(" (")
                .append("uid varchar primary key,")
                .append("name varchar,")
                .append("author varchar,")
                .append("type varchar,")
                .append("path varchar,")
                .append("title varchar,")
                .append("summary varchar,")
                .append("content varchar,")
                .append("tags varchar,")
                .append("language varchar,")
                .append("mimetype varchar,")
                .append("status varchar,")
                .append("createdby varchar,")
                .append("size bigint,")
                .append("commentable boolean,")
                .append("created timestamp,")
                .append("modified timestamp,")
                .append("published timestamp)");
        docQuery = sb.toString();
        try (Connection conn = getConnection()) {
            PreparedStatement pst;
            if (tableName.startsWith("published_") || tableName.startsWith("wip_")) {
                pst = conn.prepareStatement("create table " + tableName + docQuery);
            } else if (tableName.equals("paths")) {
                pst = conn.prepareStatement("create table paths (path varchar primary key)");
            } else if (tableName.equals("tags")) {
                pst = conn.prepareStatement("create table tags (tag varchar primary key)");
            } else {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
            boolean updated = pst.execute();
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
        if (tableName.startsWith("published_") || tableName.startsWith("wip_")) {
            try {
                putDocument(tableName, key, (Document) o);
            } catch (ClassCastException e) {
                throw new KeyValueDBException(KeyValueDBException.UNKNOWN, "object is not a Document ");
            }
        } else if (tableName.equals("paths")) {
            putPath(key);
        } else if (tableName.equals("tags")) {
            putTags(key);
        }
    }

    private void putDocument(String tableName, String key, Document doc) throws KeyValueDBException {
        try (Connection conn = getConnection()) {
            String query = "merge into ?? (uid,name,author,type,path,title,summary,content,tags,language,mimetype,status,createdby,size,commentable,created,modified,published) key (uid) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            query = query.replaceFirst("\\?\\?", tableName);
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, doc.getUid());
            pstmt.setString(2, doc.getName());
            pstmt.setString(3, doc.getAuthor());
            pstmt.setString(4, doc.getType());
            pstmt.setString(5, doc.getPath());
            pstmt.setString(6, doc.getTitle());
            pstmt.setString(7, doc.getSummary());
            pstmt.setString(8, doc.getContent());
            pstmt.setString(9, doc.getTags());
            pstmt.setString(10, doc.getLanguage());
            pstmt.setString(11, doc.getMimeType());
            pstmt.setString(12, doc.getStatus());
            pstmt.setString(13, doc.getCreatedBy());
            pstmt.setLong(14, doc.getSize());
            pstmt.setBoolean(15, doc.isCommentable());
            pstmt.setTimestamp(16, Timestamp.from(Instant.from(ISO_INSTANT.parse(doc.getCreated()))));            //doc.getCreated().toEpochMilli()));
            pstmt.setTimestamp(17, Timestamp.from(Instant.from(ISO_INSTANT.parse(doc.getModified()))));
            if (doc.getPublished() == null) {
                pstmt.setNull(18, java.sql.Types.TIMESTAMP, "TIMESTAMP");
            } else {
                pstmt.setTimestamp(18, Timestamp.from(Instant.from(ISO_INSTANT.parse(doc.getPublished()))));
            }
            int updated = pstmt.executeUpdate();
            //check?
        } catch (SQLException e) {
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        }
    }

    private void putPath(String path) throws KeyValueDBException {
        try (Connection conn = getConnection()) {
            String query = "merge into paths values (?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, path);
            int updated = pstmt.executeUpdate();
            //check?
        } catch (SQLException e) {
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        }
    }

    private void putTags(String tags) throws KeyValueDBException {
        try (Connection conn = getConnection()) {
            String[] tagArray = tags.split(",");
            String query = "merge into tags values (?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            for (String tagArray1 : tagArray) {
                if (!tagArray1.trim().isEmpty()) {
                    pstmt.setString(1, tagArray1);
                    int updated = pstmt.executeUpdate();
                }
            }
            //check?
        } catch (SQLException e) {
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public Object get(String tableName, String key) throws KeyValueDBException {
        return get(tableName, key, null);
    }

    @Override
    public Object get(String tableName, String key, Object o) throws KeyValueDBException {
        if (tableName.startsWith("wip_") || tableName.startsWith("published_")) {
            return getDocument(tableName, key, o);
        } else if (tableName.equals("paths")) {
            return getPath("paths", key, o);
        } else if (tableName.equals("tags")) {
            return getTag("tags", key, o);
        } else {
            return null;
        }
    }

    @Override
    public Map getAll(String tableName) throws KeyValueDBException {
        String query;
        HashMap<String, String> map = new HashMap<>();
        if (tableName.equals("paths")) {
            query = "select path from paths";
        } else if (tableName.equals("tags")) {
            query = "select tag from tags";
        } else {
            return map;
        }
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                map.put(rs.getString(1), rs.getString(1));
            }
        } catch (SQLException e) {
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        }
        return map;
    }

    @Override
    public boolean containsKey(String tableName, String key) throws KeyValueDBException {
        String query;
        if (tableName.startsWith("published_") || tableName.startsWith("wip_")) {
            query = "select uid from " + tableName + " where uid=?";
        } else if (tableName.equals("paths")) {
            query = "select path from paths where path=?";
        } else if (tableName.equals("tags")) {
            query = "select tag from tags where tag=?";
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
        String docQuery = "delete from ?? where uid = ?".replaceFirst("\\?\\?", tableName);
        String pathQuery = "delete from paths where path = ?";
        String tagQuery = "delete from tags where tag = ?";
        String query;
        boolean updated = false;
        if (tableName.startsWith("published_") || tableName.startsWith("wip_")) {
            query = docQuery;
        } else if (tableName.equals("paths")) {
            query = pathQuery;
        } else if (tableName.equals("tags")) {
            query = tagQuery;
        } else {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        try (Connection conn = getConnection()) {
            PreparedStatement pst;
            pst = conn.prepareStatement(query);
            pst.setString(1, key);
            updated = pst.execute();
            pst.close();
            conn.close();
        } catch (SQLException e) {
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
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
        if (ci instanceof DocumentPathAndTagComparator) {
            String path = ((Document) o).getPath();
            String tags = ((Document) o).getTags();
            String queryAll = "select uid,author,type,title,summary,content,tags,language,mimetype,status,createdby,size,commentable,created,modified,published from ?? where path = ? and tags like ?";
            String queryWithPath = "select uid,author,type,title,summary,content,tags,language,mimetype,status,createdby,size,commentable,created,modified,published from ?? where path = ?";
            String queryWithTags = "select uid,author,type,title,summary,content,tags,language,mimetype,status,createdby,size,commentable,created,modified,published from ?? where tags like ?";
            String query;
            boolean tagsOnly = path.isEmpty();
            boolean pathOnly = tags.isEmpty();
            if (tagsOnly) {
                query = queryWithTags;
            } else if (pathOnly) {
                query = queryWithPath;
            } else {
                query = queryAll;
            }
            query = query.replaceFirst("\\?\\?", tableName);
            ArrayList list = new ArrayList();
            try (Connection conn = getConnection()) {
                PreparedStatement pstmt = conn.prepareStatement(query);
                if (pathOnly) {
                    pstmt.setString(1, path);
                } else if (tagsOnly) {
                    pstmt.setString(1, "%," + tags + ",%");
                } else {
                    pstmt.setString(1, path);
                    pstmt.setString(2, "%," + tags + ",%");
                }
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    list.add(buildDocument(rs));
                }
                conn.close();
            } catch (SQLException | CmsException e) {
                e.printStackTrace();
                throw new KeyValueDBException(KeyValueDBException.UNKNOWN, e.getMessage());
            }
            return list;
        } else {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    Document buildDocument(ResultSet rs) throws CmsException, SQLException {
        Document doc = new Document();
        doc.setUid(rs.getString(1));
        doc.setAuthor(rs.getString(2));
        doc.setType(rs.getString(3));
        doc.setTitle(rs.getString(4));
        doc.setSummary(rs.getString(5));
        doc.setContent(rs.getString(6));
        doc.setTags(rs.getString(7));
        doc.setLanguage(rs.getString(8));
        doc.setMimeType(rs.getString(9));
        doc.setStatus(rs.getString(10));
        doc.setCreatedBy(rs.getString(11));
        doc.setSize(rs.getLong(12));
        doc.setCommentable(rs.getBoolean(13));
        doc.setCreated(rs.getTimestamp(14).toInstant().toString());
        doc.setModified(rs.getTimestamp(15).toInstant().toString());
        try {
            doc.setPublished(rs.getTimestamp(16).toInstant().toString());
        } catch (NullPointerException e) {
        }
        return doc;
    }

    private Object getDocument(String tableName, String key, Object defaultResult) throws KeyValueDBException {
        Document doc = null;
        try (Connection conn = getConnection()) {
            String query = "select uid,author,type,title,summary,content,tags,language,mimetype,status,createdby,size,commentable,created,modified,published from " + tableName + " where uid=?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                doc = buildDocument(rs);
            }
        } catch (SQLException e) {
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        } catch (CmsException ex) {
            Kernel.getInstance().dispatchEvent(Event.logSevere(this, "unable to restore UID"));
        }
        if (doc == null) {
            return defaultResult;
        } else {
            return doc;
        }
    }

    private Object getPath(String tableName, String key, Object defaultResult) throws KeyValueDBException {
        String path = null;
        try (Connection conn = getConnection()) {
            String query = "select * from paths where path=?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                path = rs.getString(1);
            }
        } catch (SQLException e) {
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        }
        if (path == null) {
            return defaultResult;
        } else {
            return path;
        }
    }

    private Object getTag(String tableName, String key, Object defaultResult) throws KeyValueDBException {
        String path = null;
        try (Connection conn = getConnection()) {
            String query = "select * from tags where tag=?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                path = rs.getString(1);
            }
        } catch (SQLException e) {
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        }
        if (path == null) {
            return defaultResult;
        } else {
            return path;
        }
    }

}
