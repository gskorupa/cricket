/*
 * Copyright 2020 Grzegorz Skorupa .
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
package org.cricketmsf.microsite.in.siteadmin;

import java.util.Base64;
import java.util.HashMap;
import org.cricketmsf.RequestObject;
import org.cricketmsf.event.Procedures;
import org.cricketmsf.event.ProcedureCall;
import org.cricketmsf.in.http.HttpPortedAdapter;
import org.cricketmsf.api.ResponseCode;
import org.cricketmsf.api.ResultIface;
import org.cricketmsf.api.StandardResult;
import org.cricketmsf.event.Event;
import org.cricketmsf.microsite.event.auth.AuthEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Grzegorz Skorupa 
 */
public class SiteAdminApi extends HttpPortedAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SiteAdminApi.class);

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
        logger.info("\tcontext=" + getContext());
    }

    @Override
    protected ProcedureCall preprocess(RequestObject request) {
        if ("post".equalsIgnoreCase(request.method)) {
            Event event=new Event();
            event.setData(request);
            return ProcedureCall.toForward(event, Procedures.SA_ANY);
        } else if ("get".equalsIgnoreCase(request.method)) {
            Event event=new Event();
            event.setData(request);
            return ProcedureCall.toForward(event, Procedures.SA_ANY);
        } else if ("options".equalsIgnoreCase(request.method)) {
            return ProcedureCall.toRespond(ResponseCode.OK, "");
        } else {
            return ProcedureCall.toRespond(ResponseCode.METHOD_NOT_ALLOWED, "");
        }
    }

}
