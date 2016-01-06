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
package com.gskorupa.cricket.example;

import com.gskorupa.cricket.ArgumentParser;
import com.gskorupa.cricket.in.HttpAdapter;
import com.gskorupa.cricket.Httpd;
import java.util.logging.Logger;
import com.gskorupa.cricket.RequestObject;
import com.gskorupa.cricket.Kernel;
import static java.lang.Thread.MIN_PRIORITY;
import java.util.Map;
import com.gskorupa.cricket.HttpAdapterHook;

/**
 * HelloService
 *
 * @author greg
 */
public class HelloService extends Kernel {

    // emergency logger
    private static final Logger logger = Logger.getLogger(com.gskorupa.cricket.example.HelloService.class.getName());

    // adapterClasses
    HelloStorageIface storage = null;
    HelloLoggerIface log = null;
    HelloHttpAdapterIface handler = null;

    public HelloService() {

        adapters = new Object[3];
        adapters[0] = storage;
        adapters[1] = log;
        adapters[2] = handler;
        adapterClasses = new Class[3];
        adapterClasses[0] = HelloStorageIface.class;
        adapterClasses[1] = HelloLoggerIface.class;
        adapterClasses[2] = HelloHttpAdapterIface.class;

    }

    @Override
    public void getAdapters() {
        storage = (HelloStorageIface) super.adapters[0];
        log = (HelloLoggerIface) super.adapters[1];
        handler = (HelloHttpAdapterIface) super.adapters[2];
    }

    @Override
    public void runOnce() {
        System.out.println("Hello from HelloService.runOnce()");
    }

    public HelloResult getData() {
        storage.storeData();
        HelloResult r = new HelloResult();
        r.setCode(0);
        r.setData(new HelloData());
        return r;
    }

    @HttpAdapterHook(handlerClassName = "HelloHttpAdapterIface", requestMethod = "GET")
    public Object doGet(RequestObject request) {
        return sendEcho(request);
    }
    
    @HttpAdapterHook(handlerClassName = "HelloHttpAdapterIface", requestMethod = "POST")
    public Object doPost(RequestObject request) {
        return sendEcho(request);
    }
    
    @HttpAdapterHook(handlerClassName = "HelloHttpAdapterIface", requestMethod = "PUT")
    public Object doPut(RequestObject request) {
        return sendEcho(request);
    }
    
    @HttpAdapterHook(handlerClassName = "HelloHttpAdapterIface", requestMethod = "DELETE")
    public Object doDelete(RequestObject request) {
        return sendEcho(request);
    }
    
    public Object sendEcho(RequestObject request) {
        HelloResult r = new HelloResult();
        HelloData data=new HelloData();
        Map<String, Object> map = request.parameters;
        data.list.put("request.method",request.method);
        data.list.put("request.pathExt",request.pathExt);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            //System.out.println(entry.getKey() + "=" + entry.getValue());
            data.list.put(entry.getKey(), entry.getValue());
        }
        if (data.list.containsKey("error")){
            r.setCode(HttpAdapter.SC_INTERNAL_SERVER_ERROR);
            data.list.put("error", "error forced by request");
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

        final HelloService service;

        ArgumentParser arguments = new ArgumentParser(args);
        if (arguments.isProblem()) {
            if (arguments.containsKey("error")) {
                System.out.println(arguments.get("error"));
            }
            System.out.println(new HelloService().getHelp());
            System.exit(-1);
        }

        try {

            if (arguments.containsKey("config")) {
                service = (HelloService) HelloService.getInstance(HelloService.class, arguments.get("config"));
            } else {
                service = (HelloService) HelloService.getInstanceUsingResources(HelloService.class);
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
