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

import org.cricketmsf.Kernel;
import org.cricketmsf.in.http.HtmlGenAdapterIface;
import org.cricketmsf.in.scheduler.SchedulerIface;
//import org.cricketmsf.microsite.cms.CmsIface;
import org.cricketmsf.out.auth.AuthAdapterIface;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
//import org.cricketmsf.microsite.user.User;
import org.cricketmsf.out.db.*;
import org.cricketmsf.annotation.EventHook;
import org.cricketmsf.exception.InitException;
import org.cricketmsf.api.Result;
import org.cricketmsf.in.queue.SubscriberIface;
/*
import org.cricketmsf.microsite.in.http.ContentRequestProcessor;
import org.cricketmsf.microsite.user.UserEvent;
import org.cricketmsf.microsite.out.notification.*;
import org.cricketmsf.microsite.*;
import org.cricketmsf.microsite.cms.TranslatorIface;
import org.cricketmsf.microsite.event.GetContent;
import org.cricketmsf.microsite.event.StatusRequested;
 */
import org.cricketmsf.in.openapi.OpenApiIface;
import org.cricketmsf.microsite.event.AuthEvent;
import org.cricketmsf.microsite.out.auth.Token;
import org.cricketmsf.microsite.out.siteadmin.SiteAdministrationIface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Microsite
 *
 * @author greg
 */
public class Microsite extends Kernel {

    private static final Logger logger = LoggerFactory.getLogger(Microsite.class);

    // adapterClasses
    SiteAdministrationIface siteAdmin = null;
    //EchoHttpAdapterIface echoAdapter = null;
    KeyValueDBIface database = null;
    SchedulerIface scheduler = null;
    HtmlGenAdapterIface htmlAdapter = null;
    //cms
    KeyValueDBIface cmsDatabase = null;
    //   FileReaderAdapterIface cmsFileReader = null;
    //CmsIface cms = null;
    //TranslatorIface translator = null;
    //user module
    KeyValueDBIface userDB = null;
    UserAdapterIface userAdapter = null;
    //auth module
    KeyValueDBIface authDB = null;
    AuthAdapterIface authAdapter = null;
    //
    //EmailSenderIface emailSender = null;
    SubscriberIface queueSubscriber = null;

    OpenApiIface apiGenerator = null;

    public Microsite() {
        super();
        this.configurationBaseName = "Microsite";
    }

    @Override
    public void getAdapters() {
        // standard Cricket adapters
        siteAdmin=(SiteAdministrationIface)getRegistered("SiteAdministrationModule");
        //gdprLog = (LoggerAdapterIface) getRegistered("GdprLogger");
        database = (KeyValueDBIface) getRegistered("Database");
        //scheduler = (SchedulerIface) getRegistered("Scheduler");
        //htmlAdapter = (HtmlGenAdapterIface) getRegistered("WwwService");
        //cms
        //cmsDatabase = (KeyValueDBIface) getRegistered("cmsDB");
        //cms = (CmsIface) getRegistered("cmsAdapter");
        //translator = (TranslatorIface) getRegistered("cmsTranslator");
        //user
        userAdapter = (UserAdapterIface) getRegistered("UserAdapter");
        userDB = (KeyValueDBIface) getRegistered("UserDB");
        //auth
        authAdapter = (AuthAdapterIface) getRegistered("AuthAdapter");
        authDB = (KeyValueDBIface) getRegistered("AuthDB");
        //
        //emailSender = (EmailSenderIface) getRegistered("emailSender");

        //queueSubscriber = (SubscriberIface) getRegistered("QueueSubscriber");
        apiGenerator = (OpenApiIface) getRegistered("OpenApi");
    }

    @Override
    public void runInitTasks() throws InitException {
        try {
            super.runInitTasks();
        } catch (InitException ex) {
            ex.printStackTrace();
            shutdown();
        }

        siteAdmin.initDatabases(database, userDB, authDB);
        /*emailSender.send(
                (String) getProperties().getOrDefault("admin-notification-email", ""),
                getId()+" started", getId()+" service has been started."
        );
        
        try {
            queueSubscriber.init();
        } catch (QueueException ex) {
            ex.printStackTrace();
            shutdown();
        }
         */
        apiGenerator.init(this);
        setInitialized(true);
        /*
        dispatchEvent(
                new Event(this.getName(), "SYSTEM", "message", "+10s", getUuid() + " service started")
        );
         */
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
        apiGenerator.init(this);
        System.out.println(apiGenerator.toYaml());
        logger.info("Microsite.runOnce() executed");
    }

