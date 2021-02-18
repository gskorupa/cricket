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

import java.util.HashMap;
import java.util.List;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.HtmlGenAdapterIface;
import org.cricketmsf.out.auth.AuthAdapterIface;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.out.db.*;
import org.cricketmsf.annotation.EventHook;
import org.cricketmsf.api.ResponseCode;
import org.cricketmsf.event.Procedures;
import org.cricketmsf.exception.InitException;
import org.cricketmsf.api.Result;
import org.cricketmsf.api.ResultIface;
import org.cricketmsf.api.StandardResult;
import org.cricketmsf.event.Event;
import org.cricketmsf.event.HttpEvent;
import org.cricketmsf.exception.QueueException;
import org.cricketmsf.in.http.ParameterMapResult;
import org.cricketmsf.in.queue.SubscriberIface;
import org.cricketmsf.in.openapi.OpenApiIface;
import org.cricketmsf.microsite.SiteAdministrationModule;
import org.cricketmsf.microsite.out.cms.CmsIface;
import org.cricketmsf.microsite.out.cms.RuleEngineIface;
import org.cricketmsf.microsite.out.cms.TranslatorIface;
import org.cricketmsf.microsite.event.AuthEvent;
import org.cricketmsf.microsite.event.CmsEvent;
import org.cricketmsf.microsite.event.UserEvent;
import org.cricketmsf.microsite.out.auth.Token;
import org.cricketmsf.microsite.out.cms.ContentRequestProcessor;
import org.cricketmsf.microsite.out.notification.EmailSenderIface;
import org.cricketmsf.microsite.out.siteadmin.SiteAdministrationIface;
import org.cricketmsf.microsite.out.user.User;
import org.cricketmsf.out.log.LoggerAdapterIface;
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
    KeyValueDBIface database = null;
    //SchedulerIface scheduler = null;
    HtmlGenAdapterIface htmlAdapter = null;
    //cms
    KeyValueDBIface cmsDatabase = null;
    CmsIface contentManager = null;
    RuleEngineIface ruleEngine = null;
    TranslatorIface translator = null;
    //user module
    KeyValueDBIface userDB = null;
    UserAdapterIface userAdapter = null;
    //auth module
    KeyValueDBIface authDB = null;
    AuthAdapterIface authAdapter = null;
    //
    EmailSenderIface emailSender = null;
    SubscriberIface queueSubscriber = null;

    LoggerAdapterIface gdprLogger = null;

    OpenApiIface apiGenerator = null;

    public Microsite() {
        super();
        this.configurationBaseName = "Microsite";
    }

    @Override
    public void getAdapters() {
        // standard Cricket adapters
        siteAdmin = (SiteAdministrationIface) getRegistered("SiteAdministrationModule");
        database = (KeyValueDBIface) getRegistered("Database");
        //scheduler = (SchedulerIface) getRegistered("Scheduler"); // not needed -inbound
        htmlAdapter = (HtmlGenAdapterIface) getRegistered("WwwService");
        //cms
        cmsDatabase = (KeyValueDBIface) getRegistered("CmsDB");
        contentManager = (CmsIface) getRegistered("ContentManager");
        ruleEngine = (RuleEngineIface) getRegistered("RuleEngine");
        translator = (TranslatorIface) getRegistered("CmsTranslator");
        //user
        userAdapter = (UserAdapterIface) getRegistered("UserAdapter");
        userDB = (KeyValueDBIface) getRegistered("UserDB");
        //auth
        authAdapter = (AuthAdapterIface) getRegistered("AuthAdapter");
        authDB = (KeyValueDBIface) getRegistered("AuthDB");
        //
        emailSender = (EmailSenderIface) getRegistered("EmailSender");

        queueSubscriber = (SubscriberIface) getRegistered("QueueSubscriber");
        apiGenerator = (OpenApiIface) getRegistered("OpenApi");
        // GDPR
        gdprLogger = (LoggerAdapterIface) getRegistered("GdprLogger");
    }

    @Override
    public void runInitTasks() throws InitException {
        try {
            super.runInitTasks();
        } catch (InitException ex) {
            ex.printStackTrace();
            shutdown();
        }
        eventRouter=new MicrositeEventRouter(this);

        siteAdmin.initDatabases(database, userDB, authDB);
        emailSender.send(
                (String) getProperties().getOrDefault("admin-notification-email", ""),
                getId()+" started", getId()+" service has been started."
        );

        try {
            queueSubscriber.init();
        } catch (QueueException ex) {
            ex.printStackTrace();
            shutdown();
        }

        apiGenerator.init(this);
        setInitialized(true);
        dispatchEvent(
                new Event(Procedures.SYSTEM_STATUS, 5000, getUuid() + " service started", false, this.getClass())
        );
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
        try {
            emailSender.send(
                    (String) getProperties().getOrDefault("admin-notification-email", ""),
                    getId()+" shutdown", getId()+" service is going down."
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.shutdown();
    }

    public AuthAdapterIface getAuthAdapter() {
        return authAdapter;
    }

    /**
     * Process requests from simple web server implementation given by
     * HtmlGenAdapter access web web resources
     *
     * @param event
     * @return ParameterMapResult with the file content as a byte array
     */
    @EventHook(className = "org.cricketmsf.event.HttpEvent", procedure = Procedures.WWW)
    public ResultIface handleWwwRequest(HttpEvent event) {
        RequestObject request = (RequestObject) event.getData();
        String language = (String) request.parameters.get("language");
        if (language == null || language.isEmpty()) {
            language = "en";
        }
        ResultIface result = null;
        String cacheName = "webcache_" + language;
        try {
            result = (ParameterMapResult) contentManager
                    .getFile(request, htmlAdapter.useCache() ? database : null, cacheName, language, true);
            //((HashMap) result.getData()).put("serviceurl", getProperties().get("serviceurl"));
            HashMap rd = (HashMap) result.getData();
            rd.put("serviceurl", getProperties().get("serviceurl"));
            rd.put("defaultLanguage", getProperties().get("default-language"));
            rd.put("token", (String) request.parameters.get("tid"));  // fake tokens doesn't pass SecurityFilter
            rd.put("user", request.headers.getFirst("X-user-id"));
            rd.put("environmentName", getName());
            rd.put("cricketversion", getProperties().getOrDefault("cricket-version", ""));
            rd.put("javaversion", System.getProperty("java.version"));
            rd.put("wwwTheme", getProperties().getOrDefault("www-theme", "theme0"));
            List<String> roles = request.headers.get("X-user-role");
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
        if ("HEAD".equalsIgnoreCase(request.method)) { //quick hack
            byte[] empty = {};
            result.setPayload(empty);
        }
        return result;
    }

    /**
     * Return user data
     *
     * @param event
     * @return
     */
    @EventHook(className = "org.cricketmsf.microsite.event.UserEvent", procedure = Procedures.USER_GET)
    public Result userGet(UserEvent event) {
        return userAdapter.handleGet((HashMap) event.getData()).procedure(Procedures.USER_GET);
    }

    @EventHook(className = "org.cricketmsf.microsite.event.UserEvent", procedure = Procedures.USER_REGISTER)
    public Object userAdd(UserEvent event) {
        return userAdapter.handleRegisterUser((User) event.getData()).procedure(Procedures.USER_REGISTER);
    }

    @EventHook(className = "org.cricketmsf.microsite.event.UserEvent", procedure = Procedures.USER_UPDATE)
    public Object userUpdate(UserEvent event) {
        return userAdapter.handleUpdateRequest((HashMap) event.getData()).procedure(Procedures.USER_UPDATE);
    }

    @EventHook(className = "org.cricketmsf.microsite.event.UserEvent", procedure = Procedures.USER_UPDATED)
    public Object userUpdated(UserEvent event) {
        gdprLogger.print("USER DATA UPDATED FOR " + event.getData());
        return null;
    }

    @EventHook(className = "org.cricketmsf.microsite.event.UserEvent", procedure = Procedures.USER_REMOVAL_SCHEDULED)
    public Object userRemoveSheduled(UserEvent event) {
        try {
            String uid = (String) event.getData();
            User user = userAdapter.get(uid);
            gdprLogger.print("DELETE REQUEST FOR " + user.getNumber());
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
            logger.error(e.getMessage() + " while sending confirmation emai");
        }
        return null;
    }

    @EventHook(className = "org.cricketmsf.microsite.event.UserEvent", procedure = Procedures.USER_REMOVE)
    public Object userRemove(UserEvent event) {
        return userAdapter.handleDeleteUser((HashMap) event.getData()).procedure(Procedures.USER_REMOVE);
    }

    @EventHook(className = "org.cricketmsf.microsite.event.UserEvent", procedure = Procedures.USER_CONFIRM_REGISTRATION)
    public Object userConfirmationRequired(UserEvent event) {
        try {
            String uid = (String) event.getData();
            User user = userAdapter.get(uid);
            gdprLogger.print("REGISTERED USER " + user.getNumber() + " " + user.getUid());
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
            logger.error(e.getMessage() + " while sending confirmation emai");
        }
        return null;
    }

    @EventHook(className = "org.cricketmsf.microsite.event.UserEvent", procedure = Procedures.USER_REGISTRATION_CONFIRMED)
    public Object registrationConfirmed(UserEvent event) {
        gdprLogger.print("REGISTRATION CONFIRMED FOR " + event.getData());
        return null;
    }

    @EventHook(className = "org.cricketmsf.microsite.event.UserEvent", procedure = Procedures.USER_AFTER_REMOVAL)
    public Object userRemoved(UserEvent event) {
        gdprLogger.print("DELETED USER " + event.getData());
        return null;
    }

    @EventHook(className = "org.cricketmsf.microsite.event.UserEvent", procedure = Procedures.USER_RESET_PASSWORD)
    public Object userResetPassword(UserEvent event) {
        String payload = null;
        try {
            payload = (String) event.getData();
        } catch (ClassCastException e) {
        }
        if (payload != null && !payload.isEmpty()) {
            String[] params = payload.split(":");
            if (params.length == 2) {
                //TODO: email templates from CMS
                String passResetLink = properties.getOrDefault("serviceurl", "") + "?tid=" + params[0] + "#account";
                emailSender.send(params[1], "Password Reset Request", "Click here to change password: <a href=\"" + passResetLink + "\">" + passResetLink + "</a>");
            } else {
                logger.warn("Malformed payload->{}", payload);
            }
        } else {
            logger.warn("Malformed payload->{}", payload);
        }
        gdprLogger.print("RESET PASSWORD REQUESTED FOR " + event.getData());
        return null;
    }

    @EventHook(className = "org.cricketmsf.microsite.event.AuthEvent", procedure = Procedures.AUTH_LOGIN)
    public Result authLogin(AuthEvent event) {
        Token token = authAdapter.login(event.getData().get("login"), event.getData().get("password"));
        return new Result(token != null ? token.getToken() : null, Procedures.AUTH_LOGIN);
    }

    @EventHook(className = "org.cricketmsf.microsite.event.AuthEvent", procedure = Procedures.AUTH_LOGOUT)
    public Object authLogout(AuthEvent event) {
        Boolean ok = authAdapter.logout(event.getData().get("token"));
        return new Result(ok, Procedures.AUTH_LOGOUT);
    }

    @EventHook(className = "org.cricketmsf.microsite.event.AuthEvent", procedure = Procedures.AUTH_CHECK_TOKEN)
    public Object authCheck(AuthEvent event) {
        boolean ok = authAdapter.checkToken(event.getData().get("token"));
        return new Result(ok, Procedures.AUTH_CHECK_TOKEN);
    }

    @EventHook(className = "org.cricketmsf.microsite.event.AuthEvent", procedure = Procedures.AUTH_REFRESH_TOKEN)
    public Object authRefresh(AuthEvent event) {
        boolean ok = authAdapter.refreshToken(event.getData().get("token"));
        return new Result(ok, Procedures.AUTH_REFRESH_TOKEN);
    }

    /*
    @EventHook(className = "org.cricketmsf.event.Event", procedure = Procedures.SYSTEM_STATUS)
    public Object getStatusInfo(Event event) {
        System.out.println(siteAdmin.getServiceInfo().getData());
        return null;
    }
*/
    @EventHook(className = "org.cricketmsf.event.Event", procedure = Procedures.SA_ANY)
    public Object systemServiceHandle(Event event) {
        return new SiteAdministrationModule().handleRestEvent(event);
    }

    @EventHook(className = "org.cricketmsf.microsite.event.CmsEvent", procedure = Procedures.CS_GET)
    public Object getPublishedContent(CmsEvent event) {
        try {
            return new ContentRequestProcessor().processGetPublished((HashMap) event.getData(), contentManager);
        } catch (Exception e) {
            e.printStackTrace();
            StandardResult r = new StandardResult();
            r.setCode(ResponseCode.NOT_FOUND);
            return r;
        }
    }

    @EventHook(className = "org.cricketmsf.microsite.event.CmsEvent", procedure = Procedures.CMS_GET)
    public Object getContent(CmsEvent event) {
        try {
            return new ContentRequestProcessor().processGet((HashMap) event.getData(), contentManager);
        } catch (Exception e) {
            e.printStackTrace();
            StandardResult r = new StandardResult();
            r.setCode(ResponseCode.NOT_FOUND);
            return r;
        }
    }

    @EventHook(className = "org.cricketmsf.microsite.event.CmsEvent", procedure = Procedures.CMS_POST)
    public Object setContent(CmsEvent event) {
        try {
            return new ContentRequestProcessor().processPost(event, contentManager);
        } catch (Exception e) {
            e.printStackTrace();
            StandardResult r = new StandardResult();
            r.setCode(ResponseCode.NOT_FOUND);
            return r;
        }
    }

    @EventHook(className = "org.cricketmsf.microsite.event.CmsEvent", procedure = Procedures.CMS_PUT)
    public Object updateContent(CmsEvent event) {
        try {
            return new ContentRequestProcessor().processPut(event, contentManager);
        } catch (Exception e) {
            e.printStackTrace();
            StandardResult r = new StandardResult();
            r.setCode(ResponseCode.NOT_FOUND);
            return r;
        }
    }

    @EventHook(className = "org.cricketmsf.microsite.event.CmsEvent", procedure = Procedures.CMS_DELETE)
    public Object removeContent(CmsEvent event) {
        try {
            return new ContentRequestProcessor().processDelete(event, contentManager);
        } catch (Exception e) {
            e.printStackTrace();
            StandardResult r = new StandardResult();
            r.setCode(ResponseCode.NOT_FOUND);
            return r;
        }
    }

    @EventHook(className = "org.cricketmsf.microsite.event.CmsEvent", procedure = Procedures.CMS_CONTENT_CHANGED)
    public Object clearWebCache(CmsEvent event) {
        try {
            database.clear("webcache_pl");
        } catch (KeyValueDBException ex) {
            logger.warn("Problem while clearing web cache - " + ex.getMessage());
        }
        try {
            database.clear("webcache_en");
        } catch (KeyValueDBException ex) {
            logger.warn("Problem while clearing web cache - " + ex.getMessage());
        }
        try {
            database.clear("webcache_fr");
        } catch (KeyValueDBException ex) {
            logger.warn("Problem while clearing web cache - " + ex.getMessage());
        }
        return null;
    }
    
    @EventHook(className = "org.cricketmsf.event.Event", procedure = Procedures.SYSTEM_SHUTDOWN)
    public Object handleShutdownRequest(Event event) {
        shutdown();
        return null;
    }

    @EventHook(className = "org.cricketmsf.event.Event", procedure = Procedures.SYSTEM_STATUS)
    public Object handleStatusRequest(Event event) {
        System.out.println(printStatus());
        return null;
    }

    @EventHook(className = "org.cricketmsf.event.Event", procedure = Procedures.SYSTEM_BACKUP)
    public Object handleBackupRequest(Event event) {
        SiteAdministrationModule.getInstance().backupDatabases(database, userDB, authDB, cmsDatabase,(String)event.getData());
        return null;
    }

    @EventHook(className = "org.cricketmsf.event.Event")
    public Object logEventsNotHandled(Event event) {
        logger.warn("org.cricketmsf.event.Event procedure {} not handled", getProceduresDictionary().getName(Procedures.DEFAULT));
        return null;
    }
}
