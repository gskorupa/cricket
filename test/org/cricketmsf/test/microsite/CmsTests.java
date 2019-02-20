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
public class CmsTests {

    private static Kernel service;

    public CmsTests() {
    }

    @Test
    public void a_creatingDocumentOK() {
        String responseData = "";
        int responseCode = -1;
        String sessionToken = getSessionToken("admin", "cricket", "http://localhost:8080/api/auth");
        System.out.println("@sessionToken=" + sessionToken);
        // Given
        String apiEndpoint = "http://localhost:8080/api/cm";
        
        List<NameValuePair> docParameters = new ArrayList<NameValuePair>();
        docParameters.add(new BasicNameValuePair("uid", "/doc1"));
        docParameters.add(new BasicNameValuePair("language", "en"));
        docParameters.add(new BasicNameValuePair("tags", "tagA"));
        docParameters.add(new BasicNameValuePair("mimeType", "text/html"));

        // When
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(apiEndpoint);
            httpPost.setHeader("Authentication", sessionToken);
            httpPost.setEntity(new UrlEncodedFormEntity(docParameters));
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
        Assert.assertEquals(200, responseCode);
        HashMap document = new HashMap();
        try {
            document = (HashMap) JsonReader.jsonToJava(responseData);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
        Assert.assertEquals("/doc1", document.get("uid"));
    }

    @Test
    public void b_readingNotPublishedDocumentNOK() {
        // Given
        String documentUID = "/doc1";
        String documentLanguage = "en";
        HttpClient client = new HttpClient().setCertificateCheck(false);
        Request req = new Request()
                .setMethod("GET")
                .setProperty("Accept", "application/json")
                .setUrl("https://signomix.signocom.com/api/cs" + documentUID + "?language=" + documentLanguage);
        // When
        StandardResult response = (StandardResult) client.send(req, false);
        // Then
        System.out.println("@b_readingNotPublishedDocumentNOK " + response.getCode());
        Assert.assertNotEquals(200, response.getCode());
    }

    @Test
    public void c_publishingDocumentOK() {
        String responseData = "";
        int responseCode = -1;
        String sessionToken = getSessionToken("admin", "cricket", "http://localhost:8080/api/auth");
        System.out.println("@sessionToken=" + sessionToken);
        // Given
        String apiEndpoint = "http://localhost:8080/api/cm/doc1";
        List<NameValuePair> docParameters = new ArrayList<NameValuePair>();
        docParameters.add(new BasicNameValuePair("uid", "/doc1"));
        docParameters.add(new BasicNameValuePair("language", "en"));
        docParameters.add(new BasicNameValuePair("status", "published"));

        // When
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPut httpPut = new HttpPut(apiEndpoint);
            httpPut.setHeader("Authentication", sessionToken);
            httpPut.setEntity(new UrlEncodedFormEntity(docParameters));
            CloseableHttpResponse response2 = httpclient.execute(httpPut);
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
        Assert.assertEquals(200, responseCode);
        HashMap document = new HashMap();
        try {
            document = (HashMap) JsonReader.jsonToJava(responseData);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
        Assert.assertEquals("published", document.get("status"));
    }

    @Test
    public void d_readingPublishedDocumentOK() {
        // Given
        String documentUID = "/doc1";
        String documentLanguage = "en";
        HttpClient client = new HttpClient().setCertificateCheck(false);
        Request req = new Request()
                .setMethod("GET")
                .setProperty("Accept", "application/json")
                .setUrl("http://localhost:8080/api/cs" + documentUID + "?language=" + documentLanguage);
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
        HashMap document = new HashMap();
        try {
            document = (HashMap) JsonReader.jsonToJava(data);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
        Assert.assertEquals("expecting " + documentUID, documentUID, document.get("uid"));
    }

    @Test
    public void e_gettingTaggedDocumentsOK() {
        // Given
        String tagToGet = "tagA";
        String documentLanguage = "en";
        HttpClient client = new HttpClient().setCertificateCheck(false);
        Request req = new Request()
                .setMethod("GET")
                .setProperty("Accept", "application/json")
                .setUrl("http://localhost:8080/api/cs?path=/&language=" + documentLanguage + "&tag=" + tagToGet + "&path=/");
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
        data = "{\"data\":" + data + "}";
        Object[] list = null;
        try {
            Map args = new HashMap();
            args.put(JsonReader.USE_MAPS, true);
            JsonObject o = (JsonObject) JsonReader.jsonToJava(data, args);

            try {
                list = (Object[]) o.get("data");
            } catch (Exception e) {
                System.out.println("not object[]");
            }

        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
        Assert.assertTrue(list != null && list.length > 0);
    }

    @Test
    public void f_gettingTagsOK() {
        // Given
        String tagToGet = "tagA";
        String documentLanguage = "en";
        HttpClient client = new HttpClient().setCertificateCheck(false);
        Request req = new Request()
                .setMethod("GET")
                .setProperty("Accept", "application/json")
                .setUrl("http://localhost:8080/api/cs?tagsonly=true");
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
        data = "{\"data\":" + data + "}";
        Object[] list = null;
        try {
            Map args = new HashMap();
            args.put(JsonReader.USE_MAPS, true);
            JsonObject o = (JsonObject) JsonReader.jsonToJava(data, args);

            try {
                list = (Object[]) o.get("data");
            } catch (Exception e) {
                System.out.println("not object[]");
            }

        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
        Assert.assertTrue(list != null && list.length > 0);
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
