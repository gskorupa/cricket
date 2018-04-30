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
    public void addDocument(Map parameters, String userID) throws CmsException;
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
    public Result getFile(RequestObject request, KeyValueDBIface cache, String tableName, String language, boolean updateCache);
    public void updateCache(RequestObject request, KeyValueDBIface cache, String tableName, String language, String fileContent);
}
