/*
 * Copyright 2018 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package org.cricketmsf.microsite.cms;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.ParameterMapResult;
import org.cricketmsf.in.http.Result;
import org.cricketmsf.out.OutboundAdapter;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.KeyValueDBIface;
import org.cricketmsf.out.file.FileObject;

/**
 *
 * @author greg
 */
public class CmsEmbededAdapter extends OutboundAdapter implements Adapter, CmsIface {

    public static int NOT_INITIALIZED = 0;
    public static int FAILED = 1;
    public static int OK = 3;

    private ArrayList<String> supportedLanguages;
    private ArrayList<String> supportedStatuses;

    String helperAdapterName = null;
    KeyValueDBIface database = null;
    String ruleEngineName = null;
    RuleEngineIface ruleEngine = null;

    int status = NOT_INITIALIZED;

    private String wwwRoot = null; //www root path in the filesystem
    private String fileRoot = null; //cms document files root path in the filesystem
    private String publishedFilesRoot = null;
    String indexFileName = "index.html";

    private void initRuleEngine() {
        if (ruleEngineName == null) {
            ruleEngine = new DefaultRuleEngine();
            return;
        }
        try {
            ruleEngine = (RuleEngineIface) Kernel.getInstance().getAdaptersMap().get(ruleEngineName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private KeyValueDBIface getDatabase() throws KeyValueDBException {
        if (database == null) {
            try {
                database = (KeyValueDBIface) Kernel.getInstance().getAdaptersMap().get(helperAdapterName);
                initDB();
            } catch (Exception e) {
                e.printStackTrace();
                status = FAILED;
                throw new KeyValueDBException(KeyValueDBException.UNKNOWN, "database adapter not available");
            }
        }
        return database;
    }

    /**
     * We want to use configured adapters so we must provide these adapters
     */
    public void initDB() throws CmsException {
        //TODO: load languages from properties

        // create tables
        for (int i = 0; i < supportedLanguages.size(); i++) {
            try {
                database.addTable("published_" + supportedLanguages.get(i), 1000, true);
            } catch (KeyValueDBException e) {
                Kernel.getInstance().dispatchEvent(Event.logFine(this.getClass().getSimpleName(), e.getMessage()));
            }
        }
        for (int i = 0; i < supportedLanguages.size(); i++) {
            try {
                database.addTable("wip_" + supportedLanguages.get(i), 1000, true);
            } catch (KeyValueDBException e) {
                Kernel.getInstance().dispatchEvent(Event.logFine(this.getClass().getSimpleName(), e.getMessage()));
            }
        }
        /*
        try { // each document uid has list of comments
            database.addTable("comments", 2000, true);
        } catch (KeyValueDBException e) {
            Kernel.getInstance().dispatchEvent(Event.logFine(this.getClass().getSimpleName(), e.getMessage()));
        }
        try { // each unique tag has list of documents uids
            database.addTable("tags", 100, true);
        } catch (KeyValueDBException e) {
            Kernel.getInstance().dispatchEvent(Event.logFine(this.getClass().getSimpleName(), e.getMessage()));
        }
         */
        try { // document paths
            database.addTable("paths", 100, true);
        } catch (KeyValueDBException e) {
            Kernel.getInstance().dispatchEvent(Event.logFine(this.getClass().getSimpleName(), e.getMessage()));
        }
        try { // document paths
            database.addTable("tags", 100, true);
        } catch (KeyValueDBException e) {
            Kernel.getInstance().dispatchEvent(Event.logFine(this.getClass().getSimpleName(), e.getMessage()));
        }
        status = OK;
    }

    @Override
    public Document getDocument(String uid, String language) throws CmsException {
        return getDocument(uid, language, null, null);
    }

    @Override
    public Document getDocument(String uid, String language, String status, List<String> roles) throws CmsException {
        //TODO: getDocument(uid, null, status) should return list of documents
        Document doc = null;
        //System.out.println("LANGUAGE:[" + language + "]");

        if (language != null && !supportedLanguages.contains(language)) {
            throw new CmsException(CmsException.UNSUPPORTED_LANGUAGE, "unsupported language");
        }
        String docStatus = status;
        //if (docStatus == null) {
        //    docStatus = "published";
        //}
        if (docStatus == null) {
            if (language != null && !language.isEmpty()) {
                try {
                    doc = (Document) getDatabase().get("published_" + language, uid);
                    if (doc == null) {
                        doc = (Document) getDatabase().get("wip_" + language, uid);
                    }
                } catch (KeyValueDBException e) {
                    e.printStackTrace();
                    throw new CmsException(CmsException.HELPER_EXCEPTION, e.getMessage());
                }
            } else {
                for (int i = 0; i < supportedLanguages.size(); i++) {
                    try {
                        doc = (Document) getDatabase().get("published_" + supportedLanguages.get(i), uid);
                        if (doc == null) {
                            doc = (Document) getDatabase().get("wip_" + supportedLanguages.get(i), uid);
                        }
                    } catch (KeyValueDBException e) {
                        e.printStackTrace();
                        // nothing to do
                    }
                    if (doc != null) {
                        break;
                    }
                }
                if (doc == null) {
                    throw new CmsException(CmsException.NOT_FOUND, "not found");
                }
            }
        } else if ("published".equals(docStatus) || "wip".equals(docStatus)) {
            if (language != null && !language.isEmpty()) {
                try {
                    doc = (Document) getDatabase().get(docStatus + "_" + language, uid);
                } catch (KeyValueDBException e) {
                    //e.printStackTrace();
                    throw new CmsException(CmsException.HELPER_EXCEPTION, e.getMessage());
                }
            } else {
                for (int i = 0; i < supportedLanguages.size(); i++) {
                    try {
                        doc = (Document) getDatabase().get(docStatus + "_" + supportedLanguages.get(i), uid);
                    } catch (KeyValueDBException e) {
                        e.printStackTrace();
                        // nothing to do
                    }
                    if (doc != null) {
                        break;
                    }
                }
                if (doc == null) {
                    throw new CmsException(CmsException.NOT_FOUND, "not found");
                }
            }
        } else {
            throw new CmsException(CmsException.UNSUPPORTED_STATUS, "unsupported status");
        }
        if(null==ruleEngine){
            initRuleEngine();
        }
        doc = ruleEngine.processDocument(doc, roles);
        return doc;
    }

    private String resolveTableName(Document doc) {
        return ("PUBLISHED".equalsIgnoreCase(doc.getStatus()) ? "published_" : "wip_") + doc.getLanguage();
    }

    @Override
    public void addDocument(Document doc, List<String> roles) throws CmsException {
        if (doc.getLanguage() == null || !supportedLanguages.contains(doc.getLanguage())) {
            throw new CmsException(CmsException.UNSUPPORTED_LANGUAGE);
        }
        ruleEngine.checkDocument(doc, roles);
        try {
            if (getDatabase().containsKey(resolveTableName(doc), doc.getUid())) {
                throw new CmsException(CmsException.ALREADY_EXISTS, "document already exists");
            }
            doc.setCreated(Instant.now().toString());
            doc.setModified(Instant.now().toString());
            doc.setMimeType(doc.getMimeType().trim());
            getDatabase().put("paths", doc.getPath(), doc.getPath());
            getDatabase().put("tags", doc.getTags(), doc.getTags());
            getDatabase().put(resolveTableName(doc), doc.getUid(), doc);
            Kernel.getInstance().dispatchEvent(
                    new Event(this.getClass().getSimpleName(), Event.CATEGORY_GENERIC, "CONTENT", null, doc.getUid())
            );
        } catch (KeyValueDBException e) {
            throw new CmsException(CmsException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public void addDocument(Map parameters, String userID, List<String> roles) throws CmsException {
        Document doc = new Document();
        try {
            //System.out.println("SETTING UID:" + (String) parameters.get("uid"));
            doc.setUid((String) parameters.get("uid"));
            //System.out.println("UID SET");
            doc.setAuthor((String) parameters.getOrDefault("author", ""));
            doc.setCommentable(Boolean.parseBoolean((String) parameters.getOrDefault("commentable", "false")));
            doc.setMimeType(((String) parameters.getOrDefault("mimeType", "")).trim());
            doc.setType((String) parameters.getOrDefault("type", ""));
            if (doc.getType().equals(Document.FILE)) {
                String fileLocation = (String) parameters.getOrDefault("file", "");
                String[] fParams = fileLocation.split(";");
                if (fParams.length == 3) {
                    //TODO: move file to default location
                    doc.setContent(moveFile(fParams[2], getFileRoot(), doc.getUid()));
                    doc.setMimeType(fParams[0].substring(fParams[0].indexOf(" ")));
                    try {
                        doc.setSize(Long.parseLong(fParams[1]));
                    } catch (NumberFormatException e) {
                        doc.setSize(0);
                    }

                } else {
                    doc.setContent("");
                    doc.setMimeType("");
                    doc.setSize(0);
                }
            } else {
                doc.setContent((String) parameters.getOrDefault("content", ""));
                doc.setSize(0);
            }
            //doc.setCreatedBy((String) parameters.getOrDefault("createdBy", ""));
            doc.setCreatedBy(userID);
            doc.setLanguage((String) parameters.getOrDefault("language", ""));
            doc.setModified(Instant.now().toString());
            //doc.setName();
            // doc.setPublished(Instant.EPOCH);
            doc.setCreated(Instant.now().toString());
            doc.setStatus("wip");
            doc.setSummary((String) parameters.getOrDefault("summary", ""));
            doc.setTags((String) parameters.getOrDefault("tags", ""));
            doc.setTitle((String) parameters.getOrDefault("title", ""));
            if (getDatabase().containsKey(resolveTableName(doc), doc.getUid())) {
                throw new CmsException(CmsException.ALREADY_EXISTS, "document already exists");
            }
            ruleEngine.checkDocument(doc, roles);
            getDatabase().put("paths", doc.getPath(), doc.getPath());
            getDatabase().put("tags", doc.getTags(), doc.getTags());
            getDatabase().put(resolveTableName(doc), doc.getUid(), doc);
            Kernel.getInstance().dispatchEvent(
                    new Event(this.getClass().getSimpleName(), Event.CATEGORY_GENERIC, "CONTENT", null, doc.getUid())
            );
        } catch (KeyValueDBException ex) {
            throw new CmsException(CmsException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public void updateDocument(Document doc, List<String> roles) throws CmsException {

        //TODO: when status changes from wip to published then doc should be removed from wip table
        if (doc.getLanguage() == null || !supportedLanguages.contains(doc.getLanguage())) {
            throw new CmsException(CmsException.UNSUPPORTED_LANGUAGE);
        }
        ruleEngine.checkDocument(doc, roles);
        Document original = null;
        original = getDocument(doc.getUid(), null, null, null);
        if (original == null) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "original document uid=" + doc.getUid() + " not found"));
            throw new CmsException(CmsException.NOT_FOUND, "original document not found");
        }
        doc.setCreated(original.getCreated());
        doc.setModified(Instant.now().toString());
        try {
            doc.setMimeType(doc.getMimeType().trim());
            if (!doc.getLanguage().equals(original.getLanguage()) || !doc.getStatus().equals(original.getStatus())) {
                getDatabase().remove(resolveTableName(original), doc.getUid());

            }
            if (!doc.getStatus().equals(original.getStatus())) {
                if (doc.getStatus().equals("published")) {
                    doc.setPublished(Instant.now().toString());
                }
            }
            getDatabase().put("paths", doc.getPath(), doc.getPath());
            getDatabase().put("tags", doc.getTags(), doc.getTags());
            getDatabase().put(resolveTableName(doc), doc.getUid(), doc);
        } catch (KeyValueDBException e) {
            Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName(), "error while moving document uid=" + doc.getUid() + " database will be inconsistent"));
            throw new CmsException(CmsException.HELPER_EXCEPTION, e.getMessage());
        }
        Kernel.getInstance().dispatchEvent(
                new Event(this.getClass().getSimpleName(), Event.CATEGORY_GENERIC, "CONTENT", null, doc.getUid())
        );
    }

    @Override
    public void updateDocument(String uid, String language, Map parameters, List<String> roles) throws CmsException {
        boolean statusChanged = false;
        Document doc = getDocument(uid, language);
        if (doc == null) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "original document uid=" + uid + ", language=" + language + " not found"));
            throw new CmsException(CmsException.NOT_FOUND, "original document not found");
        }
        ruleEngine.checkDocument(doc, roles);
        try {
            String actualLanguage = doc.getLanguage(); //its not possible to change document's language
            String actualStatus = doc.getStatus();

            String newTitle = (String) parameters.get("title");
            if (newTitle != null) {
                doc.setTitle(newTitle);
            }
            String newAuthor = (String) parameters.get("author");
            if (newAuthor != null) {
                doc.setAuthor(newAuthor);
            }
            String newPath = (String) parameters.get("path");
            if (newPath != null) {
                doc.setPath(newPath);
            }

            String newSummary = (String) parameters.get("summary");
            if (newSummary != null) {
                doc.setSummary(newSummary);
            }
            String newTags = (String) parameters.get("tags");
            if (newTags != null) {
                doc.setTags(newTags);
            }
            String newType = (String) parameters.get("type");
            if (newType != null) {
                doc.setType(newType);
            }
            String newStatus = (String) parameters.get("status");
            if (newStatus != null) {
                //if (!doc.getStatus().equals(newStatus)) {
                if (!newStatus.equals(actualStatus)) {
                    getDatabase().remove(resolveTableName(doc), doc.getUid());
                    statusChanged = true;
                    if (newStatus.equals("published")) {
                        doc.setPublished(Instant.now().toString());
                    }
                }
                doc.setStatus(newStatus);
            }

            //if (doc.getType().equals(Document.FILE)) {
            if (Document.FILE.equals(doc.getType())) {
                String fileLocation = (String) parameters.getOrDefault("file", "");
                String[] fParams = fileLocation.split(";");
                if (fParams.length == 3) {
                    //new file uploaded
                    //TODO: move file to default location, overwrite existing one
                    if ("published".equalsIgnoreCase(doc.getStatus())) {
                        doc.setContent(moveFile(fParams[2], getPublishedFilesRoot(), doc.getUid()));
                    } else {
                        doc.setContent(moveFile(fParams[2], getFileRoot(), doc.getUid()));
                    }
                    doc.setMimeType(fParams[0].substring(fParams[0].indexOf(" ")));
                    try {
                        doc.setSize(Long.parseLong(fParams[1]));
                    } catch (NumberFormatException e) {
                        doc.setSize(0);
                    }

                } else {
                    if (statusChanged) {
                        //move file
                        doc.setContent(moveFile(doc.getContent(), getPublishedFilesRoot(), doc.getUid()));
                    }
                }
            } else {
                String newContent = (String) parameters.get("content");
                if (newContent != null) {
                    doc.setContent(newContent);
                }
                String newMimeType = (String) parameters.get("mimeType");
                if (newMimeType != null) {
                    doc.setMimeType(newMimeType.trim());
                }
                doc.setSize(0);
            }

            doc.setModified(Instant.now().toString());
            getDatabase().put("paths", doc.getPath(), doc.getPath());
            getDatabase().put("tags", doc.getTags(), doc.getTags());
            getDatabase().put(resolveTableName(doc), doc.getUid(), doc);
        } catch (KeyValueDBException e) {
            Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName(), "error while moving document uid=" + doc.getUid() + " database will be inconsistent"));
            throw new CmsException(CmsException.HELPER_EXCEPTION, e.getMessage());
        }
        Kernel.getInstance().dispatchEvent(
                new Event(this.getClass().getSimpleName(), Event.CATEGORY_GENERIC, "CONTENT", null, doc.getUid())
        );
    }

    @Override
    public void removeDocument(String uid, List<String> roles) throws CmsException {
        boolean removed = false;
        Document doc = getDocument(uid, null, null, null);
        ruleEngine.checkDocument(doc, roles);
        if (doc != null) {
            if (Document.FILE.equals(doc.getType())) {
                //TODO: delete file (
                String filePath = doc.getContent();
            }
            String docStatus = doc.getStatus();
            for (int i = 0; i < supportedLanguages.size(); i++) {
                try {
                    getDatabase().remove(docStatus + "_" + supportedLanguages.get(i), uid);
                    removed = true;
                } catch (KeyValueDBException ex) {
                    //OK, not in this table
                }
            }
        }
        if (!removed) {
            throw new CmsException(CmsException.HELPER_EXCEPTION, "not found");
        } else {
            Kernel.getInstance().dispatchEvent(
                    new Event(this.getClass().getSimpleName(), Event.CATEGORY_GENERIC, "CONTENT", null, doc.getUid())
            );
            //TODO: remove path if thera are no more documents with this path 
        }
    }

    /*
    @Override
    public List<Document> findByPath(String path, String language, String status) throws CmsException {
        Document pattern = new Document();
        if (!supportedLanguages.contains(language)) {
            throw new CmsException(CmsException.UNSUPPORTED_LANGUAGE, language + " language is not supported");
        }
        if (!supportedStatuses.contains(status)) {
            throw new CmsException(CmsException.UNSUPPORTED_STATUS, status + " status is not supported");
        }
        pattern.setStatus(status);
        pattern.setPath(path);
        pattern.setLanguage(language);
        String tableName = status + "_" + language;
        ArrayList list = new ArrayList();
        try {
            list = (ArrayList) getDatabase().search(tableName, new DocumentPathComparator(), pattern);
        } catch (KeyValueDBException ex) {
            throw new CmsException(CmsException.HELPER_EXCEPTION, ex.getMessage());
        }
        return list;
    }
     */
    @Override
    public List<Document> findByPathAndTag(String path, String tag, String language, String status, List<String> roles) throws CmsException {
        Document pattern = new Document();
        if (!supportedLanguages.contains(language)) {
            throw new CmsException(CmsException.UNSUPPORTED_LANGUAGE, language + " language is not supported");
        }
        if (!supportedStatuses.contains(status)) {
            throw new CmsException(CmsException.UNSUPPORTED_STATUS, status + " status is not supported");
        }
        pattern.setStatus(status);
        pattern.forceTags(tag);
        pattern.setPath(path);
        pattern.setLanguage(language);
        String tableName = status + "_" + language;
        ArrayList list = new ArrayList();
        try {
            list = (ArrayList) getDatabase().search(tableName, new DocumentPathAndTagComparator(), pattern);
        } catch (KeyValueDBException ex) {
            throw new CmsException(CmsException.HELPER_EXCEPTION, ex.getMessage());
        }
        //TODO: check access rules
        list = (ArrayList) ruleEngine.processDocumentsList(list, roles);
        return list;
    }

    @Override
    public List<Comment> getComments(String uid) throws CmsException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addComment(String documentUid, Comment comment) throws CmsException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void acceptComment(String documentUid, String commentUid) throws CmsException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeComment(String documentUid, String commentUid) throws CmsException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void destroy() {
    }

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        // no specific config is required
        helperAdapterName = properties.get("helper-name");
        Kernel.getInstance().getLogger().print("\thelper-name: " + helperAdapterName);
        ruleEngineName = properties.get("rule-engine");
        Kernel.getInstance().getLogger().print("\trule-engine: " + ruleEngineName);
        initRuleEngine();
        setWwwRoot(properties.get("root-path"));
        Kernel.getInstance().getLogger().print("\troot-path: " + getWwwRoot());
        setFileRoot(properties.get("file-path"));
        Kernel.getInstance().getLogger().print("\tfile-path: " + getFileRoot());
        setPublishedFilesRoot(properties.get("file-path-published"));
        Kernel.getInstance().getLogger().print("\tfile-path-published: " + getPublishedFilesRoot());

        supportedLanguages = new ArrayList<>();
        supportedLanguages.add("pl");
        supportedLanguages.add("en");
        supportedLanguages.add("fr");
        supportedLanguages.add("it");
        supportedStatuses = new ArrayList<>();
        supportedStatuses.add("wip");
        supportedStatuses.add("published");
    }

    @Override
    public List getPaths() throws CmsException {
        ArrayList<String> list = new ArrayList<>();
        try {
            Map map = getDatabase().getAll("paths");
            map.keySet().forEach(key -> {
                list.add((String) key);
            });
        } catch (KeyValueDBException ex) {
            throw new CmsException(CmsException.HELPER_EXCEPTION, ex.getMessage());
        }
        return list;
    }

    @Override
    public List getTags() throws CmsException {
        ArrayList<String> list = new ArrayList<>();
        try {
            Map map = getDatabase().getAll("tags");
            map.keySet().forEach(key -> {
                list.add((String) key);
            });
        } catch (KeyValueDBException ex) {
            throw new CmsException(CmsException.HELPER_EXCEPTION, ex.getMessage());
        }
        return list;
    }

    /*
    @Override
    public Result getFile(String uid, String language) throws CmsException {
        ParameterMapResult result = new ParameterMapResult();

        Document doc = getDocument(uid, language);
        if (doc == null) {
            result.setCode(HttpAdapter.SC_NOT_FOUND);
            return result;
        }
        if (!doc.getType().equals(Document.FILE)) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            result.setMessage("not a file content");
            return result;
        }
        result.setCode(HttpAdapter.SC_OK);
        result.setMessage("");
        result.setPayload(doc.getContent().getBytes());
        String fileExtension = "";
        int index = doc.getDbName().lastIndexOf(".");
        if (index > 0) {
            fileExtension = doc.getDbName().substring(index);
        }
        result.setFileExtension(fileExtension);
        result.setModificationDate(Date.from(doc.getModified()));

        return result;
    }
     */
    @Override
    public void updateCache(RequestObject request, KeyValueDBIface cache, String tableName, String language, String fileContent) {
        FileObject fo;
        String filePath = getFilePath(request);
        try {
            fo = (FileObject) cache.get(tableName, filePath);
            fo.content = fileContent.getBytes();
            cache.put(tableName, filePath, cache);
        } catch (KeyValueDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Result getFile(RequestObject request, KeyValueDBIface cache, String tableName, String language) {
        return getFile(request, cache, tableName, language, true);
    }

    @Override
    public Result getFile(RequestObject request, KeyValueDBIface cache, String tableName, String language, boolean updateCache) {
        String filePath = getFilePath(request);
        byte[] content;
        byte[] emptyContent = {};
        ParameterMapResult result = new ParameterMapResult();
        result.setData(request.parameters);
        String modificationString = request.headers.getFirst("If-Modified-Since");
        Date modificationPoint = null;
        if (modificationString != null) {
            SimpleDateFormat dt1 = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
            try {
                modificationPoint = dt1.parse(modificationString);
            } catch (ParseException e) {
                //e.printStackTrace();
            }
            if (null == modificationPoint) {
                modificationString = modificationString.substring(modificationString.indexOf(",") + 2);
                dt1 = new SimpleDateFormat("d MMM yyyy HH:mm:ss z");
                try {
                    modificationPoint = dt1.parse(modificationString);
                } catch (ParseException e) {
                    //e.printStackTrace();
                }
            }
        }
        FileObject fo = null;
        boolean fileReady = false;

        // we can use cache if available
        if (cache != null) {
            try {
                try {
                    fo = (FileObject) cache.get(tableName, filePath);
                } catch (KeyValueDBException e) {
                    e.printStackTrace();
                }
                if (fo != null) {
                    fileReady = true;
                    result.setCode(HttpAdapter.SC_OK);
                    result.setMessage("");
                    result.setPayload(fo.content);
                    result.setFileExtension(fo.fileExtension);
                    result.setModificationDate(fo.modified);
                    if (fo.mimeType != null && !fo.mimeType.isEmpty()) {
                        result.setHeader("Content-type", fo.mimeType);
                    }
                    if (!isModifiedSince(fo.modified, modificationPoint)) {
                        //System.out.println("NOT MODIFIED");
                        result.setPayload("".getBytes());
                        result.setCode(HttpAdapter.SC_NOT_MODIFIED);
                    }
                    result.setHeader("X-from-cache", "true");
                    Kernel.getInstance().dispatchEvent(Event.logFine(this.getClass().getSimpleName(), "read from cache"));
                    return result;
                }
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }

        // if not in cache
        // find in CMS
        Document doc;

        try {
            doc = getDocument("/" + filePath, language, "published", null);
            if (doc != null && Document.FILE.equals(doc.getType())) {
                File file = new File(doc.getContent());
                content = readFile(file);
            } else if (doc != null && Document.CODE.equals(doc.getType())) {
                try {
                    //content = Base64.getDecoder().decode(doc.getContent());
                    content = URLDecoder.decode(doc.getContent(), "UTF-8").getBytes();
                } catch (UnsupportedEncodingException ex) {
                    content = "".getBytes();
                }
            } else {
                // do nothing
                content = emptyContent;
            }
            //System.out.println("CMS<-" + "/" + filePath + " doc=" + doc + " size=" + content.length);
            if (content.length > 0) {
                fo = new FileObject();
                fo.content = content;
                if (doc.getModified() != null) {
                    fo.modified = Date.from(Instant.from(ISO_INSTANT.parse(doc.getModified())));//Date.from(doc.getModified());
                } else {
                    if (doc.getCreated() != null) {
                        fo.modified = Date.from(Instant.from(ISO_INSTANT.parse(doc.getCreated()))); //Date.from(doc.getCreated());
                    } else {
                        fo.modified = Date.from(Instant.now());
                    }
                }
                fo.filePath = filePath;
                fo.fileExtension = getFileExt(filePath);
                if (doc.getMimeType() != null && !doc.getMimeType().isEmpty()) {
                    result.setHeader("Content-type", doc.getMimeType());
                    fo.mimeType = doc.getMimeType();
                }
                if (cache != null && updateCache && content.length > 0) {
                    try {
                        cache.put(tableName, filePath, fo);
                    } catch (KeyValueDBException e) {
                        Kernel.getInstance().dispatchEvent(Event.logFine(this.getClass().getSimpleName(), e.getMessage()));
                    }
                }
                result.setMessage("");
                result.setFileExtension(fo.fileExtension);
                result.setModificationDate(fo.modified);
                //TODO: mime-type
                result.setCode(HttpAdapter.SC_OK);
                result.setPayload(fo.content);

                Kernel.getInstance().dispatchEvent(Event.logFine(this.getClass().getSimpleName(), "read from CMS"));
                return result;
            }
        } catch (CmsException ex) {
            // do nothing
            ex.printStackTrace();
        }

        // if not found in CMS
        // read from disk
        if (!fileReady) {
            File file = new File(getWwwRoot() + filePath);
            content = readFile(file);
            if (content.length == 0) {
                // file not found or empty file
                result.setCode(HttpAdapter.SC_NOT_FOUND);
                result.setMessage("file not found");
                result.setPayload("file not found".getBytes());
                return result;
            }
            fo = new FileObject();
            fo.content = content;
            fo.modified = new Date(file.lastModified());
            fo.filePath = filePath;
            fo.fileExtension = getFileExt(filePath);
            if (cache != null && content.length > 0) {
                try {
                    cache.put(tableName, filePath, fo);
                } catch (KeyValueDBException e) {
                    e.printStackTrace();
                    Kernel.getInstance().dispatchEvent(Event.logFine(this.getClass().getSimpleName(), e.getMessage()));
                }
            }
        }
        result.setMessage("");
        result.setFileExtension(fo.fileExtension);
        result.setModificationDate(fo.modified);

        if (!isModifiedSince(fo.modified, modificationPoint)) {
            //System.out.println("NOT MODIFIED");
            result.setPayload("".getBytes());
            result.setCode(HttpAdapter.SC_NOT_MODIFIED);
        } else {
            result.setCode(HttpAdapter.SC_OK);
            result.setPayload(fo.content);
        }
        Kernel.getInstance().dispatchEvent(Event.logFine(this.getClass().getSimpleName(), "read from disk"));
        return result;
    }

    /**
     * Sets the root path
     *
     * @param wwwRoot
     */
    private void setWwwRoot(String wwwRoot) {
        this.wwwRoot = wwwRoot;
    }

    /**
     * The root path is prepended to the file path while reading file content
     *
     * @return root path
     */
    private String getWwwRoot() {
        return wwwRoot;
    }

    public String getFilePath(RequestObject request) {
        String filePath = request.pathExt;
        if (filePath.isEmpty() || filePath.endsWith("/")) {
            filePath = filePath.concat(indexFileName);
        }
        return filePath;
    }

    public String getFileExt(String filePath) {
        if (filePath.lastIndexOf(".") > 0) {
            return filePath.substring(filePath.lastIndexOf("."));
        } else {
            return "";
        }
    }

    public byte[] readFile(File file) {
        byte[] result = new byte[(int) file.length()];
        InputStream input = null;
        try {
            int totalBytesRead = 0;
            input = new BufferedInputStream(new FileInputStream(file));
            while (totalBytesRead < result.length) {
                int bytesRemaining = result.length - totalBytesRead;
                //input.read() returns -1, 0, or more :
                int bytesRead = input.read(result, totalBytesRead, bytesRemaining);
                if (bytesRead > 0) {
                    totalBytesRead = totalBytesRead + bytesRead;
                }
            }
            //input.close();
        } catch (IOException ex) {
            byte[] emptyContent = {};
            result = emptyContent;
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
            }
        }
        return result;
    }

    private boolean isModifiedSince(Date modified, Date since) {
        if (since == null) {
            return true;
        }
        boolean modif = modified.after(since);
        return modif;
    }

    /**
     * @return the fileRoot
     */
    public String getFileRoot() {
        return fileRoot;
    }

    /**
     * @param fileRoot the fileRoot to set
     */
    public void setFileRoot(String fileRoot) {
        if (fileRoot.startsWith(".")) {
            this.fileRoot = System.getProperty("user.dir") + fileRoot.substring(1);
        } else {
            this.fileRoot = fileRoot;
        }
    }

    /**
     * @return the publishedFilesRoot
     */
    public String getPublishedFilesRoot() {
        return publishedFilesRoot;
    }

    /**
     * @param publishedFilesRoot the publishedFilesRoot to set
     */
    public void setPublishedFilesRoot(String publishedFilesRoot) {
        if (publishedFilesRoot.startsWith(".")) {
            this.publishedFilesRoot = System.getProperty("user.dir") + publishedFilesRoot.substring(1);
        } else {
            this.publishedFilesRoot = publishedFilesRoot;
        }
    }

    private String moveFile(String sourcePath, String targetRoot, String uid) {
        if (sourcePath == null) {
            return "";
        }
        try {
            //String targetLocation = System.getProperty("user.dir") + targetRoot.substring(1) + uid.substring(1) + getFileExt(sourcePath);
            //String targetLocation = System.getProperty("user.dir") + targetRoot.substring(1) + Kernel.getEventId() + getFileExt(sourcePath);
            String targetLocation = targetRoot + Kernel.getEventId() + getFileExt(sourcePath);
            Files.move(Paths.get(sourcePath), Paths.get(targetLocation), REPLACE_EXISTING);
            return targetLocation;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
