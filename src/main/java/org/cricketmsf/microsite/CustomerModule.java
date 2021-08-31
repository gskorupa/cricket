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

import org.cricketmsf.microsite.out.notification.EmailSenderIface;
import org.cricketmsf.microsite.user.*;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.StandardResult;
import org.cricketmsf.microsite.out.auth.AuthAdapterIface;
import org.cricketmsf.microsite.out.auth.AuthException;
import org.cricketmsf.microsite.out.auth.Token;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.microsite.out.user.UserException;

public class CustomerModule {

    private static CustomerModule self;

    public static CustomerModule getInstance() {
        if (self == null) {
            self = new CustomerModule();
        }
        return self;
    }

    /**
     * Creates temporary token and sends e-mail including link to reset
     * password;
     *
     * @param event event
     * @param userAdapter user adapter
     * @param userID user ID
     * @param resetPassEmail email
     * @param authAdapter auth adapter
     * @param emailSender email sender adapter
     * @return result
     */
    public Object handleResetRequest(
            Event event,
            String userID,
            String resetPassEmail,
            UserAdapterIface userAdapter,
            AuthAdapterIface authAdapter,
            EmailSenderIface emailSender) {

        RequestObject request = event.getRequest();
        StandardResult result = new StandardResult();
        if (userID == null || userID.isEmpty()) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            return result;
        }
        try {
            User user = userAdapter.get(userID);
            if (user == null) {
                result.setCode(HttpAdapter.SC_FORBIDDEN);
                return result;
            }
            String email = user.getEmail();
            if (!resetPassEmail.equalsIgnoreCase(email)) {
                result.setCode(HttpAdapter.SC_FORBIDDEN);
                return result;
            }

            // create link
            Token token = authAdapter.createPermanentToken(userID, userID, false, null);
            Kernel.getInstance().dispatchEvent(new UserEvent(UserEvent.USER_RESET_PASSWORD, token.getToken() + ":" + email));

        } catch (NullPointerException | UserException | AuthException e) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
        }
        return result;
    }

    public Object handlePermanentLinkRequest(
            Event event,
            UserAdapterIface userAdapter,
            AuthAdapterIface authAdapter,
            EmailSenderIface emailSender) {

        RequestObject request = event.getRequest();
        String userID = request.headers.getFirst("X-issuer-id");
        String link = ""+event.getPayload();
        String publicUserID = "public";
        StandardResult result = new StandardResult();
        try {
            User user = userAdapter.get(userID);
            User publicUser = userAdapter.get(publicUserID);
            if (user == null) {
                result.setCode(HttpAdapter.SC_FORBIDDEN);
                return result;
            }
            if (publicUser == null) {
                result.setCode(HttpAdapter.SC_BAD_REQUEST);
                return result;
            }
            String email = user.getEmail();

            // create link
            Token token = authAdapter.createPermanentToken(publicUserID, userID, true, null);
            link=link.concat(token.getToken());
            Kernel.getInstance().dispatchEvent(new UserEvent(UserEvent.USER_NEW_PERMALINK, link));
            result.setData(link);
        } catch (NullPointerException | UserException | AuthException e) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
        }
        return result;
    }
    
    public Object handleDeleteLinkRequest(Event event){
        RequestObject request = event.getRequest();
        String userID = request.headers.getFirst("X-issuer-id");
        String link = ""+event.getPayload();
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_NOT_IMPLEMENTED);
        return null;
    }
}
