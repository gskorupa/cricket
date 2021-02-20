/*
 * Copyright 2018-2020 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
public class ContentServiceApiHttp extends HttpPortedAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ContentServiceApiHttp.class);

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
        logger.info("\tcontext: {}", getContext());
        logger.info("\tdefault-language: {}", defaultLanguage);
    }

    @Override
    protected ProcedureCall preprocess(RequestObject request) {
        if ("get".equalsIgnoreCase(request.method)) {
            return preprocessGet(request);
        } else if ("options".equalsIgnoreCase(request.method)) {
            return ProcedureCall.toRespond(ResponseCode.OK, "");
        } else {
            return ProcedureCall.toRespond(ResponseCode.METHOD_NOT_ALLOWED, "");
        }
    }

    private ProcedureCall preprocessGet(RequestObject request) {
        String pathExt = request.pathExt;
        String userID = request.headers.getFirst("X-user-id");
        List<String> roles = request.headers.get("X-user-role");

        String requiredStatus = (String) request.parameters.get("status");
        String language = (String) request.parameters.getOrDefault("language", defaultLanguage);
        HashMap params = new HashMap();
        params.put("pathext", pathExt);
        params.put("userid", userID);
        params.put("userroles", roles);
        params.put("status", requiredStatus);
        params.put("language", language);

        if (!"published".equals(requiredStatus) || pathExt.isBlank()) {
            return ProcedureCall.toRespond(ResponseCode.FORBIDDEN, "access denied");
        } else {
            return ProcedureCall.toForward(new CmsEvent(params), Procedures.CS_GET);
        }
    }

}
