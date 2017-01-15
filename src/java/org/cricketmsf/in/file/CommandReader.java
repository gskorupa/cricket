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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.in.InboundAdapter;
import org.cricketmsf.in.file.WatchdogIface;

/**
 *
 * @author greg
 */
public class CommandReader extends InboundAdapter implements Adapter, WatchdogIface {

    private final String INBOUND_METHOD_NAME = "dataready";

    private String fileName;
    File file;
    private int samplingInterval = 1000;

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
        setFile(properties.getOrDefault("path", ""));
        System.out.println("\tpath=" + fileName);
        setSamplingInterval(properties.getOrDefault("sampling-interval", "1000"));
        System.out.println("\tsampling-interval=" + samplingInterval);
    }

    @Override
    public void checkStatus() {
        byte[] content = exec();
        if (content.length > 0) {
            handle(INBOUND_METHOD_NAME, new String(content));
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                checkStatus();
                Thread.sleep(samplingInterval);
                //Thread.yield();
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
    public void setFile(String folderName) {
        this.fileName = folderName;
        file = new File(folderName);
        try {
            if (!file.exists()) {
                file = null;
                System.out.println("file not found");
            } else if (file.isDirectory()) {
                System.out.println("directory found");
                file = null;
            }
        } catch (SecurityException e) {
            System.out.println(e.getMessage());
        }
    }

    private byte[] exec() {
        try {
            Process p = Runtime.getRuntime().exec(fileName);
            p.waitFor();

            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            return sb.toString().getBytes();
        } catch (Exception e) {
            return new byte[0];
        }
    }

}
