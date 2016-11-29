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
package org.cricketmsf.in.cli;

import java.io.Console;
import java.util.HashMap;
import org.cricketmsf.Adapter;
import org.cricketmsf.in.InboundAdapter;

/**
 *
 * @author greg
 */
public class Cli extends InboundAdapter implements Adapter, CliIface {

    private int samplingInterval = 1000;
    Console c = System.console();
    private boolean started = false;
    private String command;

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
    }

    @Override
    public void start() {
        started = true;
    }

    public void readCommand() {
        command = c.readLine("Enter command: ");
        c.format("Command is %s.%n", command);
    }

    @Override
    public void run() {
        try {
            while (!started) {
                Thread.sleep(samplingInterval);
            }
        } catch (InterruptedException e) {
            System.out.println("CLI interrupted");
        }
        if (started) {
            try {
                while (true) {
                    readCommand();
                    Thread.sleep(samplingInterval);
                }
            } catch (InterruptedException e) {
                System.out.println("CLI interrupted");
            }
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

}
