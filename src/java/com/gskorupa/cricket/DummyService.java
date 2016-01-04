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
package com.gskorupa.cricket;

import com.gskorupa.cricket.in.HttpAdapter;
import java.util.logging.Logger;
import com.gskorupa.cricket.in.EchoHttpAdapterIface;
import com.gskorupa.cricket.in.EchoResult;
import com.gskorupa.cricket.out.LoggerAdapterIface;
import static java.lang.Thread.MIN_PRIORITY;
import java.util.HashMap;
import java.util.Map;

/**
 * DummyService
 *
 * @author greg
 */
public class DummyService extends Kernel {

    // emergency logger
    private static final Logger logger = Logger.getLogger(com.gskorupa.cricket.DummyService.class.getName());

    // adapterClasses
    LoggerAdapterIface logHandler = null;
    EchoHttpAdapterIface httpHandler = null;

    public DummyService() {
        adapters = new Object[2];
        adapters[0] = logHandler;
        adapters[1] = httpHandler;
        adapterClasses = new Class[2];
        adapterClasses[0] = LoggerAdapterIface.class;
        adapterClasses[1] = EchoHttpAdapterIface.class;
    }

    @Override
    public void getAdapters() {
        logHandler = (LoggerAdapterIface) super.adapters[0];
        httpHandler = (EchoHttpAdapterIface) super.adapters[1];
    }

    @Override
    public void runOnce() {
        Event e=new Event("runOnce()","LOG",Event.LOG_INFO, "executed");
        logEvent(e);
        System.out.println("Hello from DummyService.runOnce()");
    }

    @HttpAdapterHook(handlerClassName = "EchoHttpAdapterIface", requestMethod = "GET")
    public Object doGet(RequestObject request) {
        return sendEcho(request);
    }
    
    @HttpAdapterHook(handlerClassName = "EchoHttpAdapterIface", requestMethod = "POST")
    public Object doPost(RequestObject request) {
        return sendEcho(request);
    }
    
    @HttpAdapterHook(handlerClassName = "EchoHttpAdapterIface", requestMethod = "PUT")
    public Object doPut(RequestObject request) {
        return sendEcho(request);
    }
    
    @HttpAdapterHook(handlerClassName = "EchoHttpAdapterIface", requestMethod = "DELETE")
    public Object doDelete(RequestObject request) {
        return sendEcho(request);
    }
    
    @EventHook(eventCategory = "LOGGING")
    public void logEvent(Event event){
        logHandler.log(event);
    }
    
    @EventHook(eventCategory = "*")
    public void processEvent(Event event){
        //does nothing
    }
    
    public Object sendEcho(RequestObject request) {
        EchoResult r = new EchoResult();
        HashMap<String, String> data=new HashMap();
        Map<String, Object> map = request.parameters;
        data.put("request.method",request.method);
        data.put("request.pathExt",request.pathExt);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            //System.out.println(entry.getKey() + "=" + entry.getValue());
            data.put(entry.getKey(), (String)entry.getValue());
        }
        if (data.containsKey("error")){
            r.setCode(HttpAdapter.SC_INTERNAL_SERVER_ERROR);
            data.put("error", "error forced by request");
        } else {
            r.setCode(HttpAdapter.SC_OK);
        }
        r.setData(data);
        return r;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        final DummyService service;

        ArgumentParser arguments = new ArgumentParser(args);
        if (arguments.isProblem()) {
            if (arguments.containsKey("error")) {
                System.out.println(arguments.get("error"));
            }
            System.out.println(new DummyService().getHelp());
            System.exit(-1);
        }

        try {

            if (arguments.containsKey("config")) {
                service = (DummyService) DummyService.getInstance(DummyService.class, arguments.get("config"));
            } else {
                service = (DummyService) DummyService.getInstanceUsingResources(DummyService.class);
            }
            service.getAdapters();

            if (arguments.containsKey("run")) {
                if (service.isHttpHandlerLoaded()) {
                    System.out.println("Starting http server ...");
                    Runtime.getRuntime().addShutdownHook(
                            new Thread() {
                        public void run() {
                            try {
                                Thread.sleep(200);
                                //some cleaning up code could be added here ... if required
                                System.out.println("\nShutdown ...");
                                service.getHttpd().server.stop(MIN_PRIORITY);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    service.setHttpd(new Httpd(service));
                    service.getHttpd().run();
                    System.out.println("Started. Press Ctrl-C to stop");
                    while (true) {
                        Thread.sleep(100);
                    }
                } else {
                    System.out.println("Couldn't find any http request hook method. Exiting ...");
                    System.exit(MIN_PRIORITY);
                }
            } else {
                System.out.println("Executing runOnce method");
                service.runOnce();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
