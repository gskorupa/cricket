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

import com.gskorupa.cricket.AdapterHook;
import com.gskorupa.cricket.HttpAdapter;
import com.gskorupa.cricket.Httpd;
import java.util.logging.Logger;
import com.gskorupa.cricket.RequestObject;
import com.gskorupa.cricket.Service;
import java.util.Map;

/**
 * SimpleService
 *
 * @author greg
 */
public class SimpleService extends Service {

    // emergency logger
    private static final Logger logger = Logger.getLogger(com.gskorupa.cricket.example.SimpleService.class.getName());

    // adapters
    SimpleStorageIface storage = null;
    SimpleLoggerIface log = null;
    SimpleHttpAdapterIface handler = null;

    public SimpleService() {

        fields = new Object[3];
        fields[0] = storage;
        fields[1] = log;
        fields[2] = handler;
        adapters = new Class[3];
        adapters[0] = SimpleStorageIface.class;
        adapters[1] = SimpleLoggerIface.class;
        adapters[2] = SimpleHttpAdapterIface.class;

    }

    public void getAdapters() {
        storage = (SimpleStorageIface) super.fields[0];
        log = (SimpleLoggerIface) super.fields[1];
        handler = (SimpleHttpAdapterIface) super.fields[2];
    }

    public SimpleResult getData() {
        storage.storeData();
        SimpleResult r = new SimpleResult();
        r.setCode(0);
        r.setData(new SimpleData("", ""));
        return r;
    }

    //TODO: jak sprawdzić na poziomie builda, że mamy zdublowane kody błedów
    public SimpleResult doSomething(String parameter) {
        log.log("INFO", 0, this, "hello from main");
        SimpleData data = new SimpleData("", "");
        SimpleResult r = new SimpleResult();
        data.setParam1(parameter);
        if (false) {
            r.setCode(-1);
        } else {
            r.setCode(0);
        }
        r.setData(data);
        return r;
    }

    @AdapterHook(handlerClassName = "SimpleHttpAdapterIface")
    public Object sayHello(RequestObject request) {
        String name = "";
        String surname = "";
        System.out.println(this.getClass().getName());
        System.out.println(request.method);
        System.out.println(request.pathExt);
        Map<String, Object> map = request.parameters;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
            if (entry.getKey().equalsIgnoreCase("name")) {
                name = (String) entry.getValue();
            }
            if (entry.getKey().equalsIgnoreCase("surname")) {
                surname = (String) entry.getValue();
            }
        }
        SimpleResult r = new SimpleResult();
        if ("error".equalsIgnoreCase(surname)) {
            r.setCode(500);
            r.setData(new SimpleData("error", "error forced by request"));
        } else {
            r.setCode(0);
            r.setData(new SimpleData(name, surname));
        }
        //"Hello "+name+" from the service hook method";
        return r;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            final SimpleService service;
            if (args.length > 0) {
                service = (SimpleService) SimpleService.getInstance(SimpleService.class, args[0]);
                service.getAdapters();
            } else {
                service = (SimpleService) SimpleService.getInstanceUsingResources(SimpleService.class);
                service.getAdapters();
            }   
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
                SimpleResult r = service.doSomething("hello");
                System.out.println(((SimpleData) r.getData()).getParam1());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
