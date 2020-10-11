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
package org.cricketmsf.microsite.user;

import org.cricketmsf.event.Event;

/**
 *
 * @author greg
 */
public class UserEvent extends Event {
    public static final String CATEGORY_USER = "CATEGORY_USER";
    public static final String USER_REGISTERED = "USER_REG_SCHEDULED";
    public static final String USER_REG_CONFIRMED = "USER_REGISTERED";
    public static final String USER_DEL_SHEDULED = "USER_DEL_SCHEDULED";
    public static final String USER_DELETED = "USER_DELETED";
    public static final String USER_UPDATED = "USER_UPDATED";
    public static final String USER_RESET_PASSWORD = "USER_RESET_PASSWORD";
    public static final String USER_NEW_PERMALINK = "USER_NEW_PERMALINK";
        
    public UserEvent(){
        super();
        setCategory(CATEGORY_USER);
    }
    public UserEvent(String type, Object payload){
        super();
        setCategory(CATEGORY_USER);
        setType(type);
        setPayload(payload);
    }
    
    @Override
    public String[] getCategories(){
        String[] categories = {CATEGORY_USER};
        return categories;
    }
}
