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

import java.util.HashMap;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.annotation.EventHook;
import org.cricketmsf.annotation.HttpAdapterHook;
import org.cricketmsf.annotation.PortEventClassHook;
import org.cricketmsf.event.EventMaster;
import org.cricketmsf.event.HttpEvent;
import org.cricketmsf.exception.EventException;
import org.cricketmsf.exception.InitException;
import org.cricketmsf.in.http.HtmlGenAdapterIface;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.ParameterMapResult;
import org.cricketmsf.in.http.Result;
import org.cricketmsf.in.http.StandardResult;
import org.cricketmsf.in.openapi.OpenApiIface;
import org.cricketmsf.in.scheduler.SchedulerIface;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.KeyValueDBIface;
import org.cricketmsf.out.file.FileReaderAdapterIface;
import org.cricketmsf.out.log.LoggerAdapterIface;

/**
 * EchoService
 *
 * @author greg
 */
public class BasicService extends Kernel {

    // adapterClasses
    LoggerAdapterIface logAdapter = null;
    KeyValueDBIface cacheDB = null;
    SchedulerIface scheduler = null;
    HtmlGenAdapterIface htmlAdapter = null;
    FileReaderAdapterIface wwwFileReader = null;
    OpenApiIface apiGenerator = null;

    public BasicService() {
        super();
        this.configurationBaseName = "BasicService";
    }

    @Override
    public void getAdapters() {
        // standard Cricket adapters
        logAdapter = (LoggerAdapterIface) getRegistered("Logger");
        cacheDB = (KeyValueDBIface) getRegistered("CacheDB");
        scheduler = (SchedulerIface) getRegistered("Scheduler");
        htmlAdapter = (HtmlGenAdapterIface) getRegistered("WwwService");
        wwwFileReader = (FileReaderAdapterIface) getRegistered("WwwFileReader");
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
        try {
            cacheDB.addTable("webcache", 100, false);
        } catch (NullPointerException|KeyValueDBException e) {
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
        Kernel.getInstance().dispatchEvent(Event.logInfo("BasicService.runOnce()", "executed"));
    }

    /**
     * Process requests from simple web server implementation given by
     * HtmlGenAdapter access web web resources
     *
     * @param event event
     * @return ParameterMapResult with the file content as a byte array
     */
    @HttpAdapterHook(adapterName = "WwwService", requestMethod = "GET")
    public Object doGet(Event event) {
        try {
            RequestObject request = event.getRequest();
            ParameterMapResult result
                    = (ParameterMapResult) wwwFileReader
                            .getFile(request, htmlAdapter.useCache() ? cacheDB : null, "webcache");
            // caching policy 
            result.setMaxAge(120);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @HttpAdapterHook(adapterName = "Test", requestMethod = "*")
    public Object doTest(Event event) {
        try {
            RequestObject request = event.getRequest();
            StandardResult result = new StandardResult();
            result.setData("");
            Kernel.getInstance().dispatchEvent(new Event(this.getName(), "TEST", "", null, ""));
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @HttpAdapterHook(adapterName = "StatusService", requestMethod = "GET")
    public Object handleStatusRequest(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_OK);
        result.setData(reportStatus());
        return result;
    }

    @PortEventClassHook(className = "HttpEvent", procedureName = "handle")
    public Object doGetEcho(HttpEvent requestEvent) {
        return sendEcho(requestEvent.getOriginalEvent().getRequest());
    }

    @PortEventClassHook(className = "HttpEvent", procedureName = "greet")
    public Object doGreet(HttpEvent requestEvent) {
        String name = (String) requestEvent.getOriginalEvent().getPayload();
        Result result = new StandardResult("Hello " + name);
        result.setHeader("Content-type", "text/plain");
        return result;
    }

    @EventHook(eventCategory = Event.CATEGORY_LOG)
    @EventHook(eventCategory = "Category-Test")
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
            Kernel.getInstance().dispatchEvent(Event.logWarning("Event category " + event.getCategory() + " is not handled by BasicService", event.getPayload() != null ? event.getPayload().toString() : ""));
        }
    }

    public Object sendEcho(RequestObject request) {
        StandardResult r = new StandardResult();
        r.setCode(HttpAdapter.SC_OK);
        //if (!echoAdapter.isSilent()) {
        HashMap<String, Object> data = new HashMap<>(request.parameters);
        data.put("&_service.id", getId());
        data.put("&_service.uuid", getUuid().toString());
        data.put("&_service.name", getName());
        data.put("&_request.method", request.method);
        data.put("&_request.pathExt", request.pathExt);
        data.put("&_request.body", request.body);
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
        return r;
    }

}
