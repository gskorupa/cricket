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
package org.cricketmsf.in.monitor;

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
public class EnvironmentMonitor extends InboundAdapter implements Adapter, EnvironmentMonitorIface {

    private int samplingInterval = 1000;
    private File disk;
    private String diskPath;
    private String memoryLimitConfig;
    private long memoryLimit;
    private String diskLimitConfig;
    private long diskLimit;
    
    private boolean diskBelow = false;
    private boolean memoryBelow = false;

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
        setSamplingInterval(properties.getOrDefault("sampling-interval", "1000"));
        System.out.println("sampling-interval: "+samplingInterval+" miliseconds");
        setDisk(properties.getOrDefault("disk-path", "."));
        System.out.println("disk-path: "+diskPath);
        setMemoryLimit((properties.getOrDefault("memory-limit", "5M")));
        System.out.println("memory-limit: "+memoryLimitConfig);
        setDiskLimit((properties.getOrDefault("disk-limit", "10M")));
        System.out.println("disk-limit: "+diskLimitConfig);
    }

    @Override
    public void memoryCheck() {
        long memory = Runtime.getRuntime().freeMemory();
        if ((!memoryBelow) && memory <= memoryLimit) {
            handle("memoryCheck", "available memory below " + memoryLimitConfig);
            memoryBelow = true;
        }else if (memoryBelow && (memory > memoryLimit)) {
            handle("memoryCheck", "available memory OK");
            memoryBelow = false;
        }
    }

    @Override
    public void diskCheck() {
        long diskFree = disk.getFreeSpace();
        if ((!diskBelow) && (diskFree <= diskLimit)) {
            handle("diskCheck", "available disk space below " + diskLimitConfig);
            diskBelow = true;
        }else if (diskBelow && (diskFree > diskLimit)) {
            handle("diskCheck", "available disk space OK");
            diskBelow = false;
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(samplingInterval);
                memoryCheck();
                diskCheck();
                Thread.yield();
            }
        } catch (InterruptedException e) {
            Kernel.handle(Event.logWarning(this.getClass().getSimpleName(),"interrupted"));
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

    public void setDisk(String path) {
        diskPath=path;
        disk = new File(path);
    }

    /**
     * @param memoryLimit the memoryLimit to set
     */
    public void setMemoryLimit(String memoryLimit) {
        memoryLimitConfig=memoryLimit;
        this.memoryLimit = calculate(memoryLimit);
    }

    /**
     * @param diskLimit the diskLimit to set
     */
    public void setDiskLimit(String diskLimit) {
        diskLimitConfig = diskLimit;
        this.diskLimit = calculate(diskLimit);
    }

    private long calculate(String sLimit) {
        long limit;
        String unitName;
        String limitName;
        unitName = sLimit.substring(sLimit.length() - 1);
        if (unitName.matches("[0-9]+")) {
            unitName = "B";
            limitName = sLimit;
        } else {
            limitName = sLimit.substring(0, sLimit.length() - 1);
        }
        try {
            limit = Long.parseLong(limitName);
        } catch (NumberFormatException e) {
            limit = 0;
        }
        switch (unitName) {
            case "K":
                limit = limit * 1000;
                break;
            case "M":
                limit = limit * 1000000;
                break;
            case "G":
                limit = limit * 1000000000;
                break;
        }
        return limit;
    }

}
