/*
 * Copyright 2017 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package org.cricketmsf.microsite.out.user;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.cricketmsf.Adapter;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.event.Event;
import org.cricketmsf.api.ResponseCode;
import org.cricketmsf.api.StandardResult;
import org.cricketmsf.microsite.event.UserEvent;
import org.cricketmsf.out.OutboundAdapter;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.KeyValueDBIface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author greg
 */
public class UserEmbededAdapter extends OutboundAdapter implements Adapter, UserAdapterIface {

    private static final Logger logger = LoggerFactory.getLogger(UserEmbededAdapter.class);
    private KeyValueDBIface database = null;
    private String helperAdapterName = null;
    private boolean initialized = false;

    private KeyValueDBIface getDatabase() {
        if (database == null) {
            try {
                database = (KeyValueDBIface) Kernel.getInstance().getAdaptersMap().get(helperAdapterName);
            } catch (Exception e) {
            }
        }
        return database;
    }

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        helperAdapterName = properties.get("helper-name");
        logger.info("\thelper-name: " + helperAdapterName);
    }

    @Override
    public User get(String uid) throws UserException {
        User user;
        try {
            user = (User) getDatabase().get("users", uid);
            return user;
        } catch (KeyValueDBException | ClassCastException | NullPointerException e) {
            throw new UserException(UserException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public User getByNumber(long number) throws UserException {
        List users;
        try {
            Object[] params = {number};
            users = getDatabase().search("users", "where number=?", params);
            return (User) users.get(0);
        } catch (KeyValueDBException | ClassCastException | NullPointerException e) {
            throw new UserException(UserException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public Map getAll() throws UserException {
        HashMap<String, User> map;
        try {
            map = (HashMap<String, User>) getDatabase().getAll("users");
            return map;
        } catch (KeyValueDBException | ClassCastException e) {
            throw new UserException(UserException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public User register(User user) throws UserException {
        User newUser = user;
        Random r = new Random(System.currentTimeMillis());
        newUser.setUid(newUser.getUid());
        newUser.setConfirmString(Base64.getEncoder().withoutPadding().encodeToString(("" + r.nextLong()).getBytes()));
        try {
            if (getDatabase().containsKey("users", newUser.getUid())) {
                throw new UserException(UserException.USER_ALREADY_EXISTS, "cannot register");
            }
            getDatabase().put("users", newUser.getUid(), newUser);
            return get(newUser.getUid());
        } catch (KeyValueDBException e) {
            throw new UserException(UserException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public void modify(User user) throws UserException {
        try {
            if (!getDatabase().containsKey("users", user.getUid())) {
                throw new UserException(UserException.UNKNOWN_USER, "user not found");
            }
            getDatabase().put("users", user.getUid(), user);
        } catch (KeyValueDBException e) {
            throw new UserException(UserException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public void confirmRegistration(String uid) throws UserException {
        User user = get(uid);
        if (user == null) {
            throw new UserException(UserException.UNKNOWN_USER, "user not found");
        }
        try {
            user.setConfirmed(true);
            user.setStatus(User.IS_ACTIVE);
            getDatabase().put("users", user.getUid(), user);
        } catch (KeyValueDBException e) {
            throw new UserException(UserException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public void remove(String uid) throws UserException {
        try {
            getDatabase().remove("users", uid);
            //TODO: event to remove user's data
        } catch (KeyValueDBException e) {
            throw new UserException(UserException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new UserException(UserException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public boolean checkPassword(String uid, String password) throws UserException {
        try {
            User user = (User) getDatabase().get("users", uid);
            return user.checkPassword(password);
        } catch (NullPointerException | KeyValueDBException e) {
            throw new UserException(UserException.UNKNOWN_USER, e.getMessage());
        }
    }

    private boolean isAdmin(List<String> requesterRoles) {
        boolean admin = false;
        for (int i = 0; i < requesterRoles.size(); i++) {
            if ("admin".equals(requesterRoles.get(i))) {
                admin = true;
                break;
            }
        }
        return admin;
    }

    // business logic
    public Object handleGetUser(String uid, String requesterID, Long userNumber, List<String> requesterRoles) {
        boolean admin = isAdmin(requesterRoles);
        StandardResult result = new StandardResult();
        try {
            if (uid.isEmpty() && admin) {
                Map m = getAll();
                result.setData(m);
            } else if (uid.equals(requesterID) || admin) {
                User u = (User) get(uid);
                result.setData(u);
            } else if (uid.isEmpty() && null != userNumber) {
                User u = (User) getByNumber(userNumber);
                if (requesterID.equals(u.getUid())) {
                    result.setData(u);
                }
            } else {
                result.setCode(ResponseCode.FORBIDDEN);
            }
        } catch (UserException e) {
            result.setCode(ResponseCode.NOT_FOUND);
        }
        return result;
    }

    public Object handleRegisterUser(User newUser, boolean withConfirmation) {
        StandardResult result = new StandardResult();
        try {
            if (withConfirmation) {
                result.setCode(ResponseCode.ACCEPTED);
                //fire event to send "need confirmation" email
                UserEvent ev = new UserEvent(newUser.getUid());
                ev.setProcedureName("registration");
                Kernel.getInstance().dispatchEvent(ev);
            } else {
                confirmRegistration(newUser.getUid());
                result.setCode(ResponseCode.CREATED);
                //fire event to send "welcome" email
                UserEvent ev = new UserEvent(newUser.getNumber());
                ev.setProcedureName("registrationConfirmed");
                Kernel.getInstance().dispatchEvent(ev);
            }
            result.setData(newUser.getUid());
        } catch (UserException e) {
            if (e.getCode() == UserException.USER_ALREADY_EXISTS) {
                result.setCode(ResponseCode.CONFLICT);
            } else {
                result.setCode(ResponseCode.BAD_REQUEST);
            }
            result.setMessage(e.getMessage());
        } catch (NullPointerException e) {
            e.printStackTrace();
            result.setCode(ResponseCode.BAD_REQUEST);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    public Object handleDeleteUser(String uid, List<String> requesterRoles, boolean withConfirmation) {
        //TODO: check requester rights
        //only admin can do this and user status must be IS_UNREGISTERING
        boolean admin = isAdmin(requesterRoles);
        StandardResult result = new StandardResult();
        if (uid == null || !isAdmin(requesterRoles)) {
            result.setCode(ResponseCode.BAD_REQUEST);
            return result;
        }
        try {
            User tmpUser = get(uid);
            remove(uid);
            UserEvent event = new UserEvent(tmpUser.getUid(), tmpUser.getNumber());
            event.setProcedureName("afterRemove");
            Kernel.getInstance().dispatchEvent(event);
            result.setCode(ResponseCode.OK);
            result.setData(uid);
        } catch (UserException e) {
            result.setCode(ResponseCode.BAD_REQUEST);
        }
        return result;
    }

    public Object handleUpdateRequest(User updatedUser, List<String> requesterRoles) {
        StandardResult result = new StandardResult();
        boolean admin = isAdmin(requesterRoles);
        try {
            User user = get(updatedUser.getUid());
            if (user == null) {
                result.setCode(ResponseCode.NOT_FOUND);
                return result;
            }
            if (updatedUser.getEmail() != null) {
                user.setEmail(updatedUser.getEmail());
            }
            if (updatedUser.getName() != null) {
                user.setName(updatedUser.getName());
            }
            if (updatedUser.getSurname() != null) {
                user.setSurname(updatedUser.getSurname());
            }
            if (updatedUser.getRole() != null && admin) {
                user.setRole(updatedUser.getRole());
            }
            if (updatedUser.getType() != null && admin) {
                user.setType(updatedUser.getType());
            }
            if (updatedUser.getPassword() != null) {
                user.setPassword(HashMaker.md5Java(updatedUser.getPassword()));
            }

            if (updatedUser.isConfirmed() != null) {
                user.setConfirmed(updatedUser.isConfirmed());
                UserEvent ev = new UserEvent(updatedUser.getNumber());
                ev.setProcedureName("registrationConfirmed");
                Kernel.getInstance().dispatchEvent(ev);
            }
            if (updatedUser.isUnregisterRequested() != null) {
                //is this new request?
                if (!user.isUnregisterRequested() && updatedUser.isUnregisterRequested()) {
                    //fire event
                    UserEvent ev = new UserEvent(user.getUid());
                    ev.setProcedureName("removalScheduled");
                    Kernel.getInstance().dispatchEvent(ev);
                    user.setStatus(User.IS_UNREGISTERING);
                }
                user.setUnregisterRequested(updatedUser.isUnregisterRequested());
            }
            modify(user);
            //fire event
            UserEvent ev = new UserEvent(user.getNumber());
            ev.setProcedureName("updated");
            Kernel.getInstance().dispatchEvent(ev);
            result.setCode(ResponseCode.OK);
            result.setData(user);
        } catch (NullPointerException | UserException e) {
            e.printStackTrace();
            result.setCode(ResponseCode.BAD_REQUEST);
        }
        return result;
    }

}
