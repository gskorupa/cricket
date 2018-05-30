/*
 * Copyright 2017 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package org.cricketmsf.services;

import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import java.util.HashMap;
import org.cricketmsf.annotation.HttpAdapterHook;
import org.cricketmsf.in.http.HtmlGenAdapterIface;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.ParameterMapResult;
import org.cricketmsf.in.http.StandardResult;
import org.cricketmsf.in.scheduler.SchedulerIface;
import org.cricketmsf.microsite.cms.CmsIface;
import org.cricketmsf.microsite.out.auth.AuthAdapterIface;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.microsite.user.User;
import org.cricketmsf.out.db.*;
import org.cricketmsf.out.file.FileReaderAdapterIface;
import org.cricketmsf.out.log.LoggerAdapterIface;
import java.util.List;
import org.cricketmsf.annotation.EventHook;
import org.cricketmsf.microsite.auth.AuthBusinessLogic;
import org.cricketmsf.microsite.in.http.ContentRequestProcessor;
import org.cricketmsf.microsite.user.UserEvent;
import org.cricketmsf.microsite.out.notification.*;
import org.cricketmsf.microsite.*;

/**
 * Microsite
 *
 * @author greg
 */
public class Microsite extends Kernel {

    // adapterClasses
    LoggerAdapterIface logAdapter = null;
    LoggerAdapterIface gdrpLog = null;
    //EchoHttpAdapterIface echoAdapter = null;
    KeyValueDBIface database = null;
    SchedulerIface scheduler = null;
    HtmlGenAdapterIface htmlAdapter = null;
    FileReaderAdapterIface fileReader = null;
    //cms
    KeyValueDBIface cmsDatabase = null;
    FileReaderAdapterIface cmsFileReader = null;
    CmsIface cms = null;
    //user module
    KeyValueDBIface userDB = null;
    UserAdapterIface userAdapter = null;
    //auth module
    KeyValueDBIface authDB = null;
    AuthAdapterIface authAdapter = null;
    //
    EmailSenderIface emailSender = null;

    @Override
    public void getAdapters() {
        // standard Cricket adapters
        logAdapter = (LoggerAdapterIface) getRegistered("Logger");
        gdrpLog = (LoggerAdapterIface) getRegistered("GdrpLogger");
        database = (KeyValueDBIface) getRegistered("Database");
        scheduler = (SchedulerIface) getRegistered("Scheduler");
        htmlAdapter = (HtmlGenAdapterIface) getRegistered("WwwService");
        fileReader = (FileReaderAdapterIface) getRegistered("FileReader");
        //cms
        cmsFileReader = (FileReaderAdapterIface) getRegistered("CmsFileReader");
        cmsDatabase = (KeyValueDBIface) getRegistered("cmsDB");
        cms = (CmsIface) getRegistered("cmsAdapter");
        //user
        userAdapter = (UserAdapterIface) getRegistered("userAdapter");
        userDB = (KeyValueDBIface) getRegistered("userDB");
        //auth
        authAdapter = (AuthAdapterIface) getRegistered("authAdapter");
        authDB = (KeyValueDBIface) getRegistered("authDB");
        //
        emailSender = (EmailSenderIface) getRegistered("emailSender");
    }

    @Override
    public void runInitTasks() {
        //read the OS variable to get the service URL
        String urlEnvName = (String) getProperties().get("SRVC_URL_ENV_VARIABLE");
        if (null != urlEnvName) {
            try {
                String url = System.getenv(urlEnvName);
                if (null != url) {
                    getProperties().put("serviceurl", url);
                }
            } catch (Exception e) {
            }
        }
        SiteAdministrationModule.getInstance().initDatabases(database, userDB, authDB);
        SiteAdministrationModule.getInstance().initScheduledTasks(scheduler);
        emailSender.send(
                (String) getProperties().getOrDefault("admin-notification-email", ""),
                "Microsite started", "Microsite service has been started."
        );
        setInitialized(true);
    }

