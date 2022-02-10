/*
 * Copyright 2019 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;

/**
 *
 * @author greg
 */
public class H2RemoteDB extends H2EmbededDB implements SqlDBIface, Adapter {

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
        Kernel.getLogger().print("\thost: " + getPath());
        setFileName(properties.get("dbfile"));
        Kernel.getLogger().print("\tdbfile: " + getFileName());
        setLocation("tcp://"+getHost()+File.separator+getFileName());
        Kernel.getLogger().print("\tdb URL: " + getLocation());
        setTestQuery(properties.get("test-query"));
        Kernel.getLogger().print("\ttest-query: " + getTestQuery());
        setSystemVersion(properties.get("version"));
        Kernel.getLogger().print("\tversion: " + getSystemVersion());
        setUserName(properties.get("user"));
        Kernel.getLogger().print("\tuser: " + getUserName());
        setPassword(properties.get("password"));
        Kernel.getLogger().print("\tpassword=" + getPassword());
        setEncrypted(properties.get("encrypted"));
        Kernel.getLogger().print("\tencrypted=" + isEncrypted());
        setFilePassword(properties.get("filePassword"));
        Kernel.getLogger().print("\tfilePassword=" + getFilePassword());
        setAutocommit(properties.getOrDefault("autocommit", "true"));
        Kernel.getLogger().print("\tautocommit=" + autocommit);
        setIgnorecase("true".equalsIgnoreCase(properties.getOrDefault("ignorecase", "false")));
        Kernel.getLogger().print("\tignorecase=" + ignorecase);
        setSkipUpdate("true".equalsIgnoreCase(properties.getOrDefault("skip-update", "false")));
        Kernel.getLogger().print("\tskip-update=" + skipUpdate);
        setCacheSize(properties.getOrDefault("cache-size", ""));
        Kernel.getLogger().print("\tskip-update=" + skipUpdate);
        try {
            start();
        } catch (KeyValueDBException ex) {
            Kernel.getInstance().dispatchEvent(Event.logSevere(this, ex.getMessage()));
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
        if (host.startsWith("$")) {
            this.host = System.getenv(host.substring(1));
        }else{
            this.host = host;
        }
    }
}
