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
import org.cricketmsf.EventHook;
import org.cricketmsf.HttpAdapterHook;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.ParameterMapResult;
import org.cricketmsf.out.log.LoggerAdapterIface;
import java.util.HashMap;
import java.util.Map;
import org.cricketmsf.in.http.EchoHttpAdapterIface;
import org.cricketmsf.in.http.FileResult;
import org.cricketmsf.in.http.HtmlGenAdapterIface;
import org.cricketmsf.in.http.Result;
import org.cricketmsf.in.scheduler.SchedulerIface;
import org.cricketmsf.out.html.HtmlReaderAdapterIface;
import org.cricketmsf.out.db.KeyValueCacheAdapterIface;

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
    HtmlReaderAdapterIface htmlReader = null;

    @Override
    public void getAdapters() {
        logAdapter = (LoggerAdapterIface) getRegistered("LoggerAdapterIface");
        httpAdapter = (EchoHttpAdapterIface) getRegistered("EchoHttpAdapterIface");
        cache = (KeyValueCacheAdapterIface) getRegistered("KeyValueCacheAdapterIface");
        scheduler = (SchedulerIface) getRegistered("SchedulerIface");
        htmlAdapter = (HtmlGenAdapterIface) getRegistered("HtmlGenAdapterIface");
        htmlReader = (HtmlReaderAdapterIface) getRegistered("HtmlReaderAdapterIface");
    }

    @Override
    public void runOnce() {
        super.runOnce();
        Event e = new Event("EchoService.runOnce()", "LOG", Event.LOG_INFO, null, "executed");
        logEvent(e);
        System.out.println("Hello from EchoService.runOnce()");
        e = new Event("EchoService.runOnce()", "beep", "", "+5s", "I'm event from runOnce() processed by scheduler. Hello!");
        processEvent(e);
    }

    @HttpAdapterHook(handlerClassName = "HtmlGenAdapterIface", requestMethod = "GET")
    public Object doGet(Event event) {
        RequestObject request = (RequestObject) event.getPayload();
        Result result = getFile(request);
        HashMap<String, String> data = new HashMap();
        //copy parameters from request to response data without modification
        Map<String, Object> map = request.parameters;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            data.put(entry.getKey(), (String) entry.getValue());
        }
        result.setData(data);
        return result;
    }

    @HttpAdapterHook(handlerClassName = "EchoHttpAdapterIface", requestMethod = "GET")
    public Object doGetEcho(Event requestEvent) {
        Event e = new Event("EchoService.runOnce()", "beep", "", "+5s", "I'm event from doGetEcho() processed by scheduler. Hello!");
        processEvent(e);
        return sendEcho((RequestObject) requestEvent.getPayload());
    }

    @HttpAdapterHook(handlerClassName = "EchoHttpAdapterIface", requestMethod = "POST")
    public Object doPost(Event requestEvent) {
        return sendEcho((RequestObject) requestEvent.getPayload());
    }

    @HttpAdapterHook(handlerClassName = "EchoHttpAdapterIface", requestMethod = "PUT")
    public Object doPut(Event requestEvent) {
        return sendEcho((RequestObject) requestEvent.getPayload());
    }

    @HttpAdapterHook(handlerClassName = "EchoHttpAdapterIface", requestMethod = "DELETE")
    public Object doDelete(Event requestEvent) {
        return sendEcho((RequestObject) requestEvent.getPayload());
    }

    @EventHook(eventCategory = "LOG")
    public void logEvent(Event event) {
        logAdapter.log(event);
    }

    @EventHook(eventCategory = "*")
    public void processEvent(Event event) {
        if (event.getTimePoint() != null) {
            scheduler.handleEvent(event);
        } else {
            System.out.println(event.getPayload().toString());
        }
    }

    public Object sendEcho(RequestObject request) {

        //
        Long counter;
        counter = (Long) cache.get("counter", new Long(0));
        counter++;
        cache.put("counter", counter);

        ParameterMapResult r = new ParameterMapResult();
        HashMap<String, Object> data = new HashMap();
        Map<String, Object> map = request.parameters;
        data.put("service.uuid", getUuid().toString());
        data.put("request.method", request.method);
        data.put("request.pathExt", request.pathExt);
        data.put("echo.counter", cache.get("counter"));
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            //System.out.println(entry.getKey() + "=" + entry.getValue());
            data.put(entry.getKey(), (String) entry.getValue());
        }
        if (data.containsKey("error")) {
            int errCode = HttpAdapter.SC_INTERNAL_SERVER_ERROR;
            try {
                errCode = Integer.parseInt((String) data.get("error"));
            } catch (Exception e) {
            }
            r.setCode(errCode);
            data.put("error", "error forced by request");
        } else {
            r.setCode(HttpAdapter.SC_OK);
        }
        r.setData(data);
        return r;
    }

    private Result getFile(RequestObject request) {
        logEvent(new Event("EchoService", Event.CATEGORY_LOG, Event.LOG_FINEST, "", "STEP1"));
        byte[] fileContent = {};
        String filePath = request.pathExt;
        logEvent(new Event("EchoService", Event.CATEGORY_LOG, Event.LOG_FINEST, "", "pathExt=" + filePath));
        String fileExt = "";
        if (!(filePath.isEmpty() || filePath.endsWith("/")) && filePath.indexOf(".") > 0) {
            fileExt = filePath.substring(filePath.lastIndexOf("."));
        }
        Result result;
        switch (fileExt.toLowerCase()) {
            case ".jpg":
            case ".jpeg":
            case ".gif":
            case ".png":
                result = new FileResult();
                break;
            default:
                fileExt = ".html";
                result = new ParameterMapResult();
        }
        try {
            byte[] b = htmlReader.readFile(filePath);
            result.setPayload(b);
            result.setFileExtension(fileExt);
            result.setCode(HttpAdapter.SC_OK);
            result.setMessage("");
        } catch (Exception e) {
            logEvent(new Event("EchoService", Event.CATEGORY_LOG, Event.LOG_WARNING, "", e.getMessage()));
            result.setPayload(fileContent);
            result.setFileExtension(fileExt);
            result.setCode(HttpAdapter.SC_NOT_FOUND);
            result.setMessage("file not found");
        }
        return result;
    }

}
