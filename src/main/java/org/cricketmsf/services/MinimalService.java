/*
 * Copyright 2020 Grzegorz Skorupa <g.skorupa at gmail.com>.
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

import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.annotation.EventHook;
import org.cricketmsf.event.EventMaster;
import org.cricketmsf.exception.EventException;
import org.cricketmsf.exception.InitException;
import org.cricketmsf.in.openapi.OpenApiIface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EchoService
 *
 * @author greg
 */
public class MinimalService extends Kernel {

    private static final Logger logger = LoggerFactory.getLogger(MinimalService.class);
    
    // adapterClasses
    OpenApiIface apiGenerator = null;

    public MinimalService() {
        super();
        this.configurationBaseName = "MinimalService";
    }

    @Override
    public void getAdapters() {
        apiGenerator = (OpenApiIface) getRegistered("OpenApi");
    }

    @Override
    public void runInitTasks() {
        try {
            super.runInitTasks();
            // we should register event categories used by this service
            EventMaster.registerEventCategories(new Event().getCategories(), Event.class.getName());
        } catch (InitException | EventException ex) {
            ex.printStackTrace();
            shutdown();
        }
        apiGenerator.init(this);
        setInitialized(true);
    }

    @Override
    public void runFinalTasks() {
        //System.out.println(printStatus());
    }

    @Override
    public void runOnce() {
        super.runOnce();
        apiGenerator.init(this);
        Kernel.getInstance().dispatchEvent(Event.logInfo("MinimalService.runOnce()", "executed"));
    }

    @EventHook(eventCategory = "*")
    public void processEvent(Event event) {
        switch(event.getCategory()){
            case "LOG":
            case "HTTPLOG":
                logger.info(event.getPayload().toString());
                break;
            default:
                logger.info(String.format("Don't know how to handle event category %1s with payload: %2s", event.getCategory(), event.getPayload() != null ? event.getPayload().toString() : "null"));        
        }
    }
    
}
