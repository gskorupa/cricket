/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cricketmsf.microsite.cms;

import java.util.List;
import java.util.Map;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.Result;
import org.cricketmsf.out.db.KeyValueDBIface;

/**
 *
 * @author greg
 */
public interface CmsIface {
    public void destroy() throws CmsException;
    
    public List getPaths() throws CmsException;
    public Document getDocument(String uid, String language) throws CmsException;
    public Document getDocument(String uid, String language, String status) throws CmsException;
    public void addDocument(Document doc) throws CmsException;
    public void addDocument(Map parameters) throws CmsException;
    public void updateDocument(Document doc) throws CmsException;
    public void updateDocument(String uid, String language, Map parameters) throws CmsException;
    public void removeDocument(String uid) throws CmsException;
    public List<Document> findByPath(String path, String language, String status) throws CmsException;
    public Document findByTag(String path, String language, String status) throws CmsException;
    public List<Comment> getComments(String uid) throws CmsException;
    public void addComment(String documentUid, Comment comment) throws CmsException;
    public void acceptComment(String documentUid, String commentUid) throws CmsException;
    public void removeComment(String documentUid, String commentUid) throws CmsException;
    //public Result getFile(String uid, String language) throws CmsException;
    public Result getFile(RequestObject request, KeyValueDBIface cache, String tableName, String language);
}
