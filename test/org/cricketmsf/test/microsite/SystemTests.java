/*
 * Copyright 2017 Grzegorz Skorupa <g.skorupa at gmail.com>.
 * Licensed under the Apache License, Version 2.0. See LICENSE file.
 */
package org.cricketmsf.test.microsite;

import com.cedarsoftware.util.io.JsonObject;
import java.io.UnsupportedEncodingException;
import org.cricketmsf.Kernel;
import org.cricketmsf.in.http.StandardResult;
import org.cricketmsf.out.http.HttpClient;
import org.cricketmsf.out.http.Request;
import org.junit.*;
import org.junit.runners.MethodSorters;
import com.cedarsoftware.util.io.JsonReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.cricketmsf.Runner;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SystemTests {

    private static Kernel service;

    public SystemTests() {
    }

    @Test
    public void a_readUserTableOK() {
        String responseData = "";
        int responseCode = -1;
        int port = service.getPort();
        String sessionToken = getSessionToken("admin", "cricket", "http://localhost:"+port+"/api/auth");
        //System.out.println("@sessionToken=" + sessionToken);
        // Given
        String apiEndpoint = "http://localhost:"+port+"/api/system/database";
        
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("adapter", "userDB"));
        parameters.add(new BasicNameValuePair("query", "select * from users"));

        // When
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(apiEndpoint);
            httpPost.setHeader("Authentication", sessionToken);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setEntity(new UrlEncodedFormEntity(parameters));
            CloseableHttpResponse response2 = httpclient.execute(httpPost);
            try {
                responseCode = response2.getStatusLine().getStatusCode();
                System.out.println(response2.getStatusLine());
                HttpEntity entity2 = response2.getEntity();
                responseData = EntityUtils.toString(entity2);
                EntityUtils.consume(entity2);
            } finally {
                response2.close();
            }
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        // Then
        System.out.println(">>>>>>>>>>> SQL RESULT: "+responseData);
        Assert.assertEquals(200, responseCode);
    }
    
    @Test
    public void b_setSystemStatusMaintenance() {
        String responseData = "";
        int responseCode = -1;
        int port = service.getPort();
        String sessionToken = getSessionToken("admin", "cricket", "http://localhost:"+port+"/api/auth");
        //System.out.println("@sessionToken=" + sessionToken);
        // Given
        String apiEndpoint = "http://localhost:"+port+"/api/system/status";
        
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("status", "maintenance"));

        // When
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(apiEndpoint);
            httpPost.setHeader("Authentication", sessionToken);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setEntity(new UrlEncodedFormEntity(parameters));
            CloseableHttpResponse response2 = httpclient.execute(httpPost);
            try {
                responseCode = response2.getStatusLine().getStatusCode();
                System.out.println(response2.getStatusLine());
                HttpEntity entity2 = response2.getEntity();
                responseData = EntityUtils.toString(entity2);
                EntityUtils.consume(entity2);
            } finally {
                response2.close();
            }
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        // Then
        System.out.println(">>>>>>>>>>> STATUS: "+responseData);
        Assert.assertEquals(200, responseCode);
    }
    
     @Test
    public void c_setSystemStatusRunning() {
        String responseData = "";
        int responseCode = -1;
        int port = service.getPort();
        String sessionToken = getSessionToken("admin", "cricket", "http://localhost:"+port+"/api/auth");
        //System.out.println("@sessionToken=" + sessionToken);
        // Given
        String apiEndpoint = "http://localhost:"+port+"/api/system/status";
        
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("status", "online"));

        // When
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(apiEndpoint);
            httpPost.setHeader("Authentication", sessionToken);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setEntity(new UrlEncodedFormEntity(parameters));
            CloseableHttpResponse response2 = httpclient.execute(httpPost);
            try {
                responseCode = response2.getStatusLine().getStatusCode();
                System.out.println(response2.getStatusLine());
                HttpEntity entity2 = response2.getEntity();
                responseData = EntityUtils.toString(entity2);
                EntityUtils.consume(entity2);
            } finally {
                response2.close();
            }
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        // Then
        System.out.println(">>>>>>>>>>> STATUS: "+responseData);
        Assert.assertEquals(200, responseCode);
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
    }

    @AfterClass
    public static void shutdown() {

        System.out.println("@shutdown");
        service.shutdown();

    }

    private String getSessionToken(String login, String password, String authEndpoint) {
        String credentials = Base64.getEncoder().encodeToString((login + ":" + password).getBytes());
        HttpClient client = new HttpClient().setCertificateCheck(false);
        Request req = new Request()
                .setMethod("POST")
                .setProperty("Accept", "text/plain")
                .setProperty("Authentication", "Basic " + credentials)
                .setData("p=ignotethis") /*data must be added to POST or PUT requests */
                .setUrl(authEndpoint);
        StandardResult response = (StandardResult) client.send(req);
        String token;
        try {
            token = new String(response.getPayload(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return "error";
        }
        return token;
    }
}
