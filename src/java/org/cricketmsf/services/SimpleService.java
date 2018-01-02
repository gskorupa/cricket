/*
 * Copyright 2018 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
import org.cricketmsf.RequestObject;
import org.cricketmsf.annotation.EventHook;
import org.cricketmsf.annotation.HttpAdapterHook;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.ParameterMapResult;
import org.cricketmsf.in.http.StandardResult;
import org.cricketmsf.in.scheduler.SchedulerIface;
import org.cricketmsf.out.db.KeyValueDBIface;
import org.cricketmsf.out.file.FileReaderAdapterIface;
import org.cricketmsf.out.log.LoggerAdapterIface;

/**
 * EchoService
 *
 * @author greg
 */
public class SimpleService extends Kernel {

    // adapterClasses
    LoggerAdapterIface logAdapter = null;
    KeyValueDBIface database = null;
    SchedulerIface scheduler = null;
    FileReaderAdapterIface fileReader = null;

    @Override
    public void getAdapters() {
        logAdapter = (LoggerAdapterIface) getRegistered("Logger");
        fileReader = (FileReaderAdapterIface) getRegistered("FileReader");
        database = (KeyValueDBIface) getRegistered("Database");
        scheduler = (SchedulerIface) getRegistered("Scheduler");
    }

    /**
     *
     * @param event
     * @return ParameterMapResult with the file content as a byte array
     */
    @HttpAdapterHook(adapterName = "WWWService", requestMethod = "GET")
    public Object handleWwwRequests(Event event) {
        RequestObject request = event.getRequest();
        ParameterMapResult result = (ParameterMapResult) fileReader.getFile(request, null);
        // caching policy 
        result.setMaxAge(120);
        return result;
    }

    @HttpAdapterHook(adapterName = "StatusService", requestMethod = "GET")
    public Object handleStatusRequest(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_OK);
        result.setData(reportStatus());
        return result;
    }
    
    @Override
    public void runInitTasks() {
        System.out.println(printStatus());
    }

    @Override
    public void runFinalTasks() {
    }

    @Override
    public void runOnce() {
        super.runOnce();
        handleEvent(Event.logInfo("SimpleService.runOnce()", "executed"));
    }

    @EventHook(eventCategory = Event.CATEGORY_LOG)
    public void logEvent(Event event) {
        logAdapter.log(event);
    }

    @EventHook(eventCategory = Event.CATEGORY_HTTP_LOG)
    public void logHttpEvent(Event event) {
        logAdapter.log(event);
    }

    @EventHook(eventCategory = "*")
    public void processEvent(Event event) {
        if (event.getTimePoint() != null) {
            scheduler.handleEvent(event);
        } else {
            handleEvent(Event.logInfo("SimpleService", event.getPayload().toString()));
        }
    }
}
