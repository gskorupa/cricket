/*
 * Copyright 2019 Grzegorz Skorupa .
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
package org.cricketmsf.out.db;

import java.io.File;
import java.util.HashMap;
import org.cricketmsf.Adapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author greg
 */
public class H2RemoteDB extends H2EmbededDB implements SqlDBIface, Adapter {
    private static final Logger logger = LoggerFactory.getLogger(H2RemoteDB.class);
    private String host;
    
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        //we cannot use super.loadProperties(properties, adapterName);
        //so we need these 3 lines:
        this.name = adapterName;
        this.properties = (HashMap<String, String>) properties.clone();
        getStatus(adapterName); //required if we need to overwrite updateStatusItem() method
        //
        setHost(properties.get("host"));
        logger.info("\thost: " + getHost());
        setFileName(properties.get("dbfile"));
        logger.info("\tdbfile: " + getFileName());
        setLocation("tcp://"+getHost()+File.separator+getFileName());
        logger.info("\tdb URL: " + getLocation());
        setTestQuery(properties.get("test-query"));
        logger.info("\ttest-query: " + getTestQuery());
        setSystemVersion(properties.get("version"));
        logger.info("\tversion: " + getSystemVersion());
        setUserName(properties.get("user"));
        logger.info("\tuser: " + getUserName());
        setPassword(properties.get("password"));
        logger.info("\tpassword=" + getPassword());
        setEncrypted(properties.get("encrypted"));
        logger.info("\tencrypted=" + isEncrypted());
        setFilePassword(properties.get("filePassword"));
        logger.info("\tfilePassword=" + getFilePassword());
        setAutocommit(properties.getOrDefault("autocommit", "true"));
        logger.info("\tautocommit=" + autocommit);
        setIgnorecase("true".equalsIgnoreCase(properties.getOrDefault("ignorecase", "false")));
        logger.info("\tignorecase=" + ignorecase);
        setSkipUpdate("true".equalsIgnoreCase(properties.getOrDefault("skip-update", "false")));
        logger.info("\tskip-update=" + skipUpdate);
        setMaxConnections(properties.getOrDefault("max-connections","10"));
        logger.info("\tmax-connections: {}", maxConnections);
        try {
            start();
        } catch (KeyValueDBException ex) {
            logger.error(ex.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }
}
