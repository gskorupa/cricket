/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cricketmsf.microsite.cms;

import java.time.Instant;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author greg
 */
@XmlRootElement
public class Document {

    public static String PAGE = "PAGE";
    public static String ARTICLE = "ARTICLE";
    public static String CODE = "CODE";
    public static String FILE = "FILE";

    private String uid;
    private String author;
    private String type;
    private String name;
    private String path;
    private String title;
    private String summary;
    private String content; // not encoded (previously Base64 encoded) 
    private String tags;
    private String language;
    private boolean commentable;
    private String mimeType;
    private String status;
    private long size;
    /*
    private Instant created;
    private Instant modified;
    private Instant published;
    */
    private String created;
    private String modified;
    private String published;
    private String createdBy;

    public Document() {

    }

    /**
     * @return the uid
     */
    public String getUid() {
        return uid;
    }

    /**
     * @param uid the uid to set
     */
    public void setUid(String uid) throws CmsException {
        if (uid != null && !uid.isEmpty()) {
            if (uid.startsWith("/")) {
                this.uid = uid;
            } else {
                this.uid = "/" + uid;
            }
            setPath(this.uid.substring(0, this.uid.lastIndexOf("/") + 1));
            setName(this.uid.substring(this.uid.lastIndexOf("/") + 1));
        } else {
            throw new CmsException(CmsException.MALFORMED_UID, "malformed document uid");
        }
    }

    public void validateUid() {
        if (!this.uid.startsWith("/")) {
            this.uid = "/" + this.uid;
        }
        setPath(this.uid.substring(0, this.uid.lastIndexOf("/") + 1));
        setName(this.uid.substring(this.uid.lastIndexOf("/") + 1));
    }

    public String getAuthor() {
        return author;
    }

    /**
     * @param uid the uid to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
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
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @param summary the summary to set
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    /**
     * @return the content
     */
    /*public String decodeContent() {
        String decoded = content != null ? content : "";
        try {
            decoded = new String(Base64.getDecoder().decode(decoded));
        } catch (IllegalArgumentException ex) {
            // not encoded -> nothing to do
        } catch (NullPointerException ex) {
            return "";
        }
        return decoded;
    }*/

    /**
     * @param content the content to set. Must be escaped
     */
    public void setContent(String content) {
        /*String decoded = content != null ? content : "";
        try {
            decoded = new String(Base64.getDecoder().decode(decoded));
        } catch (IllegalArgumentException e) {
        }
        this.content = decoded;
        */
        this.content=content;
    }

    /**
     * @return the tags
     */
    public String[] tagsAsArray() {
        return tags.split(",");
    }

    public String getTags() {
        return tags;
    }

    /**
     * @param tags the tags to set
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    /**
     * @param tags the tags to set
     */
    public void arrayToTags(String[] tagsArray) {
        String tags = "";
        for (int i = 0; i < tagsArray.length; i++) {
            tags.concat(tagsArray[i]);
            if (i < tagsArray.length - 1) {
                tags.concat(",");
            }
        }
        this.tags = tags;
    }

    /**
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @param language the language to set
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * @return the commentable
     */
    public boolean isCommentable() {
        return commentable;
    }

    /**
     * @param commentable the commentable to set
     */
    public void setCommentable(boolean commentable) {
        this.commentable = commentable;
    }

    /**
     * @return the metadata
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @param metadata the metadata to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
        /*
        try {
            Instant.parse((String) get("created"));
            put("created", created);
        } catch (DateTimeParseException | ClassCastException e) {
            e.printStackTrace();
            put("created", "");
        }
         */
    }

    public String getModified() {
        return modified;
        /*
        try {
            return Instant.parse((String) get("updated"));
        } catch (DateTimeParseException | ClassCastException e) {
            e.printStackTrace();
        }
        return null;
         */
    }

    public void setModified(String modified) {
        this.modified = modified;
        /*
        try {
            Instant.parse((String) get("updated"));
            put("updated", updated);
        } catch (DateTimeParseException | ClassCastException e) {
            e.printStackTrace();
            put("updated", "");
        }
         */
    }

    public String getPublished() {
        return published;
        /*
        try {
            return Instant.parse((String) get("published"));
        } catch (DateTimeParseException | ClassCastException e) {
            e.printStackTrace();
        }
        return null;
         */
    }

    public void setPublished(String published) {
        this.published = published;
        /*
        try {
            Instant.parse((String) get("published"));
            put("published", published);
        } catch (DateTimeParseException | ClassCastException e) {
            e.printStackTrace();
            put("published", "");
        }
         */
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

}
