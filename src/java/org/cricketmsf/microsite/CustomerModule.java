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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
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
     * @param event
     * @param userAdapter
     * @return
     */
    public Object handleResetRequest(
            Event event,
            String userID,
            String resetPassEmail,
            UserAdapterIface userAdapter,
            AuthAdapterIface authAdapter,
            EmailSenderIface emailSender) {

        RequestObject request = event.getRequest();
        //String userID = request.headers.getFirst("X-issuer-id");
        StandardResult result = new StandardResult();
        //System.out.println("RESETPASS: "+resetPassEmail+" "+userID);
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
            //System.out.println(resetPassEmail+"=="+email);
            if (!resetPassEmail.equalsIgnoreCase(email)) {
                result.setCode(HttpAdapter.SC_FORBIDDEN);
                return result;
            }

            // create link
            Token token = authAdapter.createPermanentToken(userID, userID, false, null);
            String tokenID = URLEncoder.encode(token.getToken(), "UTF-8");
            Kernel.handle(new UserEvent(UserEvent.USER_RESET_PASSWORD, tokenID + ":" + email));

        } catch (NullPointerException | UserException | AuthException | UnsupportedEncodingException e) {
            //e.printStackTrace();
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
            String tokenID = URLEncoder.encode(token.getToken(), "UTF-8");
            link=link+tokenID;
            Kernel.handle(new UserEvent(UserEvent.USER_NEW_PERMALINK, link));
            result.setData(link+tokenID);
        } catch (NullPointerException | UserException | AuthException | UnsupportedEncodingException e) {
            //e.printStackTrace();
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
