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
import org.cricketmsf.event.EventMaster;
import org.cricketmsf.exception.EventException;
import org.cricketmsf.in.http.HtmlGenAdapterIface;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.ParameterMapResult;
import org.cricketmsf.in.http.StandardResult;
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
    //EchoHttpAdapterIface echoAdapter = null;
    KeyValueDBIface cacheDB = null;
    SchedulerIface scheduler = null;
    HtmlGenAdapterIface htmlAdapter = null;
    FileReaderAdapterIface wwwFileReader = null;
    //SubscriberIface queueSubscriber = null;
    // optional
    //HttpAdapterIface scriptingService = null;
    //ScriptingAdapterIface scriptingEngine = null;

    public BasicService() {
        super();
        this.configurationBaseName = "BasicService";
    }

    @Override
    public void getAdapters() {
        // standard Cricket adapters
        logAdapter = (LoggerAdapterIface) getRegistered("Logger");
        //echoAdapter = (EchoHttpAdapterIface) getRegistered("Echo");
        cacheDB = (KeyValueDBIface) getRegistered("CacheDB");
        scheduler = (SchedulerIface) getRegistered("Scheduler");
        htmlAdapter = (HtmlGenAdapterIface) getRegistered("WwwService");
        wwwFileReader = (FileReaderAdapterIface) getRegistered("WwwFileReader");
        //queueSubscriber = (SubscriberIface) getRegistered("QueueSubscriber");
        // optional
        //scriptingService = (HttpAdapterIface) getRegistered("ScriptingService");
        //scriptingEngine = (ScriptingAdapterIface) getRegistered("ScriptingEngine");
    }

    @Override
    public void runInitTasks() {
        // we should register event categories used by this service
        try {
            EventMaster.registerEventCategories(new Event().getCategories(), Event.class.getName());
        } catch (EventException ex) {
            ex.printStackTrace();
            shutdown();
        }
        /*
        try {
            if(null!=queueSubscriber){
                queueSubscriber.init();
            }
        } catch (QueueException ex) {
        }
         */
        try {
            cacheDB.addTable("webcache", 100, false);
        } catch (KeyValueDBException e) {
        }
        /*
        try {
            cacheDB.addTable("counters", 1, false);
        } catch (KeyValueDBException e) {
        }
         */

    }

    @Override
    public void runFinalTasks() {
        //System.out.println(printStatus());
    }

    @Override
    public void runOnce() {
        super.runOnce();
        Kernel.getInstance().dispatchEvent(Event.logInfo("BasicService.runOnce()", "executed"));
    }

    /*
    @HttpAdapterHook(adapterName = "ScriptingService", requestMethod = "*")
    public Object doGetScript(Event requestEvent) {
        StandardResult r = scriptingEngine.processRequest(requestEvent.getRequest());
        return r;
    }
     */
    /**
     * Process requests from simple web server implementation given by
     * HtmlGenAdapter access web web resources
     *
     * @param event
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

    @HttpAdapterHook(adapterName = "Echo", requestMethod = "*")
    public Object doGetEcho(Event requestEvent) {
        return sendEcho(requestEvent.getRequest());
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
        //} else {
        //    Kernel.getInstance().dispatchEvent(Event.logFine("BasicService", "echo service is silent"));
        //}
        return r;
    }

}
