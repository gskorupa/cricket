/*
 * Copyright 2016 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package org.cricketmsf.in.file;

import java.io.File;
import java.util.HashMap;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.in.InboundAdapter;

/**
 *
 * @author greg
 */
public class Watchdog extends InboundAdapter implements Adapter, WatchdogIface {

    private String folderName;
    File folder;
    private int samplingInterval = 1000;
    private HashMap<String, Long> files = new HashMap<>();

    /**
     * This method is executed while adapter is instantiated during the service
     * start. It's used to configure the adapter according to the configuration.
     *
     * @param properties map of properties readed from the configuration file
     * @param adapterName name of the adapter set in the configuration file (can
     * be different from the interface and class name.
     */
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.getServiceHooks(adapterName);
        setFolder(properties.getOrDefault("path", ""));
        System.out.println("path=" + folderName);
        setSamplingInterval(properties.getOrDefault("sampling-interval", "1000"));
    }

    @Override
    public void checkStatus() {
        long modified;
        long lastModified;
        for (String path : files.keySet()) {
            modified = new File(path).lastModified();
            lastModified = files.get(path);
            if (modified > lastModified) {
                files.put(path, modified);
                handle("modified", "modification noticed:" + path);
            }
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                checkStatus();
                Thread.sleep(samplingInterval);
                Thread.yield();
            }
        } catch (InterruptedException e) {
            Kernel.handle(Event.logWarning(this.getClass().getSimpleName(), "interrupted"));
        }
    }

    /**
     * @param samplingInterval the samplingInterval to set
     */
    public void setSamplingInterval(String samplingInterval) {
        try {
            this.samplingInterval = Integer.parseInt(samplingInterval);
        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * @param folderName the folderName to set
     */
    public void setFolder(String folderName) {
        this.folderName = folderName;
        folder = new File(folderName);
        try {
            if (!folder.exists()) {
                folder = null;
                System.out.println("folder not found");
            } else if (folder.isDirectory()) {
                File[] children = folder.listFiles();
                for (File child : children) {
                    files.put(child.getPath(), child.lastModified());
                }
            } else {
                files.put(folder.getPath(), folder.lastModified());
            }
        } catch (SecurityException e) {
            System.out.println(e.getMessage());
        }
    }

}
