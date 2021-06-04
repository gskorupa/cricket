/*
 * Copyright 2020 Grzegorz Skorupa .
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

import org.cricketmsf.Kernel;
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
        } catch (InitException ex) {
            ex.printStackTrace();
            shutdown();
        }
        if(null!=apiGenerator){
            apiGenerator.init(this);
        }
        setInitialized(true);
    }

    @Override
    public void runFinalTasks() {
        
    }

    @Override
    public void runOnce() {
        super.runOnce();
        if(null!=apiGenerator){
            apiGenerator.init(this);
        }
        logger.info("MinimalService.runOnce() executed");
    }
    
}
