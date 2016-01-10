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
package com.example.service;

import com.gskorupa.cricket.ArgumentParser;
import com.gskorupa.cricket.Event;
import com.gskorupa.cricket.EventHook;
import com.gskorupa.cricket.HttpAdapterHook;
import com.gskorupa.cricket.Kernel;
import com.gskorupa.cricket.RequestObject;
import com.gskorupa.cricket.in.EchoHttpAdapterIface;
import com.gskorupa.cricket.in.HtmlGenAdapterIface;
import com.gskorupa.cricket.in.HttpAdapter;
import com.gskorupa.cricket.in.ParameterMapResult;
import com.gskorupa.cricket.out.HtmlReaderAdapterIface;
import com.gskorupa.cricket.out.LoggerAdapterIface;
import java.util.HashMap;
import java.util.Map;

/**
 * SimpleService
 *
 * @author greg
 */
public class BasicService extends Kernel {

    // adapters
    LoggerAdapterIface logAdapter = null;
    EchoHttpAdapterIface echoAdapter = null;
    HtmlGenAdapterIface htmlAdapter = null;
    HtmlReaderAdapterIface htmlReaderAdapter = null;

    public BasicService() {

        adapters = new Object[4];
        adapters[0] = logAdapter;
        adapters[1] = echoAdapter;
        adapters[2] = htmlAdapter;
        adapters[3] = htmlReaderAdapter;
        adapterClasses = new Class[4];
        adapterClasses[0] = LoggerAdapterIface.class;
        adapterClasses[1] = EchoHttpAdapterIface.class;
        adapterClasses[2] = HtmlGenAdapterIface.class;
        adapterClasses[3] = HtmlReaderAdapterIface.class;
    }

    @Override
    public void getAdapters() {
        logAdapter = (LoggerAdapterIface) super.adapters[0];
        echoAdapter = (EchoHttpAdapterIface) super.adapters[1];
        htmlAdapter = (HtmlGenAdapterIface) super.adapters[2];
        htmlReaderAdapter = (HtmlReaderAdapterIface) super.adapters[3];
    }

    @Override
    public void runOnce() {
        //write to logs
        Event ev= new Event(
                        this.getClass().getSimpleName(),
                        Event.CATEGORY_LOG, // equals "LOG"
                        Event.LOG_INFO,     // equals "INFO"
                        null);
        logEvent(ev);
        //alternatively:
        //logAdapter.log(ev);
        System.out.println("Hi! I'm " + this.getClass().getSimpleName());
    }

    @EventHook(eventCategory = "LOG")
    public void logEvent(com.gskorupa.cricket.Event event) {
        logAdapter.log(event);
    }

    @EventHook(eventCategory = "*")
    public void processEvent(com.gskorupa.cricket.Event event) {
        //put your code here
    }
    
    @HttpAdapterHook(handlerClassName = "HtmlGenAdapterIface", requestMethod = "GET")
    public Object doGet(RequestObject request) {
        String payload = "";
        ParameterMapResult result = new ParameterMapResult();
        try {
            payload = htmlReaderAdapter.readFile(request.pathExt);
            result.setCode(HttpAdapter.SC_OK);
            result.setMessage("");
        } catch (Exception e) {
            result.setCode(HttpAdapter.SC_NOT_FOUND);
            result.setMessage("file not found");
        }
        
        //copy parameters from request
        HashMap<String, String> data = setResponseParameters(request.parameters);
        //
        result.setData(data);
        result.setPayload(payload);
        return result;
    }
    
    private HashMap<String, String> setResponseParameters(Map<String, Object> requestParameters){
        HashMap<String, String> data=new HashMap();
        for (Map.Entry<String, Object> entry : requestParameters.entrySet()) {
            data.put(entry.getKey(), (String)entry.getValue());
        }
        return data;
    }
    
    @HttpAdapterHook(handlerClassName = "EchoHttpAdapterIface", requestMethod = "GET")
    public Object processEchoRequest(RequestObject request){
        // rewrite parameters from request to response
        ParameterMapResult responseObect = new ParameterMapResult();
        HashMap<String, String> data=new HashMap();
        Map<String, Object> map = request.parameters;
        data.put("request.method",request.method);
        data.put("request.pathExt",request.pathExt);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            data.put(entry.getKey(), (String)entry.getValue());
        }
        if (data.containsKey("error")){
            responseObect.setCode(HttpAdapter.SC_INTERNAL_SERVER_ERROR);
            data.put("error", "error forced by request");
        } else {
            responseObect.setCode(HttpAdapter.SC_OK);
        }
        responseObect.setData(data);
        return responseObect;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        final BasicService service;
        ArgumentParser arguments = new ArgumentParser(args);
        if (arguments.isProblem()) {
            if (arguments.containsKey("error")) {
                System.out.println(arguments.get("error"));
            }
            System.out.println(new BasicService().getHelp());
            System.exit(-1);
        }
        try {
            if (arguments.containsKey("config")) {
                service = (BasicService) BasicService.getInstance(BasicService.class, arguments.get("config"));
            } else {
                service = (BasicService) BasicService.getInstanceUsingResources(BasicService.class);
            }
            service.getAdapters();

            if (arguments.containsKey("run")) {
                service.start();
            } else {
                service.runOnce();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
