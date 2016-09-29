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

import java.io.File;
import java.util.Date;
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
import org.cricketmsf.out.db.KeyValueCacheAdapterIface;
import org.cricketmsf.out.file.FileReaderAdapterIface;
import org.cricketmsf.in.http.ParameterMapResult;
import org.cricketmsf.in.monitor.EnvironmentMonitorIface;
import org.cricketmsf.out.file.FileObject;

/**
 * EchoService
 *
 * @author greg
 */
public class EchoService extends Kernel {

    // adapterClasses
    LoggerAdapterIface logAdapter = null;
    EchoHttpAdapterIface httpAdapter = null;
    KeyValueCacheAdapterIface cache = null;
    SchedulerIface scheduler = null;
    HtmlGenAdapterIface htmlAdapter = null;
    FileReaderAdapterIface fileReader = null;
    EchoHttpAdapterIface schedulerTester = null;
    HttpAdapterIface scriptTester = null;
    EnvironmentMonitorIface envMonitor = null;

    @Override
    public void getAdapters() {
        logAdapter = (LoggerAdapterIface) getRegistered("LoggerAdapterIface");
        httpAdapter = (EchoHttpAdapterIface) getRegistered("EchoAdapter");
        cache = (KeyValueCacheAdapterIface) getRegistered("KeyValueCacheAdapterIface");
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
        handleEvent(Event.logInfo("EchoService.runOnce()", "executed"));
        Event e = new Event("EchoService.runOnce()", "beep", "", "+5s", "I'm event from runOnce() processed by scheduler. Hello!");
        processEvent(e);
    }

    @Override
    public void runInitTasks() {
    }

    @Override
    public void runFinalTasks() {
    }

    @EventHook(eventCategory = "*")
    public void processEvent(Event event) {
        if (event.getTimePoint() != null) {
            scheduler.handleEvent(event);
        } else {
            handleEvent(Event.logInfo("EchoService", event.getPayload().toString()));
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
    public Object doGet(Event event) {
        boolean useCache = htmlAdapter.useCache();
        //RequestObject request = (RequestObject) event.getPayload();
        RequestObject request = event.getRequest();
        String filePath = fileReader.getFilePath(request);

        ParameterMapResult result = new ParameterMapResult();
        result.setData(request.parameters);

        // we can use cache if available
        FileObject fo = null;
        if (useCache && cache.containsKey(filePath)) {
            try {
                fo = (FileObject) cache.get(filePath);
            } catch (ClassCastException e) {
                fo = null;
            }
        }
        if (fo == null) {
            File file = new File(filePath);
            byte[] content = fileReader.getFileBytes(file, filePath);
            //if (content.length > 0) {
                fo = new FileObject();
                fo.content = content;
                fo.modified = new Date(file.lastModified());
                fo.filePath = filePath;
                fo.fileExtension = fileReader.getFileExt(filePath);
                if(useCache) {
                    cache.put(filePath, fo);
                }
            //}
        }else{
            handleEvent(Event.logInfo("cache", "readed from cache"));
        }
        // f==null means file not found (sure - it's a shortcut)
        if (fo.content.length > 0) {
            result.setCode(HttpAdapter.SC_OK);
            result.setMessage("");
            result.setPayload(fo.content);
            result.setFileExtension(fo.fileExtension);
            result.setModificationDate(fo.modified);
        } else {
            result.setCode(HttpAdapter.SC_NOT_FOUND);
            result.setMessage("file not found");
            result.setPayload(fo.content);
            result.setFileExtension(null);
        }

        return result;
    }

    @HttpAdapterHook(adapterName = "TestScheduler", requestMethod = "GET")
    public Object scheduleEvent(Event requestEvent) {
        Event e = new Event("EchoService.runOnce()", "beep", "", "+5s", "I'm event from doGetEcho() processed by scheduler. Hello!");
        processEvent(e);
        return sendEcho(requestEvent.getRequest());
    }

    /**
     * Handling request from EchoAdapter.
     * The method is linked to the adapter by using @HttpAdapterHook annotation.
     * Other annotations are for the API documentation only and has no result in the 
     * method or the service logic.
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
    
    @InboundAdapterHook(adapterName = "EnvironmentAdapter", inputMethod="*")
    public Object processMonitoringEvent(Event event){
        handleEvent(Event.logInfo(this.getClass().getSimpleName(), (String)event.getPayload()));
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
            Long counter;
            counter = (Long) cache.get("counter", new Long(0));
            counter++;
            cache.put("counter", counter);
            HashMap<String, Object> data = new HashMap<>(request.parameters);
            data.put("service.uuid", getUuid().toString());
            data.put("request.method", request.method);
            data.put("request.pathExt", request.pathExt);
            data.put("echo.counter", cache.get("counter"));
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
