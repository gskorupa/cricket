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
package org.cricketmsf.microsite.out.auth;

import java.util.HashMap;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.microsite.out.user.UserException;
import org.cricketmsf.microsite.user.User;
import org.cricketmsf.out.OutboundAdapter;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.KeyValueDBIface;

/**
 *
 * @author greg
 */
public class AuthEmbededAdapter extends OutboundAdapter implements Adapter, AuthAdapterIface {

    private String helperAdapterName;
    private String helperAdapterName2;
    private KeyValueDBIface database = null;
    private UserAdapterIface userAdapter = null;
    private short timeout = 900; // token default timeout ==  15min

    private KeyValueDBIface getDatabase() throws KeyValueDBException {
        if (database == null) {
            try {
                database = (KeyValueDBIface) Kernel.getInstance().getAdaptersMap().get(helperAdapterName);
            } catch (Exception e) {
                throw new KeyValueDBException(KeyValueDBException.UNKNOWN, "helper adapter not available");
            }
        }
        return database;
    }

    private UserAdapterIface getUserAdapter() throws UserException {
        if (userAdapter == null) {
            try {
                userAdapter = (UserAdapterIface) Kernel.getInstance().getAdaptersMap().get(helperAdapterName2);
            } catch (Exception e) {
                throw new UserException(UserException.HELPER_EXCEPTION, "helper adapter not available");
            }
        }
        return userAdapter;
    }

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        helperAdapterName = properties.get("helper-name");
        Kernel.getInstance().getLogger().print("\thelper-name: " + helperAdapterName);
        helperAdapterName2 = properties.get("helper-name-2");
        Kernel.getInstance().getLogger().print("\thelper-name-2: " + helperAdapterName2);
        try {
            timeout = Short.parseShort(properties.get("token-timeout"));
        } catch (NumberFormatException e) {
            Kernel.getInstance().getLogger().print("\ttoken-timeout: wrong format");
        }
        Kernel.getInstance().getLogger().print("\ttoken-timeout: " + timeout+" seconds");
    }

    @Override
    public Token login(String userID, String password) throws AuthException {
        try {
            User user = getUserAdapter().get(userID);
            if (user != null && user.checkPassword(password) && user.getStatus() == User.IS_ACTIVE) {
                return createToken(userID);
            } else {
                return null;
            }
        } catch (UserException e) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), e.getMessage()));
            throw new AuthException(AuthException.ACCESS_DENIED, e.getMessage());
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
    public Token createToken(String userID) throws AuthException {
        Token t = new Token(userID, 1000 * timeout, false);
        try {
            getDatabase().put("tokens", t.getToken(), t);
        } catch (KeyValueDBException ex) {
            throw new AuthException(AuthException.HELPER_EXCEPTION, ex.getMessage());
        }
        return t;
    }
    
    @Override
    public Token createConfirmationToken(String userID, String token, long timeout) throws AuthException{
        Token t = new Token(userID, timeout, false);
        t.setToken(token);
        try {
            getDatabase().put("tokens", t.getToken(), t);
        } catch (KeyValueDBException ex) {
            throw new AuthException(AuthException.HELPER_EXCEPTION, ex.getMessage());
        }
        return t;
    }
    
    @Override
    public boolean checkToken(String tokenID) throws AuthException {
        try {
            Token t = null;
            t = (Token) getDatabase().get("tokens", tokenID);
            if (t == null) {
                return false;
            }
            if (t.isValid()) {
                return true;
            } else {
                throw new AuthException(AuthException.EXPIRED);
            }
        } catch (KeyValueDBException ex) {
            throw new AuthException(AuthException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public User getUser(String tokenID) throws AuthException {
        return getUser(tokenID, false);
    }

    @Override
    public User getUser(String tokenID, boolean permanentToken) throws AuthException {
        try {
            Token t = null;
            if (permanentToken) {
                t = (Token) getDatabase().get("ptokens", tokenID);
            } else {
                t = (Token) getDatabase().get("tokens", tokenID);
            }
            if (t != null) {
                if (t.isValid()) {
                    User user = getUserAdapter().get(t.getUid());
                    return user;
                } else {
                    throw new AuthException(AuthException.EXPIRED,"token expired");
                }
            } else {
                return null;
            }
        } catch (UserException | KeyValueDBException ex) {
            throw new AuthException(AuthException.ACCESS_DENIED, ex.getMessage());
        }
    }

    @Override
    public boolean logout(String tokenID) throws AuthException {
        try {
            return getDatabase().remove("tokens", tokenID);
        } catch (KeyValueDBException ex) {
            throw new AuthException(AuthException.HELPER_EXCEPTION, ex.getMessage());
        } catch(Exception ex){
            ex.printStackTrace();
            throw new AuthException(AuthException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public Token createPermanentToken(String userID, String issuerID, boolean neverExpires, String payload) throws AuthException {
        Token t;
        if (neverExpires) {
            t = new Token(userID, -1, true);
        } else {
            t = new Token(userID, 1000 * timeout, true);
        }
        t.setIssuer(issuerID);
        t.setPayload(payload);
        try {
            getDatabase().put("ptokens", t.getToken(), t);
        } catch (KeyValueDBException ex) {
            throw new AuthException(AuthException.HELPER_EXCEPTION, ex.getMessage());
        }
        return t;
    }

    @Override
    public boolean checkPermanentToken(String tokenID) throws AuthException {
        try {
            Token t = null;
            t = (Token) getDatabase().get("ptokens", tokenID);
            if (t == null) {
                return false;
            }
            if (t.isValid()) {
                return true;
            } else {
                throw new AuthException(AuthException.EXPIRED);
            }
        } catch (KeyValueDBException ex) {
            throw new AuthException(AuthException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public User getIssuer(String tokenID) throws AuthException {
        try {
            Token t = null;
            t = (Token) getDatabase().get("ptokens", tokenID);
            //System.out.println("TOKEN FOUND (getIssuer)=" + t);
            if (t != null) {
                if (t.isValid()) {
                    return getUserAdapter().get(t.getIssuer());
                } else {
                    throw new AuthException(AuthException.EXPIRED);
                }
            } else {
                return null;
            }
        } catch (UserException | KeyValueDBException ex) {
            throw new AuthException(AuthException.ACCESS_DENIED, ex.getMessage());
        }
    }

    @Override
    public void removePermanentToken(String tokenID) throws AuthException {
        try {
            getDatabase().remove("ptokens", tokenID);
        } catch (KeyValueDBException ex) {
            throw new AuthException(AuthException.HELPER_EXCEPTION, ex.getMessage());
        }
    }
    
    public void updateToken(String tokenID) throws AuthException {
        try{
            Token t = (Token) getDatabase().get("tokens", tokenID);
            if(t==null){
                throw new AuthException(AuthException.UNAUTHORIZED, "");
            }
            t.refresh();
            getDatabase().put("tokens", t.getToken(), t);
        }catch(ClassCastException | KeyValueDBException e){
            throw new AuthException(AuthException.HELPER_EXCEPTION, e.getMessage());
        }
    }

}