    @Override
    public void runFinalTasks() {
        /*
        // CLI adapter doesn't start automaticaly as other inbound adapters
        if (cli != null) {
            cli.start();
        }
         */
    }

    /**
     * Executed when the Service is started in "not service" mode
     */
    @Override
    public void runOnce() {
        super.runOnce();
        dispatchEvent(Event.logInfo("Microsite.runOnce()", "executed"));
    }

    @Override
    public void shutdown() {
        try{
        emailSender.send(
                (String) getProperties().getOrDefault("admin-notification-email", ""),
                "Microsite shutdown", "Microsite service is going down."
        );
        }catch(Exception e){
            e.printStackTrace();
        }
        super.shutdown();
    }

    /**
     * Process requests from simple web server implementation given by
     * HtmlGenAdapter access web web resources
     *
     * @param event
     * @return ParameterMapResult with the file content as a byte array
     */
    @HttpAdapterHook(adapterName = "WwwService", requestMethod = "GET")
    public Object wwwGet(Event event) {

        //TODO: optimization
        dispatchEvent(Event.logFinest(this.getClass().getSimpleName(), event.getRequest().uri));
        String language = (String) event.getRequest().parameters.get("language");
        if (language == null || language.isEmpty()) {
            language = "en";
        }
        ParameterMapResult result = null;
        String cacheName = "webcache_"+language;
        try {
            result = (ParameterMapResult) cms
                    .getFile(event.getRequest(), htmlAdapter.useCache() ? database : null, cacheName, language, true);
            /*
            if (result.getCode() == HttpAdapter.SC_NOT_FOUND) {
                if (event.getRequest().pathExt.endsWith(".html")) {
                    //TODO: configurable index file params
                    RequestObject request = forceIndexFile(event.getRequest(), ".html", "index.html");
                    result = (ParameterMapResult) fileReader
                            .getFile(request, htmlAdapter.useCache() ? database : null, cacheName);
                }
            }
            */
            //((HashMap) result.getData()).put("serviceurl", getProperties().get("serviceurl"));
            HashMap rd = (HashMap) result.getData();
            rd.put("serviceurl", getProperties().get("serviceurl"));
            rd.put("defaultLanguage", getProperties().get("default-language"));
            rd.put("token", event.getRequestParameter("tid"));  // fake tokens doesn't pass SecurityFilter
            rd.put("user", event.getRequest().headers.getFirst("X-user-id"));
            rd.put("environmentName", getName());
            rd.put("javaversion", System.getProperty("java.version"));
            List<String> roles = event.getRequest().headers.get("X-user-role");
            if (roles != null) {
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < roles.size(); i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append("'").append(roles.get(i)).append("'");
                }
                sb.append("]");
                rd.put("roles", sb.toString());
            } else {
                rd.put("roles", "[]");
            }
            result.setData(rd);
            // TODO: caching policy 
            result.setMaxAge(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if("HEAD".equalsIgnoreCase(event.getRequest().method)){ //quick hack
            byte[] empty = {};
            result.setPayload(empty);
        }
        return result;
    }

    /**
     * Modify request pathExt: replaces all "*//*.html" requests to "/" (to be redirecetd to index.html)
     * Seems it is not usable anumore - TO BE REMOVED
     *
     * @param originalRequest
     * @param indexFileExt
     * @param indexFileName
     * @return
     */
    private RequestObject forceIndexFile(RequestObject originalRequest, String indexFileExt, String indexFileName) {
        RequestObject request = originalRequest;
        String[] pathElements = request.uri.split("/");
        if (pathElements.length == 0) {
            return request;
        }
        StringBuilder sb = new StringBuilder();
        if (pathElements[pathElements.length - 1].endsWith(indexFileExt)) {
            if (!pathElements[pathElements.length - 1].equals(indexFileName)) {
                for (int i = 0; i < pathElements.length - 1; i++) {
                    sb.append(pathElements[i]).append("/");
                }
                request.pathExt = sb.toString();
            }
        }
        return request;
    }

    @HttpAdapterHook(adapterName = "UserService", requestMethod = "OPTIONS")
    public Object userCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_OK);
        return result;
    }

    /**
     * Return user data
     *
     * @param event
     * @return
     */
    @HttpAdapterHook(adapterName = "UserService", requestMethod = "GET")
    public Object userGet(Event event) {
        return UserModule.getInstance().handleGetRequest(event, userAdapter);
    }

    @HttpAdapterHook(adapterName = "UserService", requestMethod = "POST")
    public Object userAdd(Event event) {
        boolean withConfirmation = "true".equalsIgnoreCase((String) getProperties().getOrDefault("user-confirm", "false"));
        return UserModule.getInstance().handleRegisterRequest(event, userAdapter, withConfirmation);
    }

    /**
     * Modify user data or sends password reset link
     *
     * @param event
     * @return
     */
    @HttpAdapterHook(adapterName = "UserService", requestMethod = "PUT")
    public Object userUpdate(Event event) {
        String resetPassEmail = event.getRequestParameter("resetpass");
        if (resetPassEmail == null || resetPassEmail.isEmpty()) {
            return UserModule.getInstance().handleUpdateRequest(event, userAdapter);
        } else {
            String userName = event.getRequestParameter("name");
            return CustomerModule.getInstance().handleResetRequest(event, userName, resetPassEmail, userAdapter, authAdapter, emailSender);
        }
    }

    /**
     * Set user as waiting for removal
     *
     * @param event
     * @return
     */
    @HttpAdapterHook(adapterName = "UserService", requestMethod = "DELETE")
    public Object userDelete(Event event) {
        boolean withConfirmation = "true".equalsIgnoreCase((String) getProperties().getOrDefault("user-confirm", "false"));
        return UserModule.getInstance().handleDeleteRequest(event, userAdapter, withConfirmation);
    }

    @HttpAdapterHook(adapterName = "AuthService", requestMethod = "OPTIONS")
    public Object authCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "AuthService", requestMethod = "POST")
    public Object authLogin(Event event) {
        return AuthBusinessLogic.getInstance().login(event, authAdapter);
    }

    @HttpAdapterHook(adapterName = "AuthService", requestMethod = "DELETE")
    public Object authLogout(Event event) {
        return AuthBusinessLogic.getInstance().logout(event, authAdapter);
    }

    @HttpAdapterHook(adapterName = "AuthService", requestMethod = "GET")
    public Object authCheck(Event event) {
        return AuthBusinessLogic.getInstance().check(event, authAdapter);
    }

    @HttpAdapterHook(adapterName = "AuthService", requestMethod = "PUT")
    public Object authRefresh(Event event) {
        return AuthBusinessLogic.getInstance().refreshToken(event, authAdapter);
    }

    /**
    @HttpAdapterHook(adapterName = "ConfirmationService", requestMethod = "GET")
    public Object userConfirm(Event event) {
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_FORBIDDEN);
        try {
            String key = event.getRequestParameter("key");
            try {
                if(authAdapter.checkToken(key)){
                        User user = authAdapter.getUser(key);
                        if (user.getStatus() == User.IS_REGISTERING && user.getConfirmString().equals(key)) {
                            user.setConfirmed(true);
                            userAdapter.modify(user);
                            result.setCode(200);
                            String pageContent
                                    = "Registration confirmed.<br>You can go to <a href=/#login>login page</a> and sign in.";
                            result.setFileExtension("html");
                            result.setHeader("Content-type", "text/html");
                            result.setPayload(pageContent.getBytes());
                        }
                } else {
                    result.setCode(401);
                    String pageContent
                            = "Oops, something has gone wrong: confirmation token not found . We cannot confirm your <a href=/#>Cricket</a> registration. Please contact support.";
                    result.setFileExtension("html");
                    result.setHeader("Content-type", "text/html");
                    result.setPayload(pageContent.getBytes());
                }
            } catch (UserException ex) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "confirmation error " + ex.getMessage()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    */

    @HttpAdapterHook(adapterName = "ContentService", requestMethod = "OPTIONS")
    public Object contentCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "ContentService", requestMethod = "GET")
    public Object contentGetPublished(Event event) {
        try {
            return new ContentRequestProcessor().processGetPublished(event, cms);
        } catch (Exception e) {
            e.printStackTrace();
            StandardResult r = new StandardResult();
            r.setCode(HttpAdapter.SC_NOT_FOUND);
            return r;
        }
    }

    @HttpAdapterHook(adapterName = "ContentManager", requestMethod = "OPTIONS")
    public Object contentManagerCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "ContentManager", requestMethod = "*")
    public Object contentManagerHandle(Event event) {
        return new ContentRequestProcessor().processRequest(event, cms);
    }

    @HttpAdapterHook(adapterName = "SystemService", requestMethod = "*")
    public Object systemServiceHandle(Event event) {
        return new SiteAdministrationModule().handleRestEvent(event);
    }
    
    @HttpAdapterHook(adapterName = "StatusService", requestMethod = "*")
    public Object systemStatusHandle(Event event) {
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_OK);
        result.setData("OK");
        return result;
    }

    @EventHook(eventCategory = Event.CATEGORY_LOG)
    public void logEvent(Event event) {
        logAdapter.log(event);
        if (event.getType().equals(Event.LOG_SEVERE)) {
            emailSender.send((String) getProperties().getOrDefault("admin-notification-email", ""), "Cricket - error", event.toString());
        }
    }

    @EventHook(eventCategory = Event.CATEGORY_HTTP_LOG)
    public void logHttpEvent(Event event) {
        logAdapter.log(event);
    }

    @EventHook(eventCategory = UserEvent.CATEGORY_USER)
    public void processUserEvent(Event event) {
        if (event.getTimePoint() != null) {
            scheduler.handleEvent(event);
            return;
        }
        switch (event.getType()) {
            case UserEvent.USER_REGISTERED:     //send confirmation email
                try {
                    String uid = (String) event.getPayload();
                    User user = userAdapter.get(uid);
                    gdrpLog.log(Event.logInfo(event.getId(), "REGISTERED USER "+user.getNumber()));
                    long timeout = 1800 * 1000; //30 minut
                    authAdapter.createConfirmationToken(uid, user.getConfirmString(), timeout);
                    emailSender.send(
                            user.getEmail(),
                            "Micrisite registration confirmation",
                            "We received a request to sign up to Microsite with this email address.<br>"
                            + "<a href='" + getProperties().get("serviceurl") + "/api/confirm?key=" + user.getConfirmString() + "'>Click here to confirm your registration</a><br>"
                            + "If you received this email by mistake, simply delete it. You won't be registered if you don't click the confirmation link above."
                    );
                    emailSender.send((String) getProperties().getOrDefault("admin-notification-email", ""), "Cricket - registration", uid);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    dispatchEvent(Event.logSevere(this.getClass().getSimpleName(), e.getMessage() + " while sending confirmation emai"));
                }
                break;
            case UserEvent.USER_DEL_SHEDULED:   //send confirmation email
                try {
                    String uid = (String) event.getPayload();
                    User user = userAdapter.get(uid);
                    gdrpLog.log(Event.logInfo(event.getId(), "DELETE REQUEST FOR "+user.getNumber()));
                    emailSender.send(
                            user.getEmail(),
                            "Cricket unregistration confirmed",
                            "We received a request to remove your account from Cricket Platform with this email address.<br>"
                            + "Your account is locked now and all data related to your account will be deleted to the end of next work day.<br>"
                            + "If you received this email by mistake, you can contact our support before this date to stop unregistration procedure."
                    );
                    emailSender.send((String) getProperties().getOrDefault("admin-notification-email", ""), "Cricket - unregister", uid);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    dispatchEvent(Event.logSevere(this.getClass().getSimpleName(), e.getMessage() + " while sending confirmation emai"));
                }
                break;
            case UserEvent.USER_DELETED:        //TODO: authorization
                String[] tmpPayload = ((String)event.getPayload()).split(" ");
                gdrpLog.log(Event.logInfo(event.getId(), "DELETED USER "+tmpPayload[0]+" "+tmpPayload[1]));
                break;
            case UserEvent.USER_RESET_PASSWORD:
                String payload = null;
                try {
                    payload = (String) event.getPayload();
                } catch (ClassCastException e) {
                }
                if (payload != null && !payload.isEmpty()) {
                    String[] params = payload.split(":");
                    if (params.length == 2) {
                        //TODO: email templates from CMS
                        String passResetLink = properties.getOrDefault("serviceurl", "") + "?tid=" + params[0] + "#account";
                        emailSender.send(params[1], "Password Reset Request", "Click here to change password: <a href=\"" + passResetLink + "\">" + passResetLink + "</a>");
                    } else {
                        dispatchEvent(Event.logWarning("UserEvent.USER_RESET_PASSWORD", "Malformed payload->" + payload));
                    }
                } else {
                    dispatchEvent(Event.logWarning("UserEvent.USER_RESET_PASSWORD", "Malformed payload->" + payload));
                }
                gdrpLog.log(Event.logInfo(event.getId(), "RESET PASSWORD REQUESTED FOR "+event.getPayload()));
            case UserEvent.USER_REG_CONFIRMED:  //TODO: update user
                gdrpLog.log(Event.logInfo(event.getId(), "REGISTRATION CONFIRMED FOR "+event.getPayload()));
                break;
            case UserEvent.USER_UPDATED:
                gdrpLog.log(Event.logInfo(event.getId(), "USER DATA UPDATED FOR"+event.getPayload()));
                break;
            default:
                dispatchEvent(Event.logInfo(this.getClass().getSimpleName(), "Event recived: " + event.getType()));
                break;
        }
    }

    /**
     * Handles system events
     *
     * @param event event object to process
     */
    @EventHook(eventCategory = Event.CATEGORY_GENERIC)
    public void processSystemEvent(Event event) {
        if (event.getTimePoint() != null) {
            scheduler.handleEvent(event);
            return;
        }
        switch (event.getType()) {
            case "SHUTDOWN":
                shutdown();
                break;
            case "CONTENT":
                try {
                    database.clear("webcache_pl");
                } catch (KeyValueDBException ex) {
                    dispatchEvent(Event.logWarning(this, "Problem while clearing web cache - " + ex.getMessage()));
                }
                try {
                    database.clear("webcache_en");
                } catch (KeyValueDBException ex) {
                    dispatchEvent(Event.logWarning(this, "Problem while clearing web cache - " + ex.getMessage()));
                }
                try {
                    database.clear("webcache_fr");
                } catch (KeyValueDBException ex) {
                    dispatchEvent(Event.logWarning(this, "Problem while clearing web cache - " + ex.getMessage()));
                }
                break;
            case "STATUS":
                System.out.println(printStatus());
                break;
            case "BACKUP":
                SiteAdministrationModule.getInstance().backupDatabases(database, userDB, authDB, cmsDatabase);
                break;
            default:
                dispatchEvent(Event.logWarning("Don't know how to handle type " + event.getType(), event.getPayload().toString()));
        }
    }

    /**
     * Handles all event categories not processed by other handler methods
     *
     * @param event event object to process
     */
    @EventHook(eventCategory = "*")
    public void processEvent(Event event) {
        if (event.getTimePoint() != null) {
            scheduler.handleEvent(event);
        } else {
            dispatchEvent(Event.logWarning("Don't know how to handle category " + event.getCategory(), event.getPayload().toString()));
        }
    }

}
