/*
 * Copyright 2017 Grzegorz Skorupa .
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

import java.io.File;
import java.sql.SQLException;
import org.cricketmsf.event.Event;
import org.cricketmsf.RequestObject;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import org.cricketmsf.Adapter;
import org.cricketmsf.Kernel;
import org.cricketmsf.api.ResponseCode;
import org.cricketmsf.api.StandardResult;
import org.cricketmsf.config.AdapterConfiguration;
import org.cricketmsf.event.ShutdownRequested;
import org.cricketmsf.in.InboundAdapter;
import org.cricketmsf.in.scheduler.SchedulerIface;
import org.cricketmsf.microsite.out.auth.Token;
import org.cricketmsf.microsite.out.user.HashMaker;
import org.cricketmsf.microsite.out.user.User;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.out.OutboundAdapter;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.KeyValueDBIface;
import org.cricketmsf.out.db.SqlDBIface;
import org.cricketmsf.util.FileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Grzegorz Skorupa
 */
public class SiteAdministrationModule {

    private static final Logger logger = LoggerFactory.getLogger(SiteAdministrationModule.class);

    public static final String ADMIN = "admin";
    
    private static SiteAdministrationModule module;
    private String backupFolder = null;
    private boolean backupDaily = false;
    private final int maxCacheSize = 1000;
    private final int maxUsers = 100;
    private String backupStrategy;

    

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
        RequestObject request = (RequestObject) event.getData();
        String method = request.method;
        String moduleName = request.pathExt;
        StandardResult result = new StandardResult();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            result.setCode(ResponseCode.OK);
            return result;
        }
        String userID = request.headers.getFirst("X-user-id");
        List<String> roles = request.headers.get("X-user-role");
        if (!hasAccessRights(userID, roles)) {
            result.setCode(ResponseCode.FORBIDDEN);
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
                    result.setCode(ResponseCode.ACCEPTED);
                    result.setData("the service will be stopped within a few seconds");
                    Kernel.getInstance().dispatchEvent(new ShutdownRequested(10000));
                    break;
                default:
                    result.setCode(ResponseCode.BAD_REQUEST);
            }
        } else if ("POST".equalsIgnoreCase(method)) {
            switch (moduleName.toLowerCase()) {
                case "database":
                    try {
                    String adapterName = (String) request.parameters.getOrDefault("adapter", "");
                    String query = (String) request.parameters.get("query");
                    String backup = (String) request.parameters.get("backup");
                    SqlDBIface adapter;
                    if (null != query) {
                        try {
                            adapter = (SqlDBIface) Kernel.getInstance().getAdaptersMap().get(adapterName);
                            result.setData(adapter.execute(query));
                        } catch (NullPointerException ex) {
                            result.setCode(ResponseCode.BAD_REQUEST);
                            result.setData("unknown adaptername " + adapterName);
                        } catch (ClassCastException ex) {
                            result.setCode(ResponseCode.BAD_REQUEST);
                            result.setData(adapterName + "is not a database adapter");
                        } catch (SQLException ex) {
                            result.setCode(ResponseCode.BAD_REQUEST);
                            result.setData(ex.getMessage());
                        }
                    } else if (null != backup && backup.equalsIgnoreCase("true")) {
                        //TODO
                        adapter = (SqlDBIface) Kernel.getInstance().getAdaptersMap().get(adapterName);
                        File f = adapter.getBackupFile();
                        if (null != f) {
                            result.setPayload(FileReader.readFile(f));
                            result.setMessage("OK");
                            result.setData(null);
                            result.setFileExtension(".zip");
                            result.setModificationDate(new Date());
                            result.setContentType("application/zip");
                            result.setHeader("Content-Disposition", "inline; filename=\""+adapterName+".zip\"");
                            result.setCode(ResponseCode.OK);
                            f.delete();
                        } else {
                            result.setCode(ResponseCode.BAD_REQUEST);
                            result.setData("error while creating backup file");
                        }
                    } else {
                        result.setCode(ResponseCode.BAD_REQUEST);
                        result.setData("query not set");
                    }
                } catch (Exception e) {
                    result.setCode(ResponseCode.INTERNAL_SERVER_ERROR);
                    result.setData(e.getMessage());
                    e.printStackTrace();
                }
                break;
                case "status":
                    try {
                    String newStatus = (String) request.parameters.getOrDefault("status", "");
                    if ("online".equalsIgnoreCase(newStatus)) {
                        Kernel.getInstance().setStatus(Kernel.ONLINE);
                    } else if ("maintenance".equalsIgnoreCase(newStatus)) {
                        Kernel.getInstance().setStatus(Kernel.MAINTENANCE);
                    }
                    result = getServiceInfo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
                case "adapter":
                    String name = (String) request.parameters.getOrDefault("name", "");
                    String propertyName = (String) request.parameters.getOrDefault("property", "");
                    String newValue = (String) request.parameters.getOrDefault("value", "");
                    if (name.isEmpty()) {
                        result.setCode(ResponseCode.BAD_REQUEST);
                        result.setData("At least adapter name must be specified");
                        break;
                    }
                    Object adapter = Kernel.getInstance().getAdaptersMap().getOrDefault(name, null);
                    if (null == adapter) {
                        result.setCode(ResponseCode.BAD_REQUEST);
                        result.setData("Adapter not found");
                        break;
                    }
                    if (adapter instanceof InboundAdapter) {
                        if (!newValue.isEmpty() && !propertyName.isEmpty()) {
                            ((InboundAdapter) adapter).setProperty(propertyName, newValue);

                        }
                    } else if (adapter instanceof OutboundAdapter) {
                        if (!newValue.isEmpty() && !propertyName.isEmpty()) {
                            ((OutboundAdapter) adapter).setProperty(propertyName, newValue);
                        }
                    }
                    result.setData(new AdapterConfiguration((Adapter) adapter));
                    break;

            }
        } else {
            result.setCode(ResponseCode.METHOD_NOT_ALLOWED);
        }
        return result;
    }

    public StandardResult getServiceInfo() {
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
     * @param database TODO doc
     * @param userDB TODO doc
     * @param authDB TODO doc
     */
    public void initDatabases(
            KeyValueDBIface database,
            KeyValueDBIface userDB,
            KeyValueDBIface authDB
    ) {
        backupFolder = (String) Kernel.getInstance().getProperties().get("backup-folder");
        try {
            //backupDaily = Boolean.parseBoolean((String) Kernel.getInstance().getProperties().get("backup-daily"));
            backupStrategy = ((String) Kernel.getInstance().getProperties().getOrDefault("backup-strategy", "")).toLowerCase();
        } catch (ClassCastException e) {
            backupStrategy = "";
        }
        if (backupFolder == null) {
            logger.error("Kernel parameter \"backup-folder\" not configured");
        }
        if (backupFolder != null && !backupFolder.endsWith(System.getProperty("file.separator"))) {
            backupFolder = backupFolder.concat(System.getProperty("file.separator"));
        }
        // web moduleName CACHE
        try {
            database.addTable("webcache_pl", maxCacheSize, false);
        } catch (ClassCastException | KeyValueDBException e) {
            logger.info(e.getMessage());
        }
        try {
            database.addTable("webcache_en", maxCacheSize, false);
        } catch (ClassCastException | KeyValueDBException e) {
            logger.info(e.getMessage());
        }
        try {
            database.addTable("webcache_fr", maxCacheSize, false);
        } catch (ClassCastException | KeyValueDBException e) {
            logger.info(e.getMessage());
        }

        // USERS DB
        try {
            userDB.addTable("users", maxUsers, true);
        } catch (KeyValueDBException e) {
            logger.info(e.getMessage());
        }
        try {
            String initialAdminEmail = (String) Kernel.getInstance().getProperties().getOrDefault("initial-admin-email", "");
            String initialAdminPassword = (String) Kernel.getInstance().getProperties().getOrDefault("initial-admin-password", "");
            if (initialAdminPassword.isEmpty()) {
                logger.error("initial-admin-password property not set. Login to the administrator's account will be impossible!");
            }
            if (initialAdminEmail.isEmpty()) {
                logger.warn("initial-admin-email property not set.");
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
            if (!userDB.containsKey("users", "tester")) {
                //create user public
                newUser = new User();
                newUser.setUid("tester");
                newUser.setEmail("");
                newUser.setType(User.USER);
                newUser.setRole("user");
                newUser.setPassword(HashMaker.md5Java("cricket"));
                newUser.setConfirmString("1234567890");
                newUser.setConfirmed(true);
                userDB.put("users", newUser.getUid(), newUser);
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

        } catch (ClassCastException | KeyValueDBException e) {
            logger.info(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // AUTH / IDM
        try {
            authDB.addTable("tokens", 2 * maxUsers, false);
        } catch (ClassCastException | KeyValueDBException e) {
            logger.info(e.getMessage());
        }

    }

    /**
     * Creates events that should be fired on the Service start.
     *
     * @param scheduler Scheduler
     */
    public void initScheduledTasks(SchedulerIface scheduler) {
        logger.warn("method not implemented");
    }

    /**
     * Runs backup for all databases
     *
     * @param database TODO doc
     * @param userDB TODO doc
     * @param authDB TODO doc
     * @param cmsDB TODO doc
     * @param errorLevel TODO doc
     */
    public void backupDatabases(
            KeyValueDBIface database,
            KeyValueDBIface userDB,
            KeyValueDBIface authDB,
            KeyValueDBIface cmsDB,
            String errorLevel
    ) {
        String message = "";
        String prefix; // = backupDaily ? getDateString() : "";
        switch (backupStrategy) {
            case "overwrite":
                prefix = "";
                break;
            case "day":
                prefix = getDateString("yyyyMMdd-");
                break;
            case "week":
                prefix = getDateString("E-");
                break;
            case "month":
                prefix = getDateString("d-");
                break;
            default:
                prefix = "";
        }
        try {
            database.backup(backupFolder + prefix + database.getBackupFileName());
        } catch (KeyValueDBException ex) {
            message = ex.getMessage();
        }
        try {
            cmsDB.backup(backupFolder + prefix + cmsDB.getBackupFileName());
        } catch (KeyValueDBException ex) {
            message = ex.getMessage();
        }
        try {
            userDB.backup(backupFolder + prefix + userDB.getBackupFileName());
        } catch (KeyValueDBException ex) {
            message = ex.getMessage();
        }
        try {
            authDB.backup(backupFolder + prefix + authDB.getBackupFileName());
        } catch (KeyValueDBException ex) {
            message = ex.getMessage();
        }
        if (!message.isEmpty()) {
            if ("warning".equalsIgnoreCase(errorLevel)) {
                logger.warn("backup error - " + message);
            } else if ("info".equalsIgnoreCase(errorLevel)) {
                logger.info("backup error - " + message);
            } else if ("debug".equalsIgnoreCase(errorLevel)) {
                logger.debug("backup error - " + message);
            } else {
                logger.error("backup error - " + message);
            }
        }
        //TODO: scheduler
        logger.info("database backup done");
    }

    public void clearUserData(String userId) {
        logger.warn("method clearUserData not implemented");
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

    private String getDateString(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }

}
