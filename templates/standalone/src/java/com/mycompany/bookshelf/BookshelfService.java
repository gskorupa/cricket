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
package com.mycompany.bookshelf;

import com.gskorupa.cricket.ArgumentParser;
import com.gskorupa.cricket.Httpd;
import java.util.logging.Logger;
import com.gskorupa.cricket.Service;
import com.gskorupa.cricket.example.SimpleData;
import com.gskorupa.cricket.example.SimpleResult;
import static java.lang.Thread.MIN_PRIORITY;
import java.util.Map;

/**
 * SimpleService
 *
 * @author greg
 */
public class BookshelfService extends Service {

    // emergency logger
    private static final Logger logger = Logger.getLogger(com.gskorupa.cricket.example.SimpleService.class.getName());

    // adapters
    //SimpleStorageIface storage = null;
    //SimpleLoggerIface log = null;
    //SimpleHttpAdapterIface handler = null;

    public BookshelfService() {

        fields = new Object[2];
        //fields[0] = storage;
        //fields[1] = log;
        //fields[2] = handler;
        //adapters = new Class[3];
        //adapters[0] = SimpleStorageIface.class;
        //adapters[1] = SimpleLoggerIface.class;
        //adapters[2] = SimpleHttpAdapterIface.class;

    }

    public void getAdapters() {
        //storage = (SimpleStorageIface) super.fields[0];
        //log = (SimpleLoggerIface) super.fields[1];
        //handler = (SimpleHttpAdapterIface) super.fields[2];
    }

    //
    public String sayHello() {
        return "Hi! I'm "+ this.getClass().getSimpleName();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        final BookshelfService service;
        Map<String, String> arguments = ArgumentParser.getArguments(args);

        if (arguments.containsKey("error")) {
            System.out.println(arguments.get("error"));
            System.exit(-1);
        }
        if (arguments.containsKey("help")) {
            BookshelfService s=new BookshelfService(); //creating instance this way is valid only for displaing help!
            System.out.println(s.getHelp());
            System.exit(-1);
        }

        try {
            
            if (arguments.containsKey("config")) {
                service = (BookshelfService) BookshelfService.getInstance(BookshelfService.class, arguments.get("config"));
            } else {
                service = (BookshelfService) BookshelfService.getInstanceUsingResources(BookshelfService.class);    
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
                // say hello
                System.out.println(service.sayHello());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
