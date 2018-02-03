/*
 * Copyright 2015 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
import org.cricketmsf.annotation.EventHook;
import org.cricketmsf.annotation.HttpAdapterHook;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.out.log.LoggerAdapterIface;
import java.util.HashMap;
import org.cricketmsf.annotation.InboundAdapterHook;
import org.cricketmsf.annotation.RestApiErrorCode;
import org.cricketmsf.annotation.RestApiParameter;
import org.cricketmsf.annotation.RestApiResult;
import org.cricketmsf.annotation.RestApiResultCode;
import org.cricketmsf.annotation.RestApiUriVariables;
import org.cricketmsf.in.http.EchoHttpAdapterIface;
import org.cricketmsf.in.http.HtmlGenAdapterIface;
import org.cricketmsf.in.http.HttpAdapterIface;
import org.cricketmsf.in.http.StandardResult;
import org.cricketmsf.in.scheduler.SchedulerIface;
import org.cricketmsf.out.file.FileReaderAdapterIface;
import org.cricketmsf.in.http.ParameterMapResult;
import org.cricketmsf.in.monitor.EnvironmentMonitorIface;
import org.cricketmsf.out.db.KeyValueDB;
import org.cricketmsf.out.db.KeyValueDBException;

/**
 * EchoService
 *
 * @author greg
 */
public class EchoService extends Kernel {

    // adapterClasses
    LoggerAdapterIface logAdapter = null;
    EchoHttpAdapterIface httpAdapter = null;
    KeyValueDB database = null;
    SchedulerIface scheduler = null;
    HtmlGenAdapterIface htmlAdapter = null;
    FileReaderAdapterIface fileReader = null;
    EchoHttpAdapterIface schedulerTester = null;
    HttpAdapterIface scriptTester = null;
    EnvironmentMonitorIface envMonitor = null;

    @Override
    public void getAdapters() {
        logAdapter = (LoggerAdapterIface) getRegistered("LoggerAdapterIface");
        logger=logAdapter;
        httpAdapter = (EchoHttpAdapterIface) getRegistered("EchoAdapter");
        database = (KeyValueDB) getRegistered("nosql");
        scheduler = (SchedulerIface) getRegistered("SchedulerIface");
        htmlAdapter = (HtmlGenAdapterIface) getRegistered("HtmlGenAdapterIface");
        fileReader = (FileReaderAdapterIface) getRegistered("FileReaderAdapterIface");
        schedulerTester = (EchoHttpAdapterIface) getRegistered("TestScheduler");
        scriptTester = (HttpAdapterIface) getRegistered("ScriptingService");
        envMonitor = (EnvironmentMonitorIface) getRegistered("EnvironmentMonitor");
    }

    @Override
    public void runOnce() {
        super.runOnce();
        dispatchEvent(Event.logInfo("EchoService.runOnce()", "executed"));
        Event e = new Event("EchoService.runOnce()", "beep", "", "+5s", "I'm event from runOnce() processed by scheduler. Hello!");
        processEvent(e);
    }

    @Override
    public void runInitTasks() {
        try {
            database.addTable("wwwcache", 100, false);
        } catch (KeyValueDBException e) {
            Kernel.getInstance().dispatchEvent(Event.logFinest("EchoService.runInitTasks", "table wwwcache exists"));
        }
        try {
            database.addTable("counters", 10, true);
        } catch (KeyValueDBException e) {
            Kernel.getInstance().dispatchEvent(Event.logFinest("EchoService.runInitTasks", "table counters exists"));
        }
    }

    @Override
    public void runFinalTasks() {
    }

    @EventHook(eventCategory = "*")
    public void processEvent(Event event) {
        if (event.getTimePoint() != null) {
            scheduler.handleEvent(event);
        } else {
            dispatchEvent(Event.logInfo("EchoService", event.getPayload().toString()));
        }
    }

    /**
     * Process requests from simple web server implementation given by
     * HtmlGenAdapter access web web resources
     *
     * @param event
     * @return ParameterMapResult with the file content as a byte array
     */
    @HttpAdapterHook(adapterName = "HtmlGenAdapterIface", requestMethod = "GET")
    public Object htmlGet(Event event) {

        RequestObject request = event.getRequest();
        ParameterMapResult result
                = (ParameterMapResult) fileReader
                        .getFile(request, htmlAdapter.useCache() ? (KeyValueDB) database : null, "webcache");
        return result;
    }

    @HttpAdapterHook(adapterName = "TestScheduler", requestMethod = "GET")
    public Object scheduleEvent(Event requestEvent) {
        Event e = new Event("EchoService.runOnce()", "beep", "", "+5s", "I'm event from doGetEcho() processed by scheduler. Hello!");
        processEvent(e);
        return sendEcho(requestEvent.getRequest());
    }

    /**
     * Handling request from EchoAdapter. The method is linked to the adapter by
     * using @HttpAdapterHook annotation. Other annotations are for the API
     * documentation only and has no result in the method or the service logic.
     *
     * @param requestEvent
     * @return
     */
    @HttpAdapterHook(adapterName = "EchoAdapter", requestMethod = "GET")
    @RestApiUriVariables(path = "/{id}", description = "eg. object ID (here not used)")
    @RestApiParameter(name = "name", constraint = "optional", description = "")
    @RestApiParameter(name = "surname", constraint = "optional", description = "")
    @RestApiResult(description = "StandardResult with copy of request parameters")
    @RestApiResultCode(code = 200, description = "OK")
    @RestApiErrorCode(code = 500, description = "error forced by request parameter")
    public Object doGetEcho(Event requestEvent) {
        return sendEcho(requestEvent.getRequest());
    }

    @InboundAdapterHook(adapterName = "EnvironmentAdapter", inputMethod = "*")
    public Object processMonitoringEvent(Event event) {
        dispatchEvent(Event.logInfo(this.getClass().getSimpleName(), (String) event.getPayload()));
        return null;
    }

    @EventHook(eventCategory = Event.CATEGORY_LOG)
    public void logEvent(Event event) {
        logAdapter.log(event);
    }

    @EventHook(eventCategory = Event.CATEGORY_HTTP_LOG)
    public void logHttpEvent(Event event) {
        logAdapter.log(event);
    }

    public Object sendEcho(RequestObject request) {
        StandardResult r = new StandardResult();
        r.setCode(HttpAdapter.SC_OK);
        if (!httpAdapter.isSilent()) {
            // with echo counter
            Long counter = 0L;
            try {
                counter = (Long) database.get("echo", "counter", new Long(0));
            } catch (KeyValueDBException e) {
                e.printStackTrace();
            }
            counter++;
            try {
                database.put("echo", "counter", counter);
            } catch (KeyValueDBException e) {
                e.printStackTrace();
            }
            HashMap<String, Object> data = new HashMap<>(request.parameters);
            data.put("service.uuid", getUuid().toString());
            data.put("request.method", request.method);
            data.put("request.pathExt", request.pathExt);
            counter = -1L;
            try {
                data.put("echo.counter", database.get("echo", "counter"));
            } catch (KeyValueDBException e) {
                e.printStackTrace();
            }
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
        }
        return r;
    }

}