    @Override
    public void shutdown() {
        /*
        try {
            emailSender.send(
                    (String) getProperties().getOrDefault("admin-notification-email", ""),
                    getId()+" shutdown", getId()+" service is going down."
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
         */
        super.shutdown();
    }
    
    public AuthAdapterIface getAuthAdapter(){
        return authAdapter;
    }

    /**
     * Process requests from simple web server implementation given by
     * HtmlGenAdapter access web web resources
     *
     * @param event
     * @return ParameterMapResult with the file content as a byte array
     */
    /*
    @HttpAdapterHook(adapterName = "WwwService", requestMethod = "GET")
    public Object wwwGet(Event event) {
        //TODO: optimization
        dispatchEvent(Event.logFinest(this.getClass().getSimpleName(), event.getRequest().uri));
        String language = (String) event.getRequest().parameters.get("language");
        if (language == null || language.isEmpty()) {
            language = "en";
        }
        ParameterMapResult result = null;
        String cacheName = "webcache_" + language;
        try {
            result = (ParameterMapResult) cms
                    .getFile(event.getRequest(), htmlAdapter.useCache() ? database : null, cacheName, language, true);
            //((HashMap) result.getData()).put("serviceurl", getProperties().get("serviceurl"));
            HashMap rd = (HashMap) result.getData();
            rd.put("serviceurl", getProperties().get("serviceurl"));
            rd.put("defaultLanguage", getProperties().get("default-language"));
            rd.put("token", event.getRequestParameter("tid"));  // fake tokens doesn't pass SecurityFilter
            rd.put("user", event.getRequest().headers.getFirst("X-user-id"));
            rd.put("environmentName", getName());
            rd.put("javaversion", System.getProperty("java.version"));
            rd.put("wwwTheme", getProperties().getOrDefault("www-theme", "theme0"));
            List<String> roles = event.getRequest().headers.get("X-user-role");
            if (roles != null && roles.size() > 0) {
                StringBuilder sb = new StringBuilder("[");
                for (String role : roles) {
                    sb.append(role).append(",");
                }
                rd.put("roles", sb.substring(0, sb.length() - 1) + "]");
            } else {
                rd.put("roles", "[]");
            }
            result.setData(rd);
            // TODO: caching policy 
            result.setMaxAge(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if ("HEAD".equalsIgnoreCase(event.getRequest().method)) { //quick hack
            byte[] empty = {};
            result.setPayload(empty);
        }
        return result;
    }
     */
    /**
     * Return user data
     *
     * @param event
     * @return
     */
    /*
    @HttpAdapterHook(adapterName = "UserService", requestMethod = "GET")
    public Object userGet(Event event) {
        return UserModule.getInstance().handleGetRequest(event, userAdapter);
    }
    
    
    @HttpAdapterHook(adapterName = "UserService", requestMethod = "POST")
    public Object userAdd(Event event) {
        boolean withConfirmation = "true".equalsIgnoreCase((String) getProperties().getOrDefault("user-confirm", "false"));
        return UserModule.getInstance().handleRegisterRequest(event, userAdapter, withConfirmation);
    }
     */
    /**
     * Modify user data or sends password reset link
     *
     * @param event
     * @return
     */
    /*
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
     */
    /**
     * Set user as waiting for removal
     *
     * @param event
     * @return
     */
    /*
    @HttpAdapterHook(adapterName = "UserService", requestMethod = "DELETE")
    public Object userDelete(Event event) {
        boolean withConfirmation = "true".equalsIgnoreCase((String) getProperties().getOrDefault("user-confirm", "false"));
        return UserModule.getInstance().handleDeleteRequest(event, userAdapter, withConfirmation);
    }
     */
    @EventHook(className = "org.cricketmsf.microsite.event.AuthEvent", procedureName = "login")
    public Result authLogin(AuthEvent event) {
        Token token = authAdapter.login(event.getData().get("login"), event.getData().get("password"));
        return new Result(token != null ? token.getToken() : null, "login");
    }

