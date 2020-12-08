package org.cricketmsf.event;

/**
 *
 * @author greg
 */
public class Procedures {

    public static final int UNDEFINED = -1;
    
    public static final int ANY = 0;
    public static final int WWW = 1;
    public static final int GET_STATUS = 2;
    public static final int PRINT_INFO = 3;
    public static final int GREET = 4;
    
    public static final int USER_GET = 5;
    public static final int USER_REGISTER = 6;
    public static final int USER_UPDATE = 7;
    public static final int USER_REMOVE = 8;
    public static final int USER_CONFIRM_REGISTRATION = 9;
    public static final int USER_REGISTRATION_CONFIRMED = 10;
    public static final int USER_AFTER_REMOVAL = 11;
    public static final int USER_REMOVAL_SCHEDULED = 12;
    public static final int USER_UPDATED = 13;
    
    public static final int AUTH_LOGIN = 14;
    public static final int AUTH_LOGOUT = 15;
    public static final int AUTH_CHECK_TOKEN = 16;
    public static final int AUTH_REFRESH_TOKEN = 17;
    
    
    
    
    public static String getName(int id){
        switch (id){
            case UNDEFINED: return "UNDEFINED";
            case ANY: return "*";
            case WWW: return "WWW";
            case GET_STATUS: return "GET_STATUS";
            case PRINT_INFO: return "PRINT_INFO";
            case GREET: return "GREET";
            case USER_AFTER_REMOVAL: return "USER_AFTER_REMOVAL";
            case USER_CONFIRM_REGISTRATION: return "USER_CONFIRM_REGISTRATION";
            case USER_GET: return "USER_GET";
            case USER_REGISTER: return "USER_REGISTER";
            case USER_REGISTRATION_CONFIRMED: return "USER_REGISTRATION_CONFIRMED";
            case USER_REMOVE: return "USER_REMOVE";
            case USER_UPDATE: return "USER_UPDATE";
            case AUTH_CHECK_TOKEN: return "AUTH_CHECK_TOKEN";
            case AUTH_LOGIN: return "AUTH_LOGIN";
            case AUTH_LOGOUT: return "AUTH_LOGOUT";
            case AUTH_REFRESH_TOKEN: return "AUTH_REFRESH_TOKEN";
            default: return "UNDEFINED";
        }
    }
    
}
