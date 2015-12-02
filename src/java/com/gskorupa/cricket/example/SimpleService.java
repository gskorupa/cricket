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
import java.util.logging.Logger;
import com.gskorupa.cricket.Httpd;
import com.gskorupa.cricket.RequestObject;
import com.gskorupa.cricket.RequestParameter;
import com.gskorupa.cricket.Service;

/**
 * SimpleService
 *
 * @author greg
 */
public class SimpleService extends Service {

    // emergency logger
    private static final Logger logger = Logger.getLogger(com.gskorupa.cricket.example.SimpleService.class.getName());

    // adapters
    SimpleStorage storage = null;
    SimpleLogger log = null;
    SimpleHttpAdapter handler = null;

    public SimpleService() {

        fields = new Object[3];
        fields[0] = storage;
        fields[1] = log;
        fields[2] = handler;
        adapters = new Class[3];
        adapters[0] = SimpleStorage.class;
        adapters[1] = SimpleLogger.class;
        adapters[2] = SimpleHttpAdapter.class;
        //super.fields=fields;
        //super.adapters=adapters;

    }

    public void getAdapters() {
        storage = (SimpleStorage) super.fields[0];
        log = (SimpleLogger) super.fields[1];
        handler = (SimpleHttpAdapter) super.fields[2];
    }

    /*
    // abstract Service.getInstance ?
    @Override
    public SimpleService getInstance(){
        return (SimpleService)super.getInstance();
    }
    
    // abstract ... ?
    @Override
    public void setInstance(Object instance){
        super.setInstance((SimpleService)instance);
    }
     */
    public SimpleResult getData() {
        storage.storeData();
        SimpleResult r = new SimpleResult();
        r.setCode(0);
        r.setData(new SimpleData());
        return r;
    }

    //TODO: jak sprawdzić na poziomie builda, że mamy zdublowane kody błedów
    public SimpleResult doSomething(String parameter) {
        log.log("INFO", 0, this, "hello from main");
        SimpleData data = new SimpleData();
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
    
    @AdapterHook(handlerClassName="SimpleHttpAdapter")
    public Object sayHello(RequestObject request){
        String name="";
        for(RequestParameter p: request.parameters){
            if(p.name.equals("name")){
                name=p.value;
            }
        }
        return "Hello "+name+" from the service hook method";
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

            SimpleResult r = service.doSomething("hello");
            System.out.println(((SimpleData) r.getData()).getParam1());

            if (service.isHttpHandlerLoaded()) {
                System.out.println("Starting http server ...");
                Runtime.getRuntime().addShutdownHook(
                        new Thread() {

                    public void run() {
                        try {
                            Thread.sleep(200);
                            //some cleaning up code...
                            System.out.println("\nShutdown ...");
                            service.getHttpd().server.stop(MIN_PRIORITY);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
