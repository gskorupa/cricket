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
import org.cricketmsf.event.Event;

/**
 *
 * @author greg
 */
public class AuthEvent extends Event {

    HashMap<String, String> data;

    public AuthEvent() {
        super();
        data=new HashMap<>();
    }

    public AuthEvent(String login, String password, String token) {
        super();
        data=new HashMap<>();
        data.put("login",login);
        data.put("password", password);
        data.put("token", token);
    }

    @Override
    public HashMap<String,String> getData(){
        return data;
    }

}