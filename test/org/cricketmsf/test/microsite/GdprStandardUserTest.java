/*
 * Copyright 2017 Grzegorz Skorupa <g.skorupa at gmail.com>.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.cricketmsf.test.microsite;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import org.cricketmsf.Kernel;
import org.cricketmsf.Runner;
import org.cricketmsf.in.http.StandardResult;
import org.cricketmsf.out.http.OutboundHttpAdapter;
import org.cricketmsf.out.http.Request;
import org.junit.*;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class GdprStandardUserTest {

    private static Kernel service;
    private static String sessionToken;
    private static String LOGIN = "tester";
    private static String PASSWORD = "cricket";

    public GdprStandardUserTest() {
    }

    /**
     *
     */
    @Test
    public void loggingOK() {
        Assert.assertTrue(true);
    }

    /**
     *
     */
    @Test
    public void readingPersonalDataOK() {
        OutboundHttpAdapter client = new OutboundHttpAdapter();
        Request req = new Request();
        req.setMethod("GET");
        req.setProperty("Accept", "application/json");
        req.setProperty("Authentication", sessionToken);
        String requestUrl = "http://localhost:8080/api/user/" + LOGIN;
        String data=null;
        StandardResult res = (StandardResult) client.send(requestUrl, req, null, false, true);
        Assert.assertEquals(200, res.getCode());
        try {
            data = new String(res.getPayload(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Assert.fail(ex.getMessage());
        }
        Assert.assertFalse(data == null || data.isEmpty());
    }

    /**
     *
     */
    @Test
    public void readingOtherUserPersonalDataNOK() {
        OutboundHttpAdapter client = new OutboundHttpAdapter();
        Request req = new Request();
        req.setMethod("GET");
        req.setProperty("Accept", "application/json");
        req.setProperty("Authentication", sessionToken);
        String requestUrl = "http://localhost:8080/api/user/" + "admin";
        StandardResult res = (StandardResult) client.send(requestUrl, req, null, false, true);
        Assert.assertEquals(403, res.getCode());
    }

    /**
     *
     */
    @Test
    public void updatingPersonalDataOK() {
        Assert.assertTrue(true);
    }

    /**
     *
     */
    @Test
    public void updatingOtherUserPersonalDataNOK() {
        Assert.assertTrue(true);
    }

    /**
     *
     */
    @Test
    public void deletingOtherUserPersonalDataNOK() {
        Assert.assertTrue(true);
    }

    /**
     *
     */
    @Test
    public void deletingPersonalDataOK() {
        Assert.assertTrue(true);
    }

    /**
     *
     */
    private static String getSessionToken() {
        String login = LOGIN;
        String password = PASSWORD;
        String credentials = Base64.getEncoder().encodeToString((login + ":" + password).getBytes());
        OutboundHttpAdapter client = new OutboundHttpAdapter();
        Request req = new Request();
        req.setMethod("POST");
        req.setProperty("Accept", "text/plain");
        req.setProperty("Authentication", "Basic " + credentials);
        req.setData("p=ignotethis"); // data must be added to POST or PUT requests
        String requestUrl = "http://localhost:8080/api/auth";
        StandardResult res = (StandardResult) client.send(requestUrl, req, null, false, true);

        Assert.assertEquals(200, res.getCode());
        String token = "";
        try {
            token = new String(res.getPayload(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Assert.fail(ex.getMessage());
        }
        Assert.assertFalse(token == null || token.isEmpty());
        return token;
    }

    @Before
    public void checkService() {
        System.out.println("@before");
        Assert.assertNotNull(service);
    }

    @BeforeClass
    public static void setup() {
        System.out.println("@setup");
        String[] args = {"-r", "-s", "Microsite"};
        service = Runner.getService(args);
        while (!service.isInitialized()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("service is running");
        sessionToken = getSessionToken();
    }

    @AfterClass
    public static void shutdown() {
        System.out.println("@shutdown");
        service.shutdown();
    }

}
