/*
 * Copyright 2020 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package org.cricketmsf;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.cricketmsf.exception.WebsocketException;
import org.cricketmsf.in.websocket.ClientList;
import org.cricketmsf.in.websocket.WebsocketAdapter;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class WebsocketServer implements Runnable {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(WebsocketServer.class);

    private ServerSocket server = null;
    private boolean running = false;
    private ConcurrentHashMap<String, ClientList> registeredContexts = new ConcurrentHashMap<>();

    public WebsocketServer(Kernel service) {
        String host = service.getHost();
        int backlog = 0;
        try {
            backlog = Integer.parseInt((String) service.getProperties().getOrDefault("wsthreads", "0"));
        } catch (NumberFormatException | ClassCastException e) {
        }
        if (null != host) {
            if (host.isEmpty() || "0.0.0.0".equals(host) || "*".equals(host)) {
                host = null;
            }
        }
        try {
            server = new ServerSocket();
            if (backlog > 0) {
                if (null == host) {
                    server.bind(new InetSocketAddress(service.getWebsocketPort()), backlog);
                } else {
                    server.bind(new InetSocketAddress(host, service.getWebsocketPort()), backlog);
                }
            } else {
                if (null == host) {
                    server.bind(new InetSocketAddress(service.getWebsocketPort()));
                } else {
                    server.bind(new InetSocketAddress(host, service.getWebsocketPort()));
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            //e.printStackTrace();
        }
        String ctx;
        WebsocketAdapter adp;
        for (Map.Entry<String, Object> adapterEntry : service.getAdaptersMap().entrySet()) {
            if (adapterEntry.getValue() instanceof org.cricketmsf.in.websocket.WebsocketAdapter) {
                adp = (WebsocketAdapter) adapterEntry.getValue();
                ctx = adp.getContext();
                registeredContexts.put(ctx, new ClientList(adp.properties));
                logger.info("ws context: " + ctx);
            }
        }
    }

    public void start() {
        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                Socket socket = server.accept();
                WebsocketAdapter client = new WebsocketAdapter(socket, this);
                client.start();
                String context=client.waitContext();
                if (null!=context) {
                    ClientList list=registeredContexts.get(client.getContext());
                    list.add(client);
                    registeredContexts.put(client.getContext(), list);
                    //TODO: remove client from the list after disconnecting
                }
            } catch (IOException waitException) {
                logger.warn("Could not wait for client connection. Websocket closed.");
                //throw new IllegalStateException("Could not wait for client connection", waitException);
            }
        }
    }

    public void stop() {
        try {
            running = false;
            Iterator it = registeredContexts.elements().asIterator();
            ClientList list;
            while (it.hasNext()) {
                list = (ClientList) it.next();
                for (int i = 0; i < list.size(); i++) {
                    ((WebsocketAdapter) list.get(i)).stop();
                }
            }
            server.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            //e.printStackTrace();
        }
    }

    /**
     * @return the registeredContexts
     */
    public ConcurrentHashMap<String, ClientList> getRegisteredContexts() {
        return registeredContexts;
    }

    public boolean sendMessage(String context, String message) throws WebsocketException {
        ClientList list = getRegisteredContexts().get(context);
        if (null == list) {
            throw new WebsocketException(WebsocketException.CONTEXT_NOT_DEFINED);
        }
        int counter = 0;
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setOutcomingData(message);
            counter++;
        }
        return counter == list.size();
    }

}
