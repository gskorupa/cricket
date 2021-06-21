/*
 * Copyright 2017 Grzegorz Skorupa .
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
package org.cricketmsf.microsite.out.cms;

import com.cedarsoftware.util.io.JsonReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.api.ResponseCode;
import org.cricketmsf.api.StandardResult;
import org.cricketmsf.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Grzegorz Skorupa 
 */
public class ContentRequestProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ContentRequestProcessor.class);
    private static String ADMIN = "admin";
    private static String REDACTOR = "redactor";
    private static String LANG_REDACTOR = "redactor.";

    private boolean hasAccessRights(String userID, List<String> roles) {
        if (userID == null || userID.isEmpty()) {
            return false;
        }
        if (roles.contains(ADMIN) || roles.contains(REDACTOR)) {
            return true;
        } else {
            for (String role : roles) {
                if (role.startsWith(LANG_REDACTOR)) {
                    return true;
                }
            }
            return false;
        }
    }

    /*
    public Object processRequest(Event event, CmsIface adapter, TranslatorIface translator) {
        String method = event.getRequest().method.toUpperCase();
        switch (method) {
            case "GET":
                return processGet(event, adapter);
            case "POST":
                return processPost(event, adapter);
            case "PUT":
                return processPut(event, adapter);
            case "PATCH":
                return processPatch(event, adapter, translator);
            case "DELETE":
                return processDelete(event, adapter);
            default:
                StandardResult result = new StandardResult();
                result.setCode(HttpAdapter.SC_METHOD_NOT_ALLOWED);
                return result;
        }
    }
    */
    
    public Object processGetPublished(HashMap params, CmsIface adapter) {
        StandardResult result = new StandardResult();

        String language = (String) params.get("language");
        String pathExt = (String) params.get("pathext");
        String path = (String) params.get("path");
        String tag = (String) params.get("tag");
        Document doc;
        if (pathExt != null && !pathExt.isEmpty()) {
            try {
                doc = adapter.getDocument("/" + pathExt, language, "published", null);
                if (null == doc && null != adapter.getDefaultLanguage()) {
                    doc = adapter.getDocument("/" + pathExt, adapter.getDefaultLanguage(), "published", null);
                }
                if (doc != null) {
                    if (doc.getType() == null ? false : doc.getType().equals(Document.FILE)) {
                        doc.setContent("*****");
                    }
                    result.setData(doc);
                } else {
                    result.setCode(ResponseCode.NOT_FOUND);
                }
            } catch (CmsException ex) {
                logger.warn(ex.getMessage());
                result.setCode(ResponseCode.NOT_FOUND);
                result.setMessage(ex.getMessage());
                result.setData(ex.getMessage());
            }
        } else {
            String pathsOnly = (String)params.get("pathsonly"/*, "false"*/);
            String tagsOnly = (String)params.get("tagsonly"/*, "false"*/);
            try {
                if ("true".equalsIgnoreCase(pathsOnly)) {
                    result.setData(adapter.getPaths());
                } else if ("true".equalsIgnoreCase(tagsOnly)) {
                    result.setData(adapter.getTags());
                } else {
                    result.setData(adapter.findByPathAndTag(path, tag, language, "published", null));
                }
            } catch (CmsException ex) {
                logger.warn(ex.getMessage());
                result.setCode(ResponseCode.NOT_FOUND);
                result.setMessage(ex.getMessage());
                result.setData(ex.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public Object processGet(HashMap params, CmsIface adapter) {
        StandardResult result = new StandardResult();
        String userID =(String)params.get("userid");
        List<String> roles = (List<String>)params.get("userroles");
        String requiredStatus = (String) params.get("status");
        String language = (String) params.getOrDefault("language", "");
        String pathExt = (String)params.get("pathext");
        
        if ("wip".equals(requiredStatus) && !hasAccessRights(userID, roles)) {
            result.setCode(ResponseCode.FORBIDDEN);
            return result;
        }
        
        Document doc;
        if (pathExt != null && !pathExt.isEmpty()) {
            String uid = "/" + pathExt;
            try {
                doc = adapter.getDocument(uid, language, requiredStatus, roles);
                if (doc != null) {
                    result.setData(doc);
                } else {
                    result.setCode(ResponseCode.NOT_FOUND);
                }
            } catch (CmsException ex) {
                logger.info(ex.getMessage());
                result.setCode(ResponseCode.NOT_FOUND);
                result.setMessage(ex.getMessage());
            }
        } else {
            //find
            String path = (String)params.getOrDefault("path", "");
            String tag = (String)params.getOrDefault("tag", "");
            String pathsOnly = (String)params.getOrDefault("pathsonly", "false");
            String tagsOnly = (String)params.getOrDefault("tagsonly", "false");
            try {
                if ("true".equalsIgnoreCase(pathsOnly)) {
                    result.setData(adapter.getPaths());
                } else if ("true".equalsIgnoreCase(tagsOnly)) {
                    result.setData(adapter.getTags());
                } else {
                    result.setData(adapter.findByPathAndTag(path, tag, language, requiredStatus, roles));
                }
            } catch (CmsException ex) {
                logger.warn(ex.getMessage());
                result.setCode(ResponseCode.NOT_FOUND);
                result.setMessage(ex.getMessage());
                result.setData(ex.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                logger.warn(e.getMessage());
                result.setCode(ResponseCode.NOT_FOUND);
                result.setMessage(e.getMessage());
                result.setData(e.getMessage());
            }
        }
        return result;
    }

    public Object processPost(Event event, CmsIface adapter) {
        RequestObject request = (RequestObject)event.getData();
        StandardResult result = new StandardResult();
        String userID = request.headers.getFirst("X-user-id");
        List<String> roles = request.headers.get("X-user-role");

        if (!hasAccessRights(userID, roles)) {
            result.setCode(ResponseCode.FORBIDDEN);
            return result;
        }

        String contentType = request.headers.getFirst("Content-Type");
        try {
            if ("application/json".equalsIgnoreCase(contentType)) {
                String jsonString = request.body;
                jsonString
                        = "{\"@type\":\"org.cricketmsf.microsite.cms.Document\","
                        + jsonString.substring(jsonString.indexOf("{") + 1);

                Document doc = null;
                try {
                    doc = (Document) JsonReader.jsonToJava(jsonString);
                } catch (Exception e) {
                    logger.error("deserialization problem - check @type declaration");
                    e.printStackTrace();
                }
                try {
                    if (doc != null) {
                        doc.validateUid(); // prepend doc.uid with "/" if needed and update doc.path
                        doc.setStatus("wip");
                        doc.setCreatedBy(userID);
                        doc.setContent(doc.getContent());
                        try {
                            adapter.addDocument(doc, roles);
                        } catch (CmsException ex) {
                            logger.error(ex.getMessage());
                        }
                        result.setData(doc);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    adapter.addDocument(request.parameters, userID, roles);
                    result.setData(adapter.getDocument((String) request.parameters.get("uid"), (String) request.parameters.get("language")));
                } catch (CmsException ex) {
                    logger.error(ex.getMessage());
                    if (ex.getCode() >= 400 && ex.getCode() < 600) {
                        result.setCode(ex.getCode());
                    } else {
                        result.setCode(ResponseCode.BAD_REQUEST);
                    }
                    result.setMessage(ex.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Object processPut(Event event, CmsIface adapter) {

        //TODO: implement document update
        StandardResult result = new StandardResult();
        try {
            RequestObject request = (RequestObject)event.getData();

            String userID = request.headers.getFirst("X-user-id");
            List<String> roles = request.headers.get("X-user-role");
            if (!hasAccessRights(userID, roles)) {
                result.setCode(ResponseCode.FORBIDDEN);
                return result;
            }

            String uid = "/" + request.pathExt;
            if (uid == null || uid.isEmpty()) {
                result.setCode(ResponseCode.NOT_FOUND);
                return result;
            }

            String contentType = request.headers.getFirst("Content-Type");

            Document doc = null;

            if ("application/json".equalsIgnoreCase(contentType)) {
                //create new document and adapter.modify(document)
                String jsonString = request.body;
                jsonString
                        = "{\"@type\":\"org.cricketmsf.microsite.cms.Document\","
                        + jsonString.substring(jsonString.indexOf("{") + 1);

                try {
                    doc = (Document) JsonReader.jsonToJava(jsonString);
                    doc.setUid(uid); //overwrite uid and path from JSON representation
                } catch (Exception e) {
                    logger.error("deserialization problem - check @type declaration");
                    e.printStackTrace();
                    result.setCode(ResponseCode.BAD_REQUEST);
                    return result;
                }
                try {
                    adapter.updateDocument(doc, roles);
                    result.setData(doc);
                } catch (CmsException ex) {
                    logger.error(ex.getMessage());
                    result.setCode(ResponseCode.BAD_REQUEST);
                }
            } else {
                try {
                    adapter.updateDocument(uid, (String) request.parameters.get("language"), request.parameters, roles);
                    result.setData(adapter.getDocument(uid, (String) request.parameters.get("language")));
                } catch (CmsException ex) {
                    logger.error(ex.getMessage());
                    result.setCode(ResponseCode.BAD_REQUEST);
                }
                //read original document, update parameters and adapter.modify(original)

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Object processPatch(Event event, CmsIface adapter, TranslatorIface translator) {

        StandardResult result = new StandardResult();
        try {
            RequestObject request = (RequestObject)event.getData();

            String userID = request.headers.getFirst("X-user-id");
            List<String> roles = request.headers.get("X-user-role");
            if (!hasAccessRights(userID, roles)) {
                result.setCode(ResponseCode.FORBIDDEN);
                return result;
            }

            String uid = "/" + request.pathExt;
            if (uid == null || uid.isEmpty()) {
                result.setCode(ResponseCode.NOT_FOUND);
                return result;
            }
            String newLanguage = request.headers.getFirst("requested-language");
            if (null == newLanguage || newLanguage.isEmpty()) {
                result.setCode(ResponseCode.BAD_REQUEST);
                result.setMessage("empty request parameter: requested-language");
                return result;
            }

            String contentType = request.headers.getFirst("Content-Type");

            Document doc = null;

            if ("application/json".equalsIgnoreCase(contentType)) {
                //create new document and adapter.modify(document)
                String jsonString = request.body;
                jsonString
                        = "{\"@type\":\"org.cricketmsf.microsite.cms.Document\","
                        + jsonString.substring(jsonString.indexOf("{") + 1);

                try {
                    doc = (Document) JsonReader.jsonToJava(jsonString);
                    doc.setUid(uid); //overwrite uid and path from JSON representation
                } catch (Exception e) {
                    logger.error("deserialization problem - check @type declaration");
                    e.printStackTrace();
                    result.setCode(ResponseCode.BAD_REQUEST);
                    return result;
                }
                try {
                    doc = translator.translate(doc, newLanguage);
                    adapter.updateDocument(doc, roles);
                    result.setData(doc);
                } catch (CmsException ex) {
                    logger.error(ex.getMessage());
                    result.setCode(ResponseCode.BAD_REQUEST);
                }
            } else {
                result.setCode(ResponseCode.BAD_REQUEST);
                result.setMessage("Not supported Content-type");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Object processDelete(Event event, CmsIface adapter) {
        //TODO: implement document removal
        StandardResult result = new StandardResult();
        RequestObject request = (RequestObject)event.getData();

        String userID = request.headers.getFirst("X-user-id");
        List<String> roles = request.headers.get("X-user-role");
        if (!hasAccessRights(userID, roles)) {
            result.setCode(ResponseCode.FORBIDDEN);
            return result;
        }

        String uid = "/" + request.pathExt;
        if (uid == null || uid.isEmpty()) {
            result.setCode(ResponseCode.NOT_FOUND);
            return result;
        }
        try {
            adapter.removeDocument(uid, roles);
        } catch (CmsException ex) {
            result.setCode(ResponseCode.NOT_FOUND);
            result.setData(ex.getMessage());
            logger.warn(ex.getMessage());
        }
        return result;
    }

}

