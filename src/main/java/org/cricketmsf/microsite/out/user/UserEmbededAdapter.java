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
package org.cricketmsf.microsite.out.user;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.cricketmsf.Adapter;
import org.cricketmsf.Kernel;
import org.cricketmsf.api.ResponseCode;
import org.cricketmsf.api.Result;
import org.cricketmsf.event.Procedures;
import org.cricketmsf.microsite.event.user.UserEvent;
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
    private boolean withConfirmation = false;

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
        withConfirmation = Boolean.parseBoolean(properties.get("confirm-registration"));
        logger.info("\thelper-name: " + helperAdapterName);
        logger.info("\tconfirm-registration: " + withConfirmation);
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
        //User newUser;
        Random r = new Random(System.currentTimeMillis());
        //newUser.setUid(newUser.getUid());
        user.setConfirmString(Base64.getEncoder().withoutPadding().encodeToString(("" + r.nextLong()).getBytes()));
        try {
            if (getDatabase().containsKey("users", user.getUid())) {
                throw new UserException(UserException.USER_ALREADY_EXISTS, "cannot register");
            }
            getDatabase().put("users", user.getUid(), user);
            return get(user.getUid());
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

    /*private boolean isAdmin(List<String> requesterRoles) {
        boolean admin = false;
        for (int i = 0; i < requesterRoles.size(); i++) {
            if ("admin".equals(requesterRoles.get(i))) {
                admin = true;
                break;
            }
        }
        return admin;
    }
     */
    private boolean isAdmin(String[] requesterRoles) {
        boolean admin = false;
        for (int i = 0; i < requesterRoles.length; i++) {
            if ("admin".equals(requesterRoles[i])) {
                admin = true;
                break;
            }
        }
        return admin;
    }

    // API methods
    public Result handleGet(HashMap params) {
        String userId = (String) params.get("uid");
        String requesterId = (String) params.get("requester");
        Long userNumber = (Long) params.get("userNumber");
        String[] roles = ((String) params.getOrDefault("roles", "")).split(",");
        if (null != userId && !userId.isEmpty()) {
            return handleGetUser(userId, requesterId, userNumber, roles);
        } else {
            return handleGetAll(requesterId, userNumber, roles);
        }
    }

    public Result handleGetUser(String uid, String requesterID, Long userNumber, String[] requesterRoles) {
        Result result = new Result();
        boolean admin = isAdmin(requesterRoles);
        try {
            if (uid.equals(requesterID) || admin) {
                User u = (User) get(uid);
                result.setData(u);
            } else if (uid.isEmpty() && null != userNumber) {
                User u = (User) getByNumber(userNumber);
                if (requesterID.equals(u.getUid())) {
                    result.setData(u);
                }
            }
        } catch (UserException e) {
            logger.warn(e.getMessage());
        }
        return result;
    }

    public Result handleGetAll(String requesterID, Long userNumber, String[] requesterRoles) {
        Result result = new Result();
        try {
            if (isAdmin(requesterRoles)) {
                result.setData(getAll());
            } else {
                result.setCode(ResponseCode.UNAUTHORIZED);
            }
        } catch (UserException e) {
            logger.warn(e.getMessage());
        }
        return result;
    }

    public Result handleRegisterUser(User newUser) {
        Result result = new Result();
        try {
            User user = register(newUser);
            if (withConfirmation) {
                UserEvent ev = new UserEvent(user.getUid());
                ev.setProcedure(Procedures.USER_CONFIRM_REGISTRATION);
                Kernel.getInstance().dispatchEvent(ev);
            } else {
                confirmRegistration(user.getUid());
                UserEvent ev = new UserEvent(newUser.getNumber());
                ev.setProcedure(Procedures.USER_REGISTRATION_CONFIRMED);
                Kernel.getInstance().dispatchEvent(ev);
            }
            result.setData(get(user.getUid()));
        } catch (UserException e) {
            logger.warn(e.getMessage()); //conflict
            if (e.getCode() == UserException.USER_ALREADY_EXISTS) {
                result.setCode(ResponseCode.CONFLICT);
            } else {
                result.setCode(ResponseCode.BAD_REQUEST);
            }
            result.setData(e.getMessage());
        } catch (NullPointerException e) {
            e.printStackTrace();
            result.setCode(ResponseCode.BAD_REQUEST);
            result.setData(e.getMessage());
        }
        return result;
    }

    //TODO: request removal (fired by the user, remove after confirmation by th user)
    public Result handleDeleteUser(HashMap params) {
        String uid = (String) params.get("uid");
        String requesterID = (String) params.get("requester");
        String[] requesterRoles = ((String) params.get("roles")).split(",");
        Result result = new Result();
        if (uid == null) {
            result.setCode(ResponseCode.FORBIDDEN);
            return result;
        } else if (!(uid.equals(requesterID) || isAdmin(requesterRoles))) {
            result.setCode(ResponseCode.UNAUTHORIZED);
            return result;
        }
        try {
            User tmpUser = get(uid);
            if (null != tmpUser) {
                remove(uid);
                UserEvent event = new UserEvent(tmpUser.getUid(), tmpUser.getNumber());
                event.setProcedure(Procedures.USER_AFTER_REMOVAL);
                Kernel.getInstance().dispatchEvent(event);
                result.setCode(ResponseCode.OK);
                result.setData(tmpUser.getNumber()+" "+uid);
            } else {
                result.setCode(ResponseCode.NOT_FOUND);
            }
        } catch (UserException e) {
            result.setCode(ResponseCode.BAD_REQUEST);
        }
        return result;
    }

    @Override
    public Result handleUpdateUser(HashMap params) {
        User updatedUser = (User) params.get("user");
        String requesterId = (String) params.get("requester");
        String[] requesterRoles = ((String) params.getOrDefault("roles", "")).split(",");
        Result result = new Result();
        boolean admin = isAdmin(requesterRoles);
        try {
            User user = get(updatedUser.getUid());
            User requester= get(requesterId);
            
            if (user == null) {
                result.setCode(ResponseCode.NOT_FOUND);
                return result;
            } else if (!(user.getUid().equals(requesterId) || admin)) {
                result.setCode(ResponseCode.UNAUTHORIZED);
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
                //user.setPassword(HashMaker.md5Java(updatedUser.getPassword()));
                System.out.println("NEW PASSWORD: "+updatedUser.getPassword());
                user.setPassword(updatedUser.getPassword());
            }

            if ((!user.isConfirmed()) && updatedUser.isConfirmed() != null && admin) {
                user.setConfirmed(updatedUser.isConfirmed());
                UserEvent ev = new UserEvent(updatedUser.getNumber());
                ev.setProcedure(Procedures.USER_REGISTRATION_CONFIRMED);
                Kernel.getInstance().dispatchEvent(ev);
            }
            if (updatedUser.isUnregisterRequested() != null) {
                //is this new request?
                if (!user.isUnregisterRequested() && updatedUser.isUnregisterRequested()) {
                    //fire event
                    UserEvent ev = new UserEvent(user.getUid());
                    ev.setProcedure(Procedures.USER_REMOVAL_SCHEDULED);
                    Kernel.getInstance().dispatchEvent(ev);
                    user.setStatus(User.IS_UNREGISTERING);
                }
                user.setUnregisterRequested(updatedUser.isUnregisterRequested());
            }
            modify(user);
            //fire event
            UserEvent ev = new UserEvent(user.getNumber());
            ev.setProcedure(Procedures.USER_UPDATED);
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
