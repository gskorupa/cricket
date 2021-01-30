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
package org.cricketmsf;

import org.cricketmsf.in.http.HttpPortedAdapter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import org.cricketmsf.in.http.HttpPortedAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class CricketHttpd implements HttpdIface {

    private static final Logger logger = LoggerFactory.getLogger(CricketHttpd.class);

    public HttpServer server = null;
    public HttpsServer sserver = null;
    private boolean ssl = false;
    String keystore;
    String password;

    //For SSL see: https://www.sothawo.com/2011/10/java-webservice-using-https/
    public CricketHttpd(Kernel service) {
        String host = service.getHost();
        int backlog = 0;
        try {
            backlog = Integer.parseInt((String) service.getProperties().getOrDefault("threads", "256"));
        } catch (NumberFormatException | ClassCastException e) {
        }
        keystore = (String) service.getProperties().getOrDefault("keystore", "");
        password = (String) service.getProperties().getOrDefault("keystore-password", "");
        //ssl = "true".equalsIgnoreCase("" + service.getProperties().getOrDefault("ssl", "false"));
        if ("false".equalsIgnoreCase(service.getSslAlgorithm()) || "no".equalsIgnoreCase(service.getSslAlgorithm())) {
            ssl = false;
        } else {
            ssl = true;
        }

        if (ssl && (keystore.isEmpty() || password.isEmpty())) {
            System.out.println("SSL not configured properly");
            System.exit(100);
        }
        if (null != host) {
            if (host.isEmpty() || "0.0.0.0".equals(host) || "*".equals(host)) {
                host = null;
            }
        }
        try {
            if (host == null) {
                if (ssl) {
                    sserver = HttpsServer.create(new InetSocketAddress(service.getPort()), backlog);
                } else {
                    server = HttpServer.create(new InetSocketAddress(service.getPort()), backlog);
                }
            } else {
                if (ssl) {
                    sserver = HttpsServer.create(new InetSocketAddress(host, service.getPort()), backlog);
                } else {
                    server = HttpServer.create(new InetSocketAddress(host, service.getPort()), backlog);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpContext context;
        SSLContext scontext;
        try {
            for (Map.Entry<String, Object> adapterEntry : service.getAdaptersMap().entrySet()) {
                if (adapterEntry.getValue() instanceof org.cricketmsf.in.http.HttpPortedAdapter) {
                    logger.info("context: " + ((HttpPortedAdapter) adapterEntry.getValue()).getContext());
                    if (ssl) {
                        scontext = SSLContext.getInstance(service.getSslAlgorithm());
                        // keystore
                        char[] keystorePassword = password.toCharArray();
                        KeyStore ks = KeyStore.getInstance("JKS");
                        ks.load(new FileInputStream(keystore), keystorePassword);
                        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                        kmf.init(ks, keystorePassword);

                        scontext.init(kmf.getKeyManagers(), null, null);

                        HttpsConfigurator configurator = new HttpsConfigurator(scontext);
                        sserver.setHttpsConfigurator(configurator);

                        context = sserver.createContext(((HttpPortedAdapter) adapterEntry.getValue()).getContext(), (com.sun.net.httpserver.HttpHandler) adapterEntry.getValue());
                    } else {
                        context = server.createContext(((HttpPortedAdapter) adapterEntry.getValue()).getContext(), (com.sun.net.httpserver.HttpHandler) adapterEntry.getValue());
                    }
                    context.getFilters().add(new MaintenanceFilter());
                    context.getFilters().add(new ParameterFilter());
                    context.getFilters().add(service.getAuthorizationFilter());
                }
            }
        } catch (NoSuchAlgorithmException | IOException | KeyStoreException | CertificateException | UnrecoverableKeyException | KeyManagementException ex) {
            ex.printStackTrace();
            System.out.println("SSL not configured properly");
            System.exit(100);
        }
    }

    public void run() {
        //Create a default executor
        if (isSsl()) {
            //sserver.setExecutor(null);
            sserver.start();
        } else {
            server.setExecutor(null);
            server.start();
        }
    }

    public void stop() {
        if (isSsl()) {
            sserver.stop(Kernel.getInstance().getShutdownDelay());
        } else {
            server.stop(Kernel.getInstance().getShutdownDelay());
        }
    }

    /**
     * @return the ssl
     */
    public boolean isSsl() {
        return ssl;
    }

}
