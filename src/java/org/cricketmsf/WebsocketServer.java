/*
 * Copyright 2019 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
import java.util.ArrayList;
import org.cricketmsf.in.websocket.WebsocketAdapter;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class WebsocketServer implements Runnable {

    private ServerSocket server = null;
    private boolean running = false;
    ArrayList<WebsocketAdapter> clients = new ArrayList<>();

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
            e.printStackTrace();
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
                Socket socket=server.accept();
                WebsocketAdapter client = new WebsocketAdapter(socket);
                client.start();
                clients.add(client);
            } catch (IOException waitException) {
                throw new IllegalStateException("Could not wait for client connection", waitException);
            }
        }
    }

    public void stop() {
        try {
            running = false;
            server.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        clients.forEach(client->{client.stop();});
    }

}
