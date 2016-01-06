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
import com.gskorupa.cricket.Event;
import com.gskorupa.cricket.EventHook;
import com.gskorupa.cricket.Kernel;
import com.gskorupa.cricket.in.EchoHttpAdapterIface;
import com.gskorupa.cricket.out.LoggerAdapterIface;
import java.util.logging.Logger;

/**
 * SimpleService
 *
 * @author greg
 */
public class BookshelfService extends Kernel {

    // emergency logger
    private static final Logger logger = Logger.getLogger(com.mycompany.bookshelf.BookshelfService.class.getName());

    // adapters
    LoggerAdapterIface logAdapter = null;
    EchoHttpAdapterIface httpAdapter = null;

    public BookshelfService() {

        adapters = new Object[2];
        adapters[0] = logAdapter;
        adapters[1] = httpAdapter;
        adapterClasses = new Class[2];
        adapterClasses[0] = LoggerAdapterIface.class;
        adapterClasses[1] = EchoHttpAdapterIface.class;
    }

    @Override
    public void getAdapters() {
        httpAdapter = (EchoHttpAdapterIface) super.adapters[1];
        logAdapter = (LoggerAdapterIface) super.adapters[0];
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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        final BookshelfService service;
        ArgumentParser arguments = new ArgumentParser(args);
        if (arguments.isProblem()) {
            if (arguments.containsKey("error")) {
                System.out.println(arguments.get("error"));
            }
            System.out.println(new BookshelfService().getHelp());
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
                service.start();
            } else {
                service.runOnce();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
