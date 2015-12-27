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

import com.gskorupa.cricket.example.SimpleService;
import java.util.logging.Logger;
import static java.lang.Thread.MIN_PRIORITY;
import java.util.Map;

/**
 * SimpleService
 *
 * @author greg
 */
public class Runner {

    // emergency logger
    private static final Logger logger = Logger.getLogger(com.gskorupa.cricket.Runner.class.getName());

    public void start(String[] args, String callingService) {

        String serviceClassName = null;
        final Service service;
        Map<String, String> arguments = ArgumentParser.getArguments(args);

        if (arguments.containsKey("error")) {
            System.out.println(arguments.get("error"));
            System.exit(-1);
        }
        if (arguments.containsKey("help")) {
            SimpleService s = new SimpleService(); //creating instance this way is valid only for displaing help!
            System.out.println(s.getHelp());
            System.exit(-1);
        }
        if (arguments.containsKey("run")) {
            serviceClassName = arguments.get("run");
            if (serviceClassName.equalsIgnoreCase("*")) {
                System.out.println("Running all or default services (*) is not possible");
                System.exit(-1);
            }
        }

        try {

            if (null == serviceClassName) {
                serviceClassName = callingService;
            }
            
            try {
                Class theClass = Class.forName(serviceClassName);
                service = (Service) theClass.newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                ex.printStackTrace();
                System.exit(-1);
            }

            if (arguments.containsKey("config")) {
                //service = (SimpleService) SimpleService.getInstance(SimpleService.class, arguments.get("config"));
            } else {
                //service = (SimpleService) SimpleService.getInstanceUsingResources(SimpleService.class);
            }
            
            
            service.getAdapters();

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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
