/*
 * Copyright 2017 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package org.cricketmsf.microsite.out.cms;

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
    public static String READWRITE = "rw";
    public static String READONLY = "r";
    
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
    private String created;
    private String modified;
    private String published;
    private String createdBy;
    
    private String rights = READWRITE;
    private String extra;

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
            this.uid=uid;
            validateUid();
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
     * @param content the content to set. Must be escaped
     */
    public void setContent(String content) {
        this.content = content;
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
        this.tags = tags!=null?tags:"";
        if (!this.tags.startsWith(",")) {
            this.tags = "," + this.tags;
        }
        if (!this.tags.endsWith(",")) {
            this.tags = this.tags + ",";
        }
    }

    public void forceTags(String tags) {
        this.tags = tags!=null?tags:"";
    }

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
        this.language = language.toLowerCase();
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
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return the rights
     */
    public String getRights() {
        return rights;
    }

    /**
     * @param rights the rights to set
     */
    public Document setRights(String rights) {
        this.rights = rights;
        return this;
    }

    /**
     * @return the userDefined
     */
    public String getExtra() {
        return extra;
    }

    /**
     * @param extra the userDefined to set
     */
    public void setExtra(String extra) {
        this.extra = extra;
    }

}
