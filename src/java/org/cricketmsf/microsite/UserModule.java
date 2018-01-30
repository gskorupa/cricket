/*
 * Copyright 2017 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package org.cricketmsf.microsite;

import java.util.List;
import org.cricketmsf.microsite.user.*;
import java.util.Map;
import org.cricketmsf.Event;
import static org.cricketmsf.Kernel.handle;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.StandardResult;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.microsite.out.user.UserException;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class UserModule extends UserBusinessLogic {

    private static UserModule self;

    public static UserModule getInstance() {
        if (self == null) {
            self = new UserModule();
        }
        return self;
    }

    private boolean isAdmin(RequestObject request) {
        boolean isAdmin = false;
        List requesterRoles = request.headers.get("X-user-role");
        if (requesterRoles != null && requesterRoles.contains("admin")) {
            isAdmin = true;
        }
        return isAdmin;
    }

    @Override
    public Object handleGetRequest(Event event, UserAdapterIface userAdapter) {
        RequestObject request = event.getRequest();
        String uid = request.pathExt;
        String requesterID = request.headers.getFirst("X-user-id");
        boolean isAdmin = isAdmin(request);
        StandardResult result = new StandardResult();
        try {
            if (uid.isEmpty() && isAdmin) {
                Map m = userAdapter.getAll();
                result.setData(m);
            } else if (uid.equals(requesterID) || isAdmin) {
                User u = (User) userAdapter.get(uid);
                result.setData(u);
            } else {
                result.setCode(HttpAdapter.SC_FORBIDDEN);
            }

        } catch (UserException e) {
            result.setCode(HttpAdapter.SC_NOT_FOUND);
        }
        return result;
    }

    @Override
    public Object handleRegisterRequest(Event event, UserAdapterIface userAdapter, boolean withConfirmation) {
        //TODO: check requester rights
        //only admin can set: role or type differ than default
        RequestObject request = event.getRequest();
        StandardResult result = new StandardResult();
        String uid = request.pathExt;
        if (uid != null && !uid.isEmpty()) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            return result;
        }
        boolean isAdmin = isAdmin(request);
        try {
            User newUser = new User();
            newUser.setUid(event.getRequestParameter("uid"));
            newUser.setEmail(event.getRequestParameter("email"));
            newUser.setType(User.USER);
            if (isAdmin) {
                newUser.setRole(event.getRequestParameter("role"));
            } else {
                newUser.setRole("");
            }
            newUser.setPassword(HashMaker.md5Java(event.getRequestParameter("password")));
            String type = event.getRequestParameter("type");
            if (null != type) {
                switch (type.toUpperCase()) {
                    case "OWNER":
                        if (isAdmin) {
                            newUser.setType(User.OWNER);
                        } else {
                            newUser.setType(User.USER);
                        }
                        break;
                    default:
                        newUser.setType(User.USER);
                        break;
                }
            } else {
                newUser.setType(User.USER);
            }
            // validate
            boolean valid = true;
            if (!(newUser.getUid() != null && !newUser.getUid().isEmpty())) {
                valid = false;
            }
            if (!(newUser.getEmail() != null && !newUser.getEmail().isEmpty())) {
                valid = false;
            }
            if (!(newUser.getPassword() != null && !newUser.getPassword().isEmpty())) {
                valid = false;
            }
            if (!valid) {
                result.setCode(HttpAdapter.SC_BAD_REQUEST);
                result.setMessage("lack of required parameters");
                return result;
            }
            newUser = userAdapter.register(newUser);
            if (withConfirmation) {
                result.setCode(HttpAdapter.SC_ACCEPTED);
                //fire event to send "need confirmation" email
                handle(new UserEvent(UserEvent.USER_REGISTERED, newUser.getUid()));
            } else {
                userAdapter.confirmRegistration(newUser.getUid());
                result.setCode(HttpAdapter.SC_CREATED);
                //fire event to send "welcome" email
                handle(new UserEvent(UserEvent.USER_REG_CONFIRMED, newUser.getUid()));
            }
            result.setData(newUser.getUid());
        } catch (UserException e) {
            if (e.getCode() == UserException.USER_ALREADY_EXISTS) {
                result.setCode(HttpAdapter.SC_CONFLICT);
            } else {
                result.setCode(HttpAdapter.SC_BAD_REQUEST);
            }
            result.setMessage(e.getMessage());
        } catch (NullPointerException e) {
            e.printStackTrace();
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    @Override
    public Object handleDeleteRequest(Event event, UserAdapterIface userAdapter, boolean withConfirmation) {
        //TODO: only admin can do this and user status must be IS_UNREGISTERING
        RequestObject request = event.getRequest();
        String uid = request.pathExt;
        StandardResult result = new StandardResult();
        String requesterID = request.headers.getFirst("X-user-id");
        boolean isAdmin = isAdmin(request);
        if (!isAdmin) {
            result.setCode(HttpAdapter.SC_FORBIDDEN);
            return result;
        }
        if (uid == null || uid.equals(requesterID)) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            return result;
        }
        try {
            userAdapter.remove(uid);
            handle(new UserEvent(UserEvent.USER_DELETED, uid));
            result.setCode(HttpAdapter.SC_OK);
            result.setData(uid);
        } catch (UserException e) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
        }
        return result;
    }

    @Override
    public Object handleUpdateRequest(Event event, UserAdapterIface userAdapter) {
        RequestObject request = event.getRequest();
        String requesterID = request.headers.getFirst("X-user-id");
        String uid = request.pathExt;
        StandardResult result = new StandardResult();
        if (uid == null || uid.contains("/")) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            return result;
        }
        boolean isAdmin = isAdmin(request);
        try {
            User user = userAdapter.get(uid);
            if (user == null) {
                result.setCode(HttpAdapter.SC_NOT_FOUND);
                result.setData("user not found");
                return result;
            } else if (!isAdmin && user.getUid() != requesterID) {
                result.setCode(HttpAdapter.SC_FORBIDDEN);
                return result;
            }
            String email = event.getRequestParameter("email");
            String type = event.getRequestParameter("type");
            String role = event.getRequestParameter("role");
            String password = event.getRequestParameter("password");
            String confirmed = event.getRequestParameter("confirmed");
            String unregisterRequested = event.getRequestParameter("unregisterRequested");
            if (email != null) {
                user.setEmail(email);
            }
            if (isAdmin && role != null) {
                user.setRole(role);
            }
            if (isAdmin && type != null) {
                try {
                    int userType = Integer.parseInt(type);
                    if (userType == User.USER || userType == User.OWNER) {
                        user.setType(userType);
                    }
                } catch (NumberFormatException e) {
                    //TODO
                }
            }
            if (password != null) {
                user.setPassword(HashMaker.md5Java(event.getRequestParameter("password")));
            }
            if (confirmed != null) {
                user.setConfirmed("true".equalsIgnoreCase(confirmed));
            }
            if (unregisterRequested != null) {
                //is this new request?
                if (!user.isUnregisterRequested() && "true".equalsIgnoreCase(unregisterRequested)) {
                    //fire event
                    handle(new UserEvent(UserEvent.USER_DEL_SHEDULED, user.getUid()));
                    user.setStatus(User.IS_UNREGISTERING);
                }
                user.setUnregisterRequested("true".equalsIgnoreCase(unregisterRequested));
            }
            userAdapter.modify(user);
            //fire event
            handle(new UserEvent(UserEvent.USER_UPDATED, user.getUid()));
            result.setCode(HttpAdapter.SC_OK);
            result.setData(user);
        } catch (NullPointerException | UserException e) {
            e.printStackTrace();
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
        }
        return result;
    }
}