    @EventHook(className = "org.cricketmsf.microsite.event.AuthEvent", procedureName = "logout")
    public Object authLogout(AuthEvent event) {
        Boolean ok = authAdapter.logout(event.getData().get("token"));
        return new Result(ok, "logout");
    }

    @EventHook(className = "org.cricketmsf.microsite.event.AuthEvent", procedureName = "check")
    public Object authCheck(AuthEvent event) {
        boolean ok = authAdapter.checkToken(event.getData().get("token"));
        return new Result(ok, "check");
    }

    @EventHook(className = "org.cricketmsf.microsite.event.AuthEvent", procedureName = "reftesh")
    public Object authRefresh(AuthEvent event) {
        boolean ok = authAdapter.refreshToken(event.getData().get("token"));
        return new Result(ok, "refresh");
    }

    /*
    @HttpAdapterHook(adapterName = "ContentService", requestMethod = "OPTIONS")
    public Object contentCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_OK);
        return result;
    }
    @HttpAdapterHook(adapterName = "ContentService", requestMethod = "GET")
    public Object contentGetPublished(Event event) {
        //synchronous processing
        return getEventProcessingResult(new GetContent(event));
    }
    @EventClassHook(className = "org.cricketmsf.microsite.event.GetContent")
    public Object getPublishedContent(GetContent event) {
        try {
            return new ContentRequestProcessor().processGetPublished(event.getOriginalEvent(), cms);
        } catch (Exception e) {
            e.printStackTrace();
            StandardResult r = new StandardResult();
            r.setCode(HttpAdapter.SC_NOT_FOUND);
            return r;
        }
    }
    @EventClassHook(className = "org.cricketmsf.microsite.event.StatusRequested")
    public Object getStatusInfo(StatusRequested event) {
        return SiteAdministrationModule.getInstance().getServiceInfo();
    }
    @HttpAdapterHook(adapterName = "ContentManager", requestMethod = "OPTIONS")
    public Object contentManagerCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_OK);
        return result;
    }
    @HttpAdapterHook(adapterName = "ContentManager", requestMethod = "*")
    public Object contentManagerHandle(Event event) {
        return new ContentRequestProcessor().processRequest(event, cms, translator);
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
    @EventHook(eventCategory = "Category-Test")
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
        switch (event.getType()) {
            case UserEvent.USER_REGISTERED:     //send confirmation email
                try {
                    String uid = (String) event.getPayload();
                    User user = userAdapter.get(uid);
                    gdprLog.log(Event.logInfo(event.getId(), "REGISTERED USER " + user.getNumber()));
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
                    gdprLog.log(Event.logInfo(event.getId(), "DELETE REQUEST FOR " + user.getNumber()));
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
                String[] tmpPayload = ((String) event.getPayload()).split(" ");
                gdprLog.log(Event.logInfo(event.getId(), "DELETED USER " + tmpPayload[0] + " " + tmpPayload[1]));
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
                gdprLog.log(Event.logInfo(event.getId(), "RESET PASSWORD REQUESTED FOR " + event.getPayload()));
            case UserEvent.USER_REG_CONFIRMED:  //TODO: update user
                gdprLog.log(Event.logInfo(event.getId(), "REGISTRATION CONFIRMED FOR " + event.getPayload()));
                break;
            case UserEvent.USER_UPDATED:
                gdprLog.log(Event.logInfo(event.getId(), "USER DATA UPDATED FOR " + event.getPayload()));
                break;
            default:
                dispatchEvent(Event.logInfo(this.getClass().getSimpleName(), "Event recived: " + event.getType()));
                break;
        }
    }
    @EventHook(eventCategory = Event.CATEGORY_GENERIC)
    public void processSystemEvent(Event event) {
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
                SiteAdministrationModule.getInstance().backupDatabases(database, userDB, authDB, cmsDatabase,(String)event.getPayload());
                break;
            default:
                dispatchEvent(Event.logWarning("Don't know how to handle event: " + event.getType(), event.getPayload().toString()));
        }
    }
    @EventHook(eventCategory = "*")
    public void processEvent(Event event) {
        dispatchEvent(Event.logWarning(
                "Event category/type " + event.getCategory() + "/" + event.getType() + " not handled",
                event.getPayload().toString()
        ));
    }
     */
}
