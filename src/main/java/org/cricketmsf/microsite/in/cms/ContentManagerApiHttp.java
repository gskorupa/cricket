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
package org.cricketmsf.microsite.in.cms;

import java.util.HashMap;
import java.util.List;
import org.cricketmsf.RequestObject;
import org.cricketmsf.api.ResponseCode;
import org.cricketmsf.event.ProcedureCall;
import org.cricketmsf.event.Procedures;
import org.cricketmsf.in.http.HttpPortedAdapter;
import org.cricketmsf.microsite.event.CmsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class ContentManagerApiHttp extends HttpPortedAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ContentManagerApiHttp.class);

    private static final String ADMIN = "admin";
    private static final String REDACTOR = "redactor";
    private static final String LANG_REDACTOR = "redactor.";
    
    private String defaultLanguage;

    /**
     * This method is executed while adapter is instantiated during the service
     * start. It's used to configure the adapter according to the configuration.
     *
     * @param properties map of properties readed from the configuration file
     * @param adapterName name of the adapter set in the configuration file (can
     * be different from the interface and class name.
     */
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        //super.getServiceHooks(adapterName);
        setContext(properties.get("context"));
        defaultLanguage = properties.getOrDefault("default-language", "en");
        logger.info("\tdefault-language: {}", defaultLanguage);
        logger.info("\tcontext: " + getContext());
    }

    @Override
    protected ProcedureCall preprocess(RequestObject request) {
        if ("options".equalsIgnoreCase(request.method)) {
            return ProcedureCall.toRespond(ResponseCode.OK, "");
        } else if ("get".equalsIgnoreCase(request.method)) {
            return preprocessGet(request);
        } else if ("post".equalsIgnoreCase(request.method)) {
            return preprocessPost(request);
        } else if ("put".equalsIgnoreCase(request.method)) {
            return preprocessPut(request);
        } else if ("delete".equalsIgnoreCase(request.method)) {
            return preprocessDelete(request);
        } else {
            return ProcedureCall.toRespond(ResponseCode.METHOD_NOT_ALLOWED, "");
        }
    }

    private ProcedureCall preprocessGet(RequestObject request) {
        String pathExt = request.pathExt;
        String pathsonly = (String) request.parameters.get("pathsonly");
        String tagsonly = (String) request.parameters.get("tagsonly");
        String path = (String) request.parameters.get("path");
        String tag = (String) request.parameters.get("tag");
        String userID = request.headers.getFirst("X-user-id");
        List<String> roles = request.headers.get("X-user-role");
        String requiredStatus = (String) request.parameters.get("status");
        String language = (String) request.parameters.getOrDefault("language", defaultLanguage);
        
        HashMap params = new HashMap();
        params.put("pathext", pathExt);
        params.put("path", path);
        params.put("tag", tag);
        params.put("userid", userID);
        params.put("userroles", roles);
        params.put("status", requiredStatus);
        params.put("language", language);
        params.put("pathsonly",pathsonly);
        params.put("tagsonly",tagsonly);

        return ProcedureCall.toForward(new CmsEvent(params), Procedures.CMS_GET);
    }
    
    private ProcedureCall preprocessPost(RequestObject request) {
        String userID = request.headers.getFirst("X-user-id");
        List<String> roles = request.headers.get("X-user-role");

        String requiredStatus = (String) request.parameters.get("status");
        String language = (String) request.parameters.getOrDefault("language", defaultLanguage);

        if (!hasAccessRights(userID, roles, language)) {
            return ProcedureCall.toRespond(ResponseCode.FORBIDDEN, "access denied");
        } else {
            return ProcedureCall.toForward(new CmsEvent(request), Procedures.CMS_POST);
        }
    }
    
    private ProcedureCall preprocessPut(RequestObject request) {
        String userID = request.headers.getFirst("X-user-id");
        List<String> roles = request.headers.get("X-user-role");

        String requiredStatus = (String) request.parameters.get("status");
        String language = (String) request.parameters.getOrDefault("language", defaultLanguage);

        if (!hasAccessRights(userID, roles, language)) {
            return ProcedureCall.toRespond(ResponseCode.FORBIDDEN, "access denied");
        } else {
            return ProcedureCall.toForward(new CmsEvent(request), Procedures.CMS_PUT);
        }
    }
    
    private ProcedureCall preprocessDelete(RequestObject request) {
        String userID = request.headers.getFirst("X-user-id");
        List<String> roles = request.headers.get("X-user-role");

        String requiredStatus = (String) request.parameters.get("status");
        String language = (String) request.parameters.getOrDefault("language", defaultLanguage);

        if (!hasAccessRights(userID, roles, language)) {
            return ProcedureCall.toRespond(ResponseCode.FORBIDDEN, "access denied");
        } else {
            return ProcedureCall.toForward(new CmsEvent(request), Procedures.CMS_DELETE);
        }
    }

    private boolean hasAccessRights(String userID, List<String> roles, String language) {

        if (userID == null || userID.isEmpty()) {
            return false;
        }
        if (roles.contains(ADMIN) || roles.contains(REDACTOR)) {
            return true;
        } else {
            for (String role : roles) {
                if (role.equals(LANG_REDACTOR + language)) {
                    return true;
                }
            }
            return false;
        }
    }

}
