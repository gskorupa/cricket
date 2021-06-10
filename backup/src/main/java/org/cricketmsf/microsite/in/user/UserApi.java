/*
 * Copyright 2017 Grzegorz Skorupa .
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
package org.cricketmsf.microsite.in.user;

import java.util.HashMap;
import org.cricketmsf.RequestObject;
import org.cricketmsf.event.Procedures;
import org.cricketmsf.event.ProcedureCall;
import org.cricketmsf.in.http.HttpPortedAdapter;
import org.cricketmsf.api.ResponseCode;
import org.cricketmsf.api.ResultIface;
import org.cricketmsf.api.StandardResult;
import org.cricketmsf.microsite.event.UserEvent;
import org.cricketmsf.microsite.out.user.HashMaker;
import org.cricketmsf.microsite.out.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Grzegorz Skorupa 
 */
public class UserApi extends HttpPortedAdapter {

    private static final Logger logger = LoggerFactory.getLogger(UserApi.class);
    private boolean withConfirmation = false;

    /**
     * This method is executed while adapter is instantiated during the service
     * start. It's used to configure the adapter according to the configuration.
     *
     * @param properties map of properties readed from the configuration file
     * @param adapterName firstName of the adapter set in the configuration file (can
 be different from the interface and class firstName.
     */
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        //super.getServiceHooks(adapterName);
        setContext(properties.get("context"));
        withConfirmation = Boolean.parseBoolean(properties.get("confirm-registration"));
        logger.info("\tconfirm-registration: " + withConfirmation);
        logger.info("\tcontext=" + getContext());
    }

    @Override
    protected ProcedureCall preprocess(RequestObject request) {
        if ("post".equalsIgnoreCase(request.method)) {
            return preprocessPost(request);
        } else if ("put".equalsIgnoreCase(request.method)) {
            return preprocessPut(request);
        } else if ("get".equalsIgnoreCase(request.method)) {
            return preprocessGet(request);
        } else if ("delete".equalsIgnoreCase(request.method)) {
            return preprocessDelete(request);
        } else if ("options".equalsIgnoreCase(request.method)) {
            return ProcedureCall.toRespond(ResponseCode.OK, "");
        } else {
            return ProcedureCall.toRespond(ResponseCode.METHOD_NOT_ALLOWED, "");
        }
    }

    private ProcedureCall preprocessGet(RequestObject request) {
        String uid = request.pathExt;
        String requesterID = request.headers.getFirst("X-user-id");
        String userNumber = (String) request.parameters.getOrDefault("n", "");
        String requesterRoles = request.headers.getFirst("X-user-role");
        long number = -1;
        try {
            number = Long.parseLong(userNumber);
        } catch (NumberFormatException e) {
        }
        return ProcedureCall.toForward(new UserEvent(uid, requesterID, number, requesterRoles), Procedures.USER_GET);
    }

    private ProcedureCall preprocessDelete(RequestObject request) {
        String uid = request.pathExt;
        String requesterID = request.headers.getFirst("X-user-id");
        String requesterRoles = request.headers.getFirst("X-user-role");
        return ProcedureCall.toForward(new UserEvent(uid, requesterID, requesterRoles), Procedures.USER_REMOVE);
    }

    private ProcedureCall preprocessPost(RequestObject request) {
        try {
            String uid = (String) request.parameters.get("uid");
            String email = (String) request.parameters.get("email");
            String password = (String) request.parameters.get("password");
            // validate
            boolean valid = true;
            if (uid == null || uid.isEmpty()) {
                valid = false;
            }
            if (withConfirmation && (null == email || email.isEmpty())) {
                valid = false;
            }
            if (password == null || password.isEmpty()) {
                valid = false;
            }
            if (!valid) {
                return ProcedureCall.toRespond(ResponseCode.BAD_REQUEST, "lack of required parameters");
            }
            User newUser = new User();
            newUser.setUid(uid);
            newUser.setEmail(email);
            newUser.setName((String) request.parameters.get("name"));
            newUser.setSurname((String) request.parameters.get("surname"));
            newUser.setRole("");
            newUser.setPassword(HashMaker.md5Java(password));
            String type = (String) request.parameters.get("type");
            if (null != type) {
                switch (type.toUpperCase()) {
                    case "APPLICATION":
                        newUser.setType(User.APPLICATION);
                        break;
                    case "OWNER":
                        newUser.setType(User.OWNER);
                        break;
                    default:
                        newUser.setType(User.USER);
                        break;
                }
            } else {
                newUser.setType(User.USER);
            }
            return ProcedureCall.toForward(new UserEvent(newUser), Procedures.USER_REGISTER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ProcedureCall.toRespond(ResponseCode.BAD_REQUEST, "bad request");
    }

    private ProcedureCall preprocessPut(RequestObject request) {
        String uid = request.pathExt;
        if (uid == null || uid.contains("/")) {
            return ProcedureCall.toRespond(ResponseCode.BAD_REQUEST, "");
        }
        try {
            User user = new User();
            user.clearStatus(); //important!
            user.setUid(uid);
            String email = (String) request.parameters.get("email");
            String type = (String) request.parameters.get("type");
            String role = (String) request.parameters.get("role");
            String password = (String) request.parameters.get("password");
            String confirmed = (String) request.parameters.get("confirmed");
            String firstName = (String) request.parameters.get("name");
            String surname = (String) request.parameters.get("surname");
            String unregisterRequested = (String) request.parameters.get("unregisterRequested");
            if (email != null) {
                user.setEmail(email);
            }
            if (firstName != null) {
                user.setName(firstName);
            }
            if (surname != null) {
                user.setSurname(surname);
            }
            if (role != null) {
                user.setRole(role);
            }
            if (type != null) {
                try {
                    user.setType(Integer.parseInt(type));
                } catch (NumberFormatException e) {
                }
            }
            if (password != null) {
                user.setPassword(HashMaker.md5Java(password));
            }

            if (confirmed != null) {
                user.setConfirmed("true".equalsIgnoreCase(confirmed));
            }
            if (unregisterRequested != null) {
                user.setUnregisterRequested("true".equalsIgnoreCase(unregisterRequested));
            }
            String requesterRoles = request.headers.getFirst("X-user-role");
            String requesterID = request.headers.getFirst("X-user-id");
            return ProcedureCall.toForward(new UserEvent(user, requesterID, requesterRoles), Procedures.USER_UPDATE);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return ProcedureCall.toRespond(ResponseCode.BAD_REQUEST, "");
        }
    }

    @Override
    protected ResultIface postprocess(ResultIface fromService) {

        StandardResult result = new StandardResult();
        result.setCode(fromService.getCode());
        result.setData(fromService.getData());
        switch (fromService.getProcedure()) {
            case Procedures.USER_GET:
                if (null == fromService.getData()) {
                    result.setCode(ResponseCode.UNAUTHORIZED);
                    result.setData("unauthorized (3)");
                }
                break;
            case Procedures.USER_REGISTER:
                break;
            case Procedures.USER_UPDATE:
                break;
            case Procedures.USER_REMOVE:
                break;
            default:
                result.setCode(ResponseCode.BAD_REQUEST);
        }
        return result;
    }
}
