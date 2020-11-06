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
package org.cricketmsf.microsite.auth;

import java.util.Base64;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.StandardResult;
import org.cricketmsf.microsite.out.auth.AuthAdapterIface;
import org.cricketmsf.microsite.out.auth.AuthException;
import org.cricketmsf.microsite.out.auth.Token;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class AuthBusinessLogic {

    private static AuthBusinessLogic self;

    public static AuthBusinessLogic getInstance() {
        if (self == null) {
            self = new AuthBusinessLogic();
        }
        return self;
    }

    public Object check(Event event, AuthAdapterIface authAdapter) {
        RequestObject request = event.getRequest();
        String tokenValue = request.pathExt;
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_FORBIDDEN);
        try {
            if (authAdapter.checkToken(tokenValue)) {
                result.setCode(HttpAdapter.SC_OK);
            }
        } catch (AuthException ex) {
            Kernel.getInstance().dispatchEvent(Event.logFine(this.getClass().getSimpleName(), ex.getMessage()));
            if (ex.getCode() == AuthException.EXPIRED) {
                result.setCode(401);
            }
        }
        return result;
    }

    public Object login(Event event, AuthAdapterIface authAdapter) {
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_FORBIDDEN);
        result.setData("authorization required");

        String authData = event.getRequest().headers.getFirst("Authentication");
        String authData2 = event.getRequest().headers.getFirst("Authorization");
        if(authData2.startsWith("Basic ")){
            authData=authData2;
        }
        //handle(Event.logFinest("apiLogin", "authData=" + authData));
        if (authData != null) {
            try {
                String[] s = authData.split(" ");
                if (s.length == 2 && s[0].equalsIgnoreCase("Basic")) {
                    String authPair = new String(Base64.getDecoder().decode(s[1]));
                    while (authPair.endsWith("\r") || authPair.endsWith("\n")) {
                        authPair = authPair.substring(0, authPair.length() - 1);
                    }
                    s = authPair.split(":");
                    //handle(Event.logFinest("apiLogin", "authPair=" + authPair));
                    if (s.length == 2) {
                        Token token = authAdapter.login(s[0], s[1]);
                        if (token != null) {
                            result.setData(token.getToken());
                            result.setCode(HttpAdapter.SC_OK);
                        }
                    }
                }
            } catch (Exception e) {
                Kernel.getInstance().dispatchEvent(Event.logInfo(this.getClass().getSimpleName(), e.getMessage()));
            }
        }
        return result;
    }

    public Object logout(Event event, AuthAdapterIface authAdapter) {
        RequestObject request = event.getRequest();
        String tokenValue = request.pathExt;
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_FORBIDDEN);
        try {
            if (authAdapter.logout(tokenValue)) {
                result.setCode(HttpAdapter.SC_OK);
            }
        } catch (AuthException ex) {
            Kernel.getInstance().dispatchEvent(Event.logFine(this.getClass().getSimpleName(), ex.getMessage()));
        }
        return result;
    }

    public Object refreshToken(Event event, AuthAdapterIface authAdapter) {
        RequestObject request = event.getRequest();
        String token = request.headers.getFirst("Authentication");
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_FORBIDDEN);
        try {
            authAdapter.updateToken(token);
            result.setCode(HttpAdapter.SC_OK);
        } catch (AuthException ex) {
            ex.printStackTrace();
            Kernel.getInstance().dispatchEvent(Event.logFine(this, ex.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
