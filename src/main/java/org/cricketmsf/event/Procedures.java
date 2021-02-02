package org.cricketmsf.event;

import java.util.HashMap;

/**
 *
 * @author greg
 */
public class Procedures implements ProceduresIface {

    public static final int UNDEFINED = -1;
    
    public static final int DEFAULT = 0;
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
    public static final int USER_RESET_PASSWORD = 14;
            
    public static final int AUTH_LOGIN = 20;
    public static final int AUTH_LOGOUT = 21;
    public static final int AUTH_CHECK_TOKEN = 22;
    public static final int AUTH_REFRESH_TOKEN = 23;
    
    public static final int SYSTEM_CONTENT_READY = 30;
    public static final int SYSTEM_SHUTDOWN = 31;
    public static final int SYSTEM_STATUS = 32;
    public static final int SYSTEM_BACKUP = 33;
    
    public static final int CS_GET = 40;
    
    public static final int CMS_GET = 50;
    public static final int CMS_POST = 51;
    public static final int CMS_PUT = 52;
    public static final int CMS_DELETE = 53;
    public static final int CMS_CONTENT_CHANGED = 54;
    
    public static final int SA_ANY = 60;
    
    public static HashMap<Integer,String> names;
    public static HashMap<String,Integer> identifiers;
    
    public Procedures(){
        names=new HashMap<>();
        identifiers=new HashMap<>();
        add(UNDEFINED,"UNDEFNIED");
        add(DEFAULT,"DEFAULT");
        add(WWW,"WWW");
        add(GET_STATUS,"GET_STATUS");
        add(PRINT_INFO,"PRINT_INFO");
        add(GREET, "GREET");
        
        add(USER_AFTER_REMOVAL, "USER_AFTER_REMOVAL");
        add(USER_CONFIRM_REGISTRATION,"USER_CONFIRM_REGISTRATION");
        add(USER_GET,"USER_GET");
        add(USER_REGISTER, "USER_REGISTER");
        add(USER_REGISTRATION_CONFIRMED,"USER_REGISTRATION_CONFIRMED");
        add(USER_REMOVAL_SCHEDULED, "USER_REMOVAL_SCHEDULED");
        add(USER_REMOVE,"USER_REMOVE");
        add(USER_RESET_PASSWORD, "USER_RESET_PASSWORD");
        add(USER_UPDATE, "USER_UPDATE");
        add(USER_UPDATED, "USER_UPDATED");
        
        add(AUTH_CHECK_TOKEN, "AUTH_CHECK_TOKEN");
        add(AUTH_LOGIN, "AUTH_LOGIN");
        add(AUTH_LOGOUT, "AUTH_LOGOUT");
        add(AUTH_REFRESH_TOKEN, "AUTH_REFRESH_TOKEN");
        
        add(SYSTEM_BACKUP, "SYSTEM_BACKUP");
        add(SYSTEM_CONTENT_READY, "SYSTEM_CONTENT_READY");
        add(SYSTEM_SHUTDOWN, "SYSTEM_SHUTDOWN");
        add(SYSTEM_STATUS, "SYSTEM_STATUS");
        
        add(CS_GET, "CS_GET");
        
        add(CMS_CONTENT_CHANGED, "CMS_CONTENT_CHANGED");
        add(CMS_DELETE, "CMS_DELETE");
        add(CMS_GET, "CMS_GET");
        add(CMS_POST, "CMS_POST");
        add(CMS_PUT, "CMS_PUT");
        
        add(SA_ANY, "SA_ANY");
        
    }
    
    public void add(int identifier, String name){
        names.put(identifier,name);
        identifiers.put(name, identifier);
    }
    
    public String getName(int id){
        return names.getOrDefault(id, "DEFAULT");
    }
    
    public int getId(String name){
        return identifiers.getOrDefault(name, DEFAULT);
    }
    
    /*
    public String getName(int id){
        switch (id){
            case UNDEFINED: return "UNDEFINED";
            case DEFAULT: return "*";
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
            case USER_REMOVAL_SCHEDULED: return "REMOVAL_SCHEDULED";
            case USER_UPDATED: return "USER_UPDATED";
            
            case AUTH_CHECK_TOKEN: return "AUTH_CHECK_TOKEN";
            case AUTH_LOGIN: return "AUTH_LOGIN";
            case AUTH_LOGOUT: return "AUTH_LOGOUT";
            case AUTH_REFRESH_TOKEN: return "AUTH_REFRESH_TOKEN";
            
            case CS_GET: return "CS_GET";
            case CMS_GET: return "CMS_GET";
            case CMS_POST: return "CMS_POST";
            case CMS_PUT: return "CMS_PUT";
            case CMS_DELETE: return "CMS_DELETE";
            case CMS_CONTENT_CHANGED: return "CMS_CONTENT_CHANGED";
            
            //case SA_ANY: return "SA_ANY";
            //case SA_STATUS: return "SA_STATUS";
            
            default: return "UNDEFINED";
        }
    }
    
    public int getId(String name){
        switch (name){
            case "UNDEFINED": return UNDEFINED;
            case "DEFAULT": return DEFAULT;
            case "*": return DEFAULT;
            case "WWW": return WWW;
            case "GET_STATUS": return GET_STATUS;
            case "PRINT_INFO": return PRINT_INFO;
            case "GREET": return GREET;
            
            case "USER_AFTER_REMOVAL": return USER_AFTER_REMOVAL;
            case "USER_CONFIRM_REGISTRATION": return USER_CONFIRM_REGISTRATION;
            case "USER_GET": return USER_GET;
            case "USER_REGISTER": return USER_REGISTER;
            case "USER_REGISTRATION_CONFIRMED": return USER_REGISTRATION_CONFIRMED;
            case "USER_REMOVE": return USER_REMOVE;
            case "USER_UPDATE": return USER_UPDATE;
            case "USER_REMOVAL_SCHEDULED": return USER_REMOVAL_SCHEDULED;
            case "USER_UPDATED": return USER_UPDATED;
            
            case "AUTH_CHECK_TOKEN": return AUTH_CHECK_TOKEN;
            case "AUTH_LOGIN": return AUTH_LOGIN;
            case "AUTH_LOGOUT": return AUTH_LOGOUT;
            case "AUTH_REFRESH_TOKEN": return AUTH_REFRESH_TOKEN;
            
            case "CS_GET": return CS_GET;
            case "CMS_GET": return CMS_GET;
            case "CMS_POST": return CMS_POST;
            case "CMS_PUT": return CMS_PUT;
            case "CMS_DELETE": return CMS_DELETE;
            case "CMS_CONTENT_CHANGED": return CMS_CONTENT_CHANGED;
            
            //case "SA_STATUS": return SA_STATUS;
            //case "SA_SHUTDOWN": return SA_HUTDOWN;
            
            default: return UNDEFINED;
        }
    }
*/
}
