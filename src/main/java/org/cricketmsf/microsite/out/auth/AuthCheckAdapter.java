/*
 * Copyright 2021 Grzegorz Skorupa .
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
public class AuthCheckAdapter extends OutboundAdapter implements Adapter, AuthAdapterIface {

    private static final Logger logger = LoggerFactory.getLogger(AuthCheckAdapter.class);
    private static final String PERMANENT_TOKEN_PREFIX = "~~";

    private String databaseAdapterName;
    private KeyValueDBIface database = null;

    private KeyValueDBIface getDatabase() throws KeyValueDBException {
        if (database == null) {
            try {
                database = (KeyValueDBIface) Kernel.getInstance().getAdaptersMap().get(databaseAdapterName);
            } catch (Exception e) {
                throw new KeyValueDBException(KeyValueDBException.UNKNOWN, "helper adapter not available");
            }
        }
        return database;
    }

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        databaseAdapterName = properties.get("database-adapter-name");
        logger.info("\tdatabase-adapter-name: " + databaseAdapterName);
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
    public boolean checkToken(String tokenID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Token login(String userID, String password) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void userAuthorize(String userId, String role) throws AuthException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void cmsAuthorize(String docId, String role) throws AuthException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Token createToken(UserProxy user) throws AuthException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Token createPermanentToken(UserProxy user, UserProxy issuer, boolean neverExpires, String payload) throws AuthException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean logout(String tokenID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removePermanentToken(String tokenID) throws AuthException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean refreshToken(String tokenID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Token createConfirmationToken(User user, String token, long timeout) throws AuthException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
