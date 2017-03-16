/*
 * Copyright 2017 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
import java.util.HashMap;
import static org.cricketmsf.Kernel.handle;
import org.cricketmsf.annotation.EventHook;
import org.cricketmsf.annotation.HttpAdapterHook;
import org.cricketmsf.in.http.EchoHttpAdapterIface;
import org.cricketmsf.in.http.HtmlGenAdapterIface;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.HttpAdapterIface;
import org.cricketmsf.in.http.ParameterMapResult;
import org.cricketmsf.in.http.StandardResult;
import org.cricketmsf.in.scheduler.SchedulerIface;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.KeyValueDBIface;
import org.cricketmsf.out.file.FileReaderAdapterIface;
import org.cricketmsf.out.log.LoggerAdapterIface;
import org.cricketmsf.out.script.ScriptingAdapterIface;

/**
 * EchoService
 *
 * @author greg
 */
public class BasicService extends Kernel {

    // adapterClasses
    LoggerAdapterIface logAdapter = null;
    EchoHttpAdapterIface echoAdapter = null;
    KeyValueDBIface database = null;
    SchedulerIface scheduler = null;
    HtmlGenAdapterIface htmlAdapter = null;
    FileReaderAdapterIface fileReader = null;
    // optional
    HttpAdapterIface scriptingService = null;
    ScriptingAdapterIface scriptingEngine = null;

    @Override
    public void getAdapters() {
        // standard Cricket adapters
        logAdapter = (LoggerAdapterIface) getRegistered("Logger");
        echoAdapter = (EchoHttpAdapterIface) getRegistered("Echo");
        database = (KeyValueDBIface) getRegistered("Database");
        scheduler = (SchedulerIface) getRegistered("Scheduler");
        htmlAdapter = (HtmlGenAdapterIface) getRegistered("WWWService");
        fileReader = (FileReaderAdapterIface) getRegistered("FileReader");
        // optional
        //scriptingService = (HttpAdapterIface) getRegistered("ScriptingService");
        //scriptingEngine = (ScriptingAdapterIface) getRegistered("ScriptingEngine");
    }

    @Override
    public void runInitTasks() {
        try {
            database.addTable("webcache", 100, false);
        } catch (KeyValueDBException e) {
        }
        try {
            database.addTable("counters", 1, false);
        } catch (KeyValueDBException e) {
        }
    }

    @Override
    public void runFinalTasks() {
    }

    @Override
    public void runOnce() {
        super.runOnce();
        handleEvent(Event.logInfo("BasicService.runOnce()", "executed"));
    }

    @HttpAdapterHook(adapterName = "ScriptingService", requestMethod = "*")
    public Object doGetScript(Event requestEvent) {
        StandardResult r = scriptingEngine.processRequest(requestEvent.getRequest());
        return r;
    }

    /**
     * Process requests from simple web server implementation given by
     * HtmlGenAdapter access web web resources
     *
     * @param event
     * @return ParameterMapResult with the file content as a byte array
     */
    @HttpAdapterHook(adapterName = "WWWService", requestMethod = "GET")
    public Object doGet(Event event) {

        RequestObject request = event.getRequest();
        ParameterMapResult result
                = (ParameterMapResult) fileReader
                        .getFile(request, htmlAdapter.useCache() ? database : null, "webcache");
        // caching policy 
        result.setMaxAge(120);
        return result;
    }

    @HttpAdapterHook(adapterName = "Echo", requestMethod = "*")
    public Object doGetEcho(Event requestEvent) {
        return sendEcho(requestEvent.getRequest());
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
            handleEvent(Event.logInfo("BasicService", event.getPayload().toString()));
        }
    }

    public Object sendEcho(RequestObject request) {
        StandardResult r = new StandardResult();
        r.setCode(HttpAdapter.SC_OK);
        try {
            if (!echoAdapter.isSilent()) {
                // with echo counter
                Long counter;
                counter = (Long) database.get("counters", "echo.count", new Long(0));
                counter++;
                database.put("counters", "echo.count", counter);
                HashMap<String, Object> data = new HashMap<>(request.parameters);
                data.put("service.id", getId());
                data.put("service.uuid", getUuid().toString());
                data.put("service.name", getName());
                data.put("request.method", request.method);
                data.put("request.pathExt", request.pathExt);
                data.put("echo.counter", database.get("counters", "echo.count"));
                if (data.containsKey("error")) {
                    int errCode = HttpAdapter.SC_INTERNAL_SERVER_ERROR;
                    try {
                        errCode = Integer.parseInt((String) data.get("error"));
                    } catch (Exception e) {
                    }
                    r.setCode(errCode);
                    data.put("error", "error forced by request");
                }
                r.setData(data);
                r.setHeader("x-echo-greeting", "hello");
            }else{
                handle(Event.logFine("BasicService", "echo service is silent"));
            }
        } catch (KeyValueDBException e) {
            handle(Event.logSevere(this.getClass().getSimpleName(), e.getMessage()));
        }
        return r;
    }
}

