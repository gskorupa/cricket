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
import org.cricketmsf.microsite.user.User;
import org.cricketmsf.out.OutboundAdapter;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.KeyValueDBIface;

/**
 *
 * @author greg
 */
public class UserEmbededAdapter extends OutboundAdapter implements Adapter, UserAdapterIface {

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
        Kernel.getInstance().getLogger().print("\thelper-name: " + helperAdapterName);
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
            Object[] params={number};
            users = getDatabase().search("users", "where number=?",params);
            return (User)users.get(0);
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
            if(!getDatabase().containsKey("users", user.getUid())){
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
        if(user==null){
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

    /*
    @Override
    public void unregister(String uid) throws UserException {
        User user = get(uid);
        if(user==null){
            throw new UserException(UserException.UNKNOWN_USER, "user not found");
        }
        try {
            Random r = new Random(System.currentTimeMillis());
            user.setConfirmString(Base64.getEncoder().withoutPadding().encodeToString(("" + r.nextLong()).getBytes()));
            user.setUnregisterRequested(true);
            user.setStatus(User.IS_UNREGISTERING);
            getDatabase().put("users", user.getUid(), user);
        } catch (KeyValueDBException e) {
            throw new UserException(UserException.HELPER_EXCEPTION, e.getMessage());
        }
    }
*/

    @Override
    public void remove(String uid) throws UserException {
        try {
            getDatabase().remove("users", uid);
            //TODO: event to remove user's data
        } catch (KeyValueDBException e) {
            throw new UserException(UserException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e){
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

}
