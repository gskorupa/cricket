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
package org.cricketmsf.microsite;

import org.cricketmsf.Event;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.StandardResult;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import org.cricketmsf.Kernel;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.scheduler.SchedulerIface;
import org.cricketmsf.microsite.out.auth.Token;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.microsite.user.HashMaker;
import org.cricketmsf.microsite.user.User;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.KeyValueDBIface;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class SiteAdministrationModule {

    private static SiteAdministrationModule module;
    private String backupFolder = null;
    private boolean backupDaily = false;
    private final int maxCacheSize = 1000;
    private final int maxUsers = 100;
    
    private final String ADMIN = "admin";
    
    private boolean hasAccessRights(String userID, List<String> roles) {
        if (userID == null || userID.isEmpty()) {
            return false;
        }
        return roles.contains(ADMIN);
    }

    /**
     * Returns the class instance
     *
     * @return SiteAdministrationModule instance object
     */
    public static SiteAdministrationModule getInstance() {
        if (module == null) {
            module = new SiteAdministrationModule();
        }
        return module;
    }

    /**
     * Process API requests related to platform administration
     *
     * @param event HTTP request encapsulated in Event object
     * @return Result object encapsulating HTTP response
     */
    public Object handleRestEvent(Event event) {
        RequestObject request = event.getRequest();
        String method = request.method;
        String moduleName = request.pathExt;
        StandardResult result = new StandardResult();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            result.setCode(HttpAdapter.SC_OK);
            return result;
        }
        String userID = request.headers.getFirst("X-user-id");
        List<String> roles = request.headers.get("X-user-role");
        if (!hasAccessRights(userID, roles)) {
            result.setCode(HttpAdapter.SC_FORBIDDEN);
            return result;
        }
        if ("GET".equalsIgnoreCase(method)) {
            switch (moduleName.toLowerCase()) {
                case "status":
                    result = getServiceInfo();
                    break;
                case "config":
                    result = getServiceConfig();
                    break;
                case "shutdown":
                    result.setCode(HttpAdapter.SC_ACCEPTED);
                    result.setData("the service will be stopped within few seconds");
                    Kernel.getInstance().dispatchEvent(
                            new Event(
                                    this.getClass().getSimpleName(),
                                    Event.CATEGORY_GENERIC,
                                    "SHUTDOWN",
                                    "+5s",
                                    ""
                            )
                    );
                    break;
                default:
                    result.setCode(HttpAdapter.SC_BAD_REQUEST);
            }
        } else {
            result.setCode(HttpAdapter.SC_METHOD_NOT_ALLOWED);
        }
        return result;
    }

    private StandardResult getServiceInfo() {
        StandardResult result = new StandardResult();
        result.setData(Kernel.getInstance().reportStatus());
        return result;
    }
    
    private StandardResult getServiceConfig() {
        StandardResult result = new StandardResult();
        result.setData(Kernel.getInstance().getConfigSet().getConfigurationById(Kernel.getInstance().getId()));
        return result;
    }

    /**
     * Creates required database structure and default objects
     *
     * @param database
     * @param userDB
     * @param authDB
     * @param thingsDB
     * @param iotDataDB
     * @param actuatorCommandsDB
     */
    public void initDatabases(
            KeyValueDBIface database,
            KeyValueDBIface userDB,
            KeyValueDBIface authDB
    ) {
        backupFolder = (String) Kernel.getInstance().getProperties().get("backup-folder");
        try {
            backupDaily = Boolean.parseBoolean((String) Kernel.getInstance().getProperties().get("backup-daily"));
        } catch (ClassCastException e) {
        }
        if (backupFolder!=null && !backupFolder.endsWith(System.getProperty("file.separator"))) {
            backupFolder = backupFolder.concat(System.getProperty("file.separator"));
        }
        // web moduleName CACHE
        try {
            database.addTable("webcache_pl", maxCacheSize, false);
        } catch (ClassCastException | KeyValueDBException e) {
            Kernel.getInstance().dispatchEvent(Event.logInfo(getClass().getSimpleName(), e.getMessage()));
        }
        try {
            database.addTable("webcache_en", maxCacheSize, false);
        } catch (ClassCastException | KeyValueDBException e) {
            Kernel.getInstance().dispatchEvent(Event.logInfo(getClass().getSimpleName(), e.getMessage()));
        }
        try {
            database.addTable("webcache_fr", maxCacheSize, false);
        } catch (ClassCastException | KeyValueDBException e) {
            Kernel.getInstance().dispatchEvent(Event.logInfo(getClass().getSimpleName(), e.getMessage()));
        }

        // USERS DB
        try {
            userDB.addTable("users", maxUsers, true);
        } catch (KeyValueDBException e) {
            Kernel.getInstance().dispatchEvent(Event.logInfo(getClass().getSimpleName(), e.getMessage()));
        }
        try {
            String initialAdminEmail = (String) Kernel.getInstance().getProperties().getOrDefault("initial-admin-email", "");
            String initialAdminPassword = (String) Kernel.getInstance().getProperties().getOrDefault("initial-admin-password", "");
            if (initialAdminEmail.isEmpty() || initialAdminPassword.isEmpty()) {
                Kernel.getInstance().dispatchEvent(Event.logSevere(this, "initial-admin-email or initial-admin-secret properties not set. Stop the server now!"));
            }
            User newUser;
            //create admin account
            if (!userDB.containsKey("users", "admin")) {
                newUser = new User();
                newUser.setUid("admin");
                newUser.setEmail(initialAdminEmail);
                newUser.setType(User.OWNER);
                newUser.setRole("admin,redactor");
                newUser.setPassword(HashMaker.md5Java(initialAdminPassword));
                Random r = new Random(System.currentTimeMillis());
                newUser.setConfirmString(Base64.getEncoder().withoutPadding().encodeToString(("" + r.nextLong()).getBytes()));
                // no confirmation necessary for initial admin account
                newUser.setConfirmed(true);
                System.out.println("CREATING admin");
                userDB.put("users", newUser.getUid(), newUser);
                System.out.println("CREATING admin DONE");
            }
            //create user demo
            if (Kernel.getInstance().getName().toLowerCase(Locale.getDefault()).contains("demo")) {
                if (!userDB.containsKey("users", "demo")) {
                    newUser = new User();
                    newUser.setUid("demo");
                    newUser.setEmail(initialAdminEmail);
                    newUser.setType(User.DEMO);
                    newUser.setRole("user");
                    newUser.setPassword(HashMaker.md5Java("demo"));
                    newUser.setConfirmString("1234567890");
                    // no confirmation necessary for test account
                    newUser.setConfirmed(true);
                    userDB.put("users", newUser.getUid(), newUser);
                }
            }

            if (!userDB.containsKey("users", "public")) {
                //create user public
                newUser = new User();
                newUser.setUid("public");
                newUser.setEmail("");
                newUser.setType(User.READONLY);
                newUser.setRole("guest");
                newUser.setConfirmed(true);
                userDB.put("users", newUser.getUid(), newUser);
            }

        } catch (ClassCastException | KeyValueDBException e) {
            Kernel.getInstance().dispatchEvent(Event.logInfo(getClass().getSimpleName(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // AUTH / IDM
        try {
            authDB.addTable("tokens", 2 * maxUsers, false);
        } catch (ClassCastException | KeyValueDBException e) {
            Kernel.getInstance().dispatchEvent(Event.logInfo(getClass().getSimpleName(), e.getMessage()));
        }

        // CMS
        /*
        try{
           cms.initialize(cmsDatabase, cmsFileReader, logAdapter);
        }catch(CmsException e){
           //TODO: 
        }*/
    }

    /**
     * Creates events that should be fired on the Service start.
     */
    public void initScheduledTasks(SchedulerIface scheduler) {
        String initialTasks = scheduler.getProperty("init");
        String[] params;
        String[] tasks;
        if (initialTasks != null && !initialTasks.isEmpty()) {
            tasks = initialTasks.split(";");
            for (int i = 0; i < tasks.length; i++) {
                params = tasks[i].split(",");
                if (params.length == 6) {
                    scheduler.handleEvent(
                            new Event(params[1], params[2], params[3], params[4], params[5]).putName(params[0]), false, true);
                }
            }
        }
    }

    /**
     * Runs backup for all databases
     *
     * @param database
     * @param userDB
     * @param authDB
     * @param cmsDB
     * @param thingsDB
     * @param iotDataDB
     * @param actuatorCommandsDB
     */
    public void backupDatabases(
            KeyValueDBIface database,
            KeyValueDBIface userDB,
            KeyValueDBIface authDB,
            KeyValueDBIface cmsDB
    ) {
        String prefix = backupDaily ? getDateString() : "";
        try {
            database.backup(backupFolder + prefix + database.getBackupFileName());
        } catch (KeyValueDBException ex) {
            Kernel.getInstance().dispatchEvent(Event.logSevere(this, "backup error - " + ex.getMessage()));
        }
        try {
            cmsDB.backup(backupFolder + prefix + cmsDB.getBackupFileName());
        } catch (KeyValueDBException ex) {
            Kernel.getInstance().dispatchEvent(Event.logSevere(this, "backup error - " + ex.getMessage()));
        }
        try {
            userDB.backup(backupFolder + prefix + userDB.getBackupFileName());
        } catch (KeyValueDBException ex) {
            Kernel.getInstance().dispatchEvent(Event.logSevere(this, "backup error - " + ex.getMessage()));
        }
        try {
            authDB.backup(backupFolder + prefix + authDB.getBackupFileName());
        } catch (KeyValueDBException ex) {
            Kernel.getInstance().dispatchEvent(Event.logSevere(this, "backup error - " + ex.getMessage()));
        }
        //TODO: scheduler
        Kernel.getInstance().dispatchEvent(Event.logInfo(this, "database backup done"));
    }

    public void clearUserData(String userId) {
        Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "method clearUserData not implemented"));
    }

    public void clearData(
            boolean demoMode,
            String category,
            String type,
            UserAdapterIface userAdapter,
            KeyValueDBIface database) {

        //System.out.println("CLEARDATA:" + category + "," + type);
        //clear all expired tokens and permanent tokens
        Map tokens;
        Token t;
        try {
            tokens = database.getAll("tokens");
            tokens.keySet().forEach((key) -> {
                try {
                    if (!((Token) database.get("tokens", (String) key)).isValid()) {
                        database.remove("tokens", (String) key);
                    }
                } catch (KeyValueDBException ex) {
                    ex.printStackTrace();
                }
            });
        } catch (KeyValueDBException ex) {
            ex.printStackTrace();
        }
        try {
            tokens = database.getAll("ptokens");
            tokens.keySet().forEach((key) -> {
                try {
                    if (!((Token) database.get("ptokens", (String) key)).isValid()) {
                        database.remove("ptokens", (String) key);
                    }
                } catch (KeyValueDBException ex) {
                    ex.printStackTrace();
                }
            });
        } catch (KeyValueDBException ex) {
            ex.printStackTrace();
        }

    }
    
    private String getDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-");
        return sdf.format(new Date());
    }

}
