/*
 * Copyright 2020 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package org.cricketmsf.microsite.in.auth;

import org.cricketmsf.microsite.in.user.UserApi;
import java.util.Base64;
import java.util.HashMap;
import org.cricketmsf.RequestObject;
import org.cricketmsf.event.Procedures;
import org.cricketmsf.event.ProcedureCall;
import org.cricketmsf.in.http.HttpPortedAdapter;
import org.cricketmsf.api.ResponseCode;
import org.cricketmsf.api.ResultIface;
import org.cricketmsf.api.StandardResult;
import org.cricketmsf.microsite.event.AuthEvent;
import org.cricketmsf.microsite.out.auth.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class AuthApi extends HttpPortedAdapter {

    private static final Logger logger = LoggerFactory.getLogger(UserApi.class);

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
            return preprocessLogin(request);
        } else if ("put".equalsIgnoreCase(request.method)) {
            return preprocessRefreshToken(request);
        } else if ("get".equalsIgnoreCase(request.method)) {
            return preprocessCheckToken(request);
        } else if ("delete".equalsIgnoreCase(request.method)) {
            return preprocessLogout(request);
        } else if ("options".equalsIgnoreCase(request.method)) {
            return ProcedureCall.toRespond(ResponseCode.OK, "");
        } else {
            return ProcedureCall.toRespond(ResponseCode.METHOD_NOT_ALLOWED, "");
        }
    }

    private ProcedureCall preprocessCheckToken(RequestObject request) {
        String token = request.pathExt;
        return ProcedureCall.toForward(new AuthEvent(null, null, token), Procedures.AUTH_CHECK_TOKEN);
    }

    private ProcedureCall preprocessLogout(RequestObject request) {
        String token = request.pathExt;
        return ProcedureCall.toForward(new AuthEvent(null, null, token), Procedures.AUTH_LOGOUT);
    }

    private ProcedureCall preprocessLogin(RequestObject request) {
        String authData = request.headers.getFirst("Authorization");
        if (authData != null) {
            try {
                String[] s = authData.split(" ");
                if (s.length == 2 && s[0].equals("Basic")) {
                    String authPair = new String(Base64.getDecoder().decode(s[1]));
                    while (authPair.endsWith("\r") || authPair.endsWith("\n")) {
                        authPair = authPair.substring(0, authPair.length() - 1);
                    }
                    s = authPair.split(":");
                    if (s.length == 2) {
                        return ProcedureCall.toForward(new AuthEvent(s[0], s[1], null), Procedures.AUTH_LOGIN);
                    }
                }
            } catch (Exception e) {
                logger.debug(e.getMessage());
            }
            return ProcedureCall.toRespond(ResponseCode.UNAUTHORIZED, "unauthorized (1)");
        } else {
            return ProcedureCall.toRespond(ResponseCode.UNAUTHORIZED, "unauthorized (2)");
        }
    }

    private ProcedureCall preprocessRefreshToken(RequestObject request) {
        String token = request.headers.getFirst("Authorization");
        return ProcedureCall.toForward(new AuthEvent(null, null, token), Procedures.AUTH_REFRESH_TOKEN);
    }

    protected ResultIface postprocess(ResultIface fromService) {
        StandardResult result = new StandardResult();
        switch (fromService.getProcedure()) {
            case Procedures.AUTH_LOGIN:
                if (null != fromService.getData()) {
                    result.setData(fromService.getData());
                } else {
                    result.setCode(ResponseCode.UNAUTHORIZED);
                    result.setData("unauthorized (3)");
                }
                break;
            case Procedures.AUTH_LOGOUT:
            case Procedures.AUTH_CHECK_TOKEN:
            case Procedures.AUTH_REFRESH_TOKEN:
                if (null != fromService.getData() && (Boolean)fromService.getData()) {
                    result.setData(fromService.getData());
                } else {
                    result.setCode(ResponseCode.UNAUTHORIZED);
                    result.setData("unauthorized (4)");
                }
                break;
            default:
                result.setCode(ResponseCode.BAD_REQUEST);
        }
        return result;
    }

}
