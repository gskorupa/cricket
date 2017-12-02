/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cricketmsf.microsite.user;

import org.cricketmsf.Event;

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
    
    public UserEvent(String type, Object payload){
        super();
        setCategory(CATEGORY_USER);
        setType(type);
        setPayload(payload);
    }
    
}
