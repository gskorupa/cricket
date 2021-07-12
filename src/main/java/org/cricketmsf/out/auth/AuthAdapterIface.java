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
package org.cricketmsf.out.auth;

import org.cricketmsf.microsite.out.auth.AuthException;
import org.cricketmsf.microsite.out.auth.Token;
import org.cricketmsf.microsite.out.auth.UserProxy;
import org.cricketmsf.microsite.out.user.User;

/**
 *
 * @author greg
 */
public interface AuthAdapterIface {

    public Token login(String login, String password);

    public boolean checkToken(String tokenID);

    public boolean logout(String tokenID);

    public boolean refreshToken(String tokenID);

    public void userAuthorize(String userId, String role) throws AuthException;

    public void cmsAuthorize(String docId, String role) throws AuthException;

    public Token createToken(UserProxy user) throws AuthException;

    public Token createConfirmationToken(User user, String token, long timeout) throws AuthException;

    public Token createPermanentToken(UserProxy user, UserProxy issuer, boolean neverExpires, String payload) throws AuthException;

    public Token getToken(String tokenID);

    /**
     * Removes permanent token from database
     *
     * @param tokenID token identifier
     * @throws AuthException TODO doc
     */
    public void removePermanentToken(String tokenID) throws AuthException;

}
