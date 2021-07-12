/*
 * Copyright 2017 Grzegorz Skorupa .
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
package org.cricketmsf.microsite.out.auth;

import org.cricketmsf.out.auth.AuthAdapterIface;
import java.util.HashMap;
import org.cricketmsf.Adapter;
import org.cricketmsf.Kernel;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.microsite.out.user.UserException;
import org.cricketmsf.microsite.out.user.User;
import org.cricketmsf.out.OutboundAdapter;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.KeyValueDBIface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author greg
 */
public class AuthEmbededAdapter extends OutboundAdapter implements Adapter, AuthAdapterIface {

    private static final Logger logger = LoggerFactory.getLogger(AuthEmbededAdapter.class);
    private static final String PERMANENT_TOKEN_PREFIX = "~~";

    private String databaseAdapterName;
    private String userAdapterName;
    private KeyValueDBIface database = null;
    private UserAdapterIface userAdapter = null;
    private short timeout = 900; // token default timeout ==  15min

    private KeyValueDBIface getDatabase() throws KeyValueDBException {
        if (database == null) {
            try {
                database = (KeyValueDBIface) Kernel.getInstance().getAdaptersMap().get(databaseAdapterName);
            } catch (Exception e) {
                throw new KeyValueDBException(KeyValueDBException.UNKNOWN, "database adapter not available");
            }
        }
        return database;
    }

    private UserAdapterIface getUserAdapter() throws UserException {
        if (userAdapter == null) {
            try {
                userAdapter = (UserAdapterIface) Kernel.getInstance().getAdaptersMap().get(userAdapterName);
            } catch (Exception e) {
                throw new UserException(UserException.HELPER_EXCEPTION, "user adapter not available");
            }
        }
        return userAdapter;
    }

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        databaseAdapterName = properties.get("database-adapter-name");
        logger.info("\tdatabase-adapter-name: " + databaseAdapterName);
        userAdapterName = properties.get("user-adapter-name");
        logger.info("\tuser-adapter-name: " + userAdapterName);
        try {
            timeout = Short.parseShort(properties.get("token-timeout"));
        } catch (NumberFormatException e) {
            logger.info("\ttoken-timeout: wrong format");
        }
        logger.info("\ttoken-timeout: " + timeout + " seconds");
    }

    @Override
    public Token login(String userID, String password) {
        try {
            User user = getUserAdapter().get(userID);
            if (user != null && user.checkPassword(password) && user.getStatus() == User.IS_ACTIVE) {
                try {
                    return createToken(new UserProxy(user.getUid(), user.getRole()));
                } catch (AuthException ex) {
                    logger.debug(ex.getMessage());
                    return null;
                }
            } else {
                return null;
            }
        } catch (UserException e) {
            logger.warn(e.getMessage());
            return null;
        }
    }

    @Override
    public void userAuthorize(String userId, String role) throws AuthException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cmsAuthorize(String docId, String role) throws AuthException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Token createToken(UserProxy user) throws AuthException {
        Token t = new Token(user, 1000 * timeout, false);
        try {
            getDatabase().put("tokens", t.getToken(), t);
        } catch (KeyValueDBException ex) {
            throw new AuthException(AuthException.HELPER_EXCEPTION, ex.getMessage());
        }
        return t;
    }

    @Override
    public Token createConfirmationToken(User user, String token, long timeout) throws AuthException {
        UserProxy proxy=new UserProxy(user.getUid(), user.getRole());
        Token t = new Token(proxy, 1000 * timeout, false);
        t.setToken(token);
        try {
            getDatabase().put("tokens", t.getToken(), t);
        } catch (KeyValueDBException ex) {
            throw new AuthException(AuthException.HELPER_EXCEPTION, ex.getMessage());
        }
        return t;
    }

    @Override
    public Token createPermanentToken(UserProxy user, UserProxy issuer, boolean neverExpires, String payload) throws AuthException {
        Token t;
        if (neverExpires) {
            t = new Token(user, -1, true);
        } else {
            t = new Token(user, 1000 * timeout, true);
        }
        t.setIssuerId(issuer.getUid());
        t.setPayload(payload);
        try {
            getDatabase().put("ptokens", t.getToken(), t);
        } catch (KeyValueDBException ex) {
            throw new AuthException(AuthException.HELPER_EXCEPTION, ex.getMessage());
        }
        return t;
    }

    @Override
    public boolean checkToken(String tokenID) {
        Token t=getToken(tokenID);
        return t != null && t.isValid();
    }

    @Override
    public Token getToken(String tokenID) {
        try {
            Token t;
            if (tokenID.startsWith(PERMANENT_TOKEN_PREFIX)) {
                t = (Token) getDatabase().get("ptokens", tokenID);
            } else {
                t = (Token) getDatabase().get("tokens", tokenID);
            }
            return t; 
       } catch (KeyValueDBException ex) {
            logger.error(ex.getMessage());
            return null;
        }
    }

    @Override
    public boolean logout(String tokenID) {
        try {
            return getDatabase().remove("tokens", tokenID);
        } catch (KeyValueDBException ex) {
            logger.debug(ex.getMessage());
        } catch (Exception ex) {
            logger.debug(ex.getMessage());
        }
        return false;
    }

    @Override
    public void removePermanentToken(String tokenID) throws AuthException {
        try {
            getDatabase().remove("ptokens", tokenID);
        } catch (KeyValueDBException ex) {
            throw new AuthException(AuthException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    public boolean refreshToken(String tokenID) {
        boolean result = false;
        try {
            Token t = (Token) getDatabase().get("tokens", tokenID);
            if (t != null) {
                t.refresh();
                getDatabase().put("tokens", tokenID, t);
                result = true;
            }
        } catch (ClassCastException | KeyValueDBException e) {
            logger.debug(e.getMessage());
        }
        return result;
    }

}
