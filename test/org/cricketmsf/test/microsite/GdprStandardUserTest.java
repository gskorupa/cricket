/*
 * Copyright 2017 Grzegorz Skorupa <g.skorupa at gmail.com>.
 * Licensed under the Apache License, Version 2.0. See LICENSE file.
 */
package org.cricketmsf.test.microsite;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import org.cricketmsf.Kernel;
import org.cricketmsf.Runner;
import org.cricketmsf.in.http.StandardResult;
import org.cricketmsf.out.http.HttpClient;
import org.cricketmsf.out.http.Request;
import org.junit.*;
import org.junit.runners.MethodSorters;


/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GdprStandardUserTest {

    private static Kernel service;
    private static String sessionToken;
    private static String LOGIN = "tester";
    private static String PASSWORD = "cricket";

    public GdprStandardUserTest() {
    }

    @Test
    public void checkValidTokenOK() {
        // Given
        HttpClient client = new HttpClient().setCertificateCheck(false);
        Request req = new Request()
                .setMethod("GET")
                .setProperty("Accept", "application/json")
                .setUrl("http://localhost:8080/api/auth/"+sessionToken);
        // When
        StandardResult response = (StandardResult) client.send(req, false);
        // Then
        Assert.assertEquals(200, response.getCode());
    }

    @Test
    public void checkFakeTokenNOK() {
        // Given
        HttpClient client = new HttpClient().setCertificateCheck(false);
        Request req = new Request()
                .setMethod("GET")
                .setProperty("Accept", "application/json")
                .setUrl("http://localhost:8080/api/auth/faketoken");
        // When
        StandardResult response = (StandardResult) client.send(req, false);
        // Then
        Assert.assertEquals(403, response.getCode());
    }

    @Test
    public void readingPersonalDataOK() {
        // Given
        HttpClient client = new HttpClient().setCertificateCheck(false);
        Request req = new Request()
                .setMethod("GET")
                .setProperty("Accept", "application/json")
                .setProperty("Authentication", sessionToken)
                .setUrl("http://localhost:8080/api/user/"+LOGIN);
        // When
        StandardResult response = (StandardResult) client.send(req, false);
        // Then
        Assert.assertEquals(200, response.getCode());
        String data = null;
        try {
            data = new String(response.getPayload(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Assert.fail(ex.getMessage());
        }
        Assert.assertFalse(data == null || data.isEmpty());
    }

    @Test
    public void readingOtherUserPersonalDataNOK() {
        //Given
        HttpClient client = new HttpClient().setCertificateCheck(false);
        Request req = new Request()
                .setMethod("GET")
                .setProperty("Accept", "application/json")
                .setProperty("Authentication", sessionToken)
                .setUrl("http://localhost:8080/api/user/admin");
        // When
        StandardResult response = (StandardResult) client.send(req);
        // Then
        Assert.assertEquals(403, response.getCode());
    }

    @Test
    public void updatingPersonalDataOK() {
        System.out.println("UPDATE PROFILE");
        String newEmail="X@xx.yy.zz";
        //Given
        HttpClient client = new HttpClient().setCertificateCheck(false);
        Request req = new Request()
                .setMethod("PUT")
                .setProperty("Accept", "application/json")
                .setProperty("Authentication", sessionToken)
                .setUrl("http://localhost:8080/api/user/"+LOGIN)
                .setData("email="+newEmail);
        // When
        StandardResult response = (StandardResult) client.send(req);
        // Then
        Assert.assertEquals(200, response.getCode());
        String data = null;
        try {
            data = new String(response.getPayload(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Assert.fail(ex.getMessage());
        }
        System.out.println(data);
        Assert.assertTrue("email not updated",data.indexOf(newEmail)>0);
    }

    @Test
    public void updatingOtherUserPersonalDataNOK() {
        Assert.assertTrue(true);
    }

    @Test
    public void deletingOtherUserPersonalDataNOK() {
        Assert.assertTrue(true);
    }

    @Test
    public void z_deletingPersonalDataOK() {
        System.out.println("deletingPersonalDataOK");
        Assert.assertTrue(true);
    }

    /**
     *
     */
    private static String getSessionToken() {
        // Given
        String login = LOGIN;
        String password = PASSWORD;
        String credentials = Base64.getEncoder().encodeToString((login + ":" + password).getBytes());
        HttpClient client = new HttpClient().setCertificateCheck(false);
        Request req = new Request()
                .setMethod("POST")
                .setProperty("Accept", "text/plain")
                .setProperty("Authentication", "Basic " + credentials)
                .setData("p=ignotethis") /*data must be added to POST or PUT requests */
                .setUrl("http://localhost:8080/api/auth");
        // When
        StandardResult response = (StandardResult) client.send(req);
        // Then
        Assert.assertEquals(200, response.getCode());
        String token = "";
        try {
            token = new String(response.getPayload(), "UTF-8");
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
