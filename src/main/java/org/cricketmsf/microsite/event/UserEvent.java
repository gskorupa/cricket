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
package org.cricketmsf.microsite.event;

import java.util.HashMap;
import java.util.List;
import org.cricketmsf.event.Event;
import org.cricketmsf.microsite.out.user.User;

/**
 *
 * @author greg
 */
public class UserEvent extends Event {

    Object data;

    public UserEvent() {
        super();
    }

    public UserEvent(String token) {
        super();
        data = token;
    }

    public UserEvent(Long number) {
        super();
        data = number;
    }

    public UserEvent(User user) {
        super();
        this.data = user;
    }

    public UserEvent(String uid, String requesterID, Long userNumber, String requesterRoles) {
        super();
        try {
            HashMap map = new HashMap();
            map.put("uid", uid);
            map.put("requester", requesterID);
            map.put("userNumber", userNumber);
            map.put("roles", requesterRoles);
            data = map;
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public UserEvent(User user, String requesterID, String requesterRoles) {
        super();
        HashMap map = new HashMap();
        map.put("user", user);
        map.put("requester", requesterID);
        map.put("roles", requesterRoles);
        data = map;
    }

    public UserEvent(String uid, String requesterID, String requesterRoles) {
        super();
        HashMap map = new HashMap();
        map.put("uid", uid);
        map.put("requester", requesterID);
        map.put("roles", requesterRoles);
        data = map;
    }

    public UserEvent(String uid, Long userNumber) {
        super();
        HashMap map = new HashMap();
        map.put("uid", uid);
        map.put("number", userNumber);
        data = map;
    }

    @Override
    public Object getData() {
        return data;
    }

}
