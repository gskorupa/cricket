/*
 * Copyright 2017 Grzegorz Skorupa .
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

import java.io.File;
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
import org.cricketmsf.microsite.event.auth.AuthEvent;
import org.cricketmsf.microsite.event.cms.CmsEvent;
import org.cricketmsf.microsite.event.user.UserEvent;
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
        setEventRouter(new MicrositeEventRouter(this));
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
    public void runInitTasks() {
        try {
            super.runInitTasks();
        } catch (InitException ex) {
            ex.printStackTrace();
            shutdown();
        }
        siteAdmin.initDatabases(database, userDB, authDB);
        emailSender.send(
                (String) getProperties().getOrDefault("admin-notification-email", ""),
                getId() + " started", getId() + " service has been started."
        );

        try {
            queueSubscriber.init();
        } catch (QueueException ex) {
            ex.printStackTrace();
            shutdown();
        }

        apiGenerator.init(this);
        setInitialized(true);
        checkAdmWebappInstallation();
        dispatchEvent(
                new Event(Procedures.SYSTEM_STATUS, 5000, getUuid() + " service started", false, this.getClass())
        );
    }

    private void checkAdmWebappInstallation() {
        boolean ok = false;
        try {
            ok = new File("./www/adm/index.html").isFile();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if(!ok){
            logger.warn("The web application for the site administrator is not available. Check if it has been properly installed in the 'www/adm' subfolder.");
        }
    }

    @Override
    public void runFinalTasks() {
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
                    getId() + " shutdown", getId() + " service is going down."
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.shutdown();
    }

    @Override
    public AuthAdapterIface getAuthAdapter() {
        return authAdapter;
    }
    
    public CmsIface getCmsAdapter(){
        return contentManager;
    }

}
