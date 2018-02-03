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
package org.cricketmsf.microsite.user;

import java.util.List;
import java.util.Map;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.StandardResult;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.microsite.out.user.UserException;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class UserBusinessLogic {

    private static UserBusinessLogic self;

    public static UserBusinessLogic getInstance() {
        if (self == null) {
            self = new UserBusinessLogic();
        }
        return self;
    }

    public Object handleGetRequest(Event event, UserAdapterIface userAdapter) {
        RequestObject request = event.getRequest();
        //handle(Event.logFinest(this.getClass().getSimpleName(), request.pathExt));
        String uid = request.pathExt;
        String requesterID = request.headers.getFirst("X-user-id");
        List<String> requesterRoles = request.headers.get("X-user-role");
        //String requesterRole = request.headers.getFirst("X-user-role");
        boolean admin = false;
        for (int i = 0; i < requesterRoles.size(); i++) {
            if ("admin".equals(requesterRoles.get(i))) {
                admin = true;
                break;
            }
        }

        StandardResult result = new StandardResult();
        try {
            if (uid.isEmpty() && admin) {
                Map m = userAdapter.getAll();
                result.setData(m);
            } else if (uid.equals(requesterID) || admin) {
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

    public Object handleRegisterRequest(Event event, UserAdapterIface userAdapter, boolean withConfirmation) {
        //TODO: check requester rights
        //only admin can set: role or type differ than default (plus APPLICATION type)
        RequestObject request = event.getRequest();
        //handle(Event.logFinest(this.getClass().getSimpleName(), request.pathExt));
        //System.out.println("X-cms-user="+request.headers.getFirst("X-user-id"));
        StandardResult result = new StandardResult();
        String uid = request.pathExt;
        if (uid != null && !uid.isEmpty()) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            return result;
        }
        try {
            User newUser = new User();
            newUser.setUid(event.getRequestParameter("uid"));
            newUser.setEmail(event.getRequestParameter("email"));
            newUser.setType(User.USER);
            newUser.setRole("");
            newUser.setPassword(HashMaker.md5Java(event.getRequestParameter("password")));
            String type = event.getRequestParameter("type");
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
                Kernel.getInstance().dispatchEvent(new UserEvent(UserEvent.USER_REGISTERED, newUser.getUid()));
            } else {
                userAdapter.confirmRegistration(newUser.getUid());
                result.setCode(HttpAdapter.SC_CREATED);
                //fire event to send "welcome" email
                Kernel.getInstance().dispatchEvent(new UserEvent(UserEvent.USER_REG_CONFIRMED, newUser.getUid()));
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

    public Object handleDeleteRequest(Event event, UserAdapterIface userAdapter, boolean withConfirmation) {
        //TODO: check requester rights
        //only admin can do this and user status must be IS_UNREGISTERING
        RequestObject request = event.getRequest();
        String uid = request.pathExt;
        StandardResult result = new StandardResult();
        if (uid == null) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            return result;
        }
        try {
            userAdapter.remove(uid);
            Kernel.getInstance().dispatchEvent(new UserEvent(UserEvent.USER_DELETED, uid));
            result.setCode(HttpAdapter.SC_OK);
            result.setData(uid);
        } catch (UserException e) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
        }
        return result;
    }

    public Object handleUpdateRequest(Event event, UserAdapterIface userAdapter) {
        //TODO: check requester rights
        //only admin can set: role or type differ than default
        RequestObject request = event.getRequest();
        String uid = request.pathExt;
        StandardResult result = new StandardResult();
        if (uid == null || uid.contains("/")) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            return result;
        }
        try {
            User user = userAdapter.get(uid);
            if (user == null) {
                result.setCode(HttpAdapter.SC_NOT_FOUND);
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
            if (role != null) {
                user.setRole(role);
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
                    Kernel.getInstance().dispatchEvent(new UserEvent(UserEvent.USER_DEL_SHEDULED, user.getUid()));
                    user.setStatus(User.IS_UNREGISTERING);
                }
                user.setUnregisterRequested("true".equalsIgnoreCase(unregisterRequested));
            }
            userAdapter.modify(user);
            //fire event
            Kernel.getInstance().dispatchEvent(new UserEvent(UserEvent.USER_UPDATED, user.getUid()));
            result.setCode(HttpAdapter.SC_OK);
            result.setData(user);
        } catch (NullPointerException | UserException e) {
            e.printStackTrace();
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
        }
        return result;
    }
}
