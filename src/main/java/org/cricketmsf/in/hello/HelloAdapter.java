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
package org.cricketmsf.in.hello;

import java.util.ArrayList;
import java.util.HashMap;
import org.cricketmsf.RequestObject;
import org.cricketmsf.event.ProcedureCall;
import org.cricketmsf.in.http.HttpPortedAdapter;
import org.cricketmsf.in.openapi.BodyContent;
import org.cricketmsf.in.openapi.Operation;
import org.cricketmsf.in.openapi.Parameter;
import org.cricketmsf.in.openapi.ParameterLocation;
import org.cricketmsf.in.openapi.RequestBody;
import org.cricketmsf.in.openapi.Response;
import org.cricketmsf.in.openapi.Schema;
import org.cricketmsf.in.openapi.SchemaProperty;
import org.cricketmsf.in.openapi.SchemaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloAdapter extends HttpPortedAdapter {

    private static final Logger logger = LoggerFactory.getLogger(HelloAdapter.class);

    public static int PARAM_NOT_FOUND = 1;

    public HelloAdapter() {
        super();
    }
    
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        // "context" parameter is read by HttpPortedAdapter class
    }

    @Override
    protected ProcedureCall preprocess(RequestObject request, long rootEventId) {
        switch (request.method) {
            case "GET":
                return preprocessGet(request);
            case "POST":
                return preprocessPost(request);
            case "OPTIONS":
                return ProcedureCall.respond(200, null);
            default:
                HashMap<String, Object> err = new HashMap<>();
                err.put("code", 405); //code<100 || code >1000
                err.put("message", String.format("method %1s not allowed", request.method));
                return ProcedureCall.respond(405, err);
        }
    }

    private ProcedureCall preprocessGet(RequestObject request) {

        /*
        Validating request
         */
        ProcedureCall validationResult = validateGet(request);
        if (null != validationResult) {
            return validationResult;
        }

        /*
        Getting request parameters
         */
        String[] pathParams = pathParams = request.pathExt.split("/");
        String name = pathParams.length > 0 ? pathParams[0] : "";
        String friend = (String) request.parameters.getOrDefault("friend", "");

        /*
        Forwarding dedicated event type to the service
         */
        return ProcedureCall.forward(new HelloEvent(name, friend), "sayHello");
    }

    private ProcedureCall preprocessPost(RequestObject request) {
        // validation and translation 

        // the name parameter is required
        String name = (String) request.parameters.getOrDefault("name", "");
        // forwarding dedicated event type to the service
        return ProcedureCall.forward(new HelloEvent(name, ""), "addUser");
    }

    private ProcedureCall validateGet(RequestObject request) {
        HashMap<String, Object> err = new HashMap<>();
        
        /*
        * Validation using the API definition
        */
        String[] pathParams = new String[0];
        if (!request.pathExt.isBlank()) {
            pathParams = request.pathExt.split("/");
        }
        int pathParamsCount = pathParams.length;
        if (pathParamsCount > 0 && pathParams[0].isBlank()) {
            pathParamsCount = 0;
        }
        // checking required path parameters
        ArrayList<Parameter> requiredPathParams = getParams("GET", true, ParameterLocation.path);
        System.out.println("REQUIRED PATH PARAMS " + requiredPathParams.size());
        System.out.println("PATH PARAMS " + pathParamsCount);
        if (requiredPathParams.size() > pathParamsCount) {
            err.put("code", 400); //code<100 || code >1000
            for (int i = 0; i < requiredPathParams.size(); i++) {
                if (i >= pathParamsCount) {
                    err.put("message" + i, String.format("path parameter '%1s' not found", requiredPathParams.get(i).getName()));
                }
            }
            return ProcedureCall.respond(400, err);
        }

        //query parameters
        String name = (String) request.parameters.getOrDefault("name", "");
        if (name.isEmpty()) {
            err.put("code", 400); //code<100 || code >1000
            // http status codes can be used directly:
            // err.put("code", 404);
            err.put("message", "unknown name");
            return ProcedureCall.respond(400, err);
        }
        /* End of API validation */

        /*
        * Custom validation
        * The second parameter should be in query and it's optional
        * but we decide that value "world" is not allowed
        */
        //if ("world".equalsIgnoreCase(friend)) {
        //    err.put("code", 400);
        //    err.put("message", String.format("friend value '%1s' is not allowed", "world"));
        //    return ProcedureCall.respond(400, err);
        //}
        
        return null;
    }

    /**
     * The method provides API documentation for this adapter.
     */
    @Override
    public void defineApi() {
        // GET request definition
        Operation getOp = new Operation("GET")
                .tag("hello")
                .description("get greetings")
                .summary("example get method")
                .pathModifier("/{name}")
                .parameter(
                        new Parameter(
                                "name",
                                ParameterLocation.path,
                                true,
                                "User name.",
                                new Schema(SchemaType.string)
                        )
                )
                .parameter(
                        new Parameter(
                                "friend",
                                ParameterLocation.query,
                                false,
                                "The name of the friend you want to send greetings to.")
                )
                .response(new Response("200").content("text/plain").description("response"))
                .response(new Response("400").description("Invalid request parameters "))
                .response(new Response("404").description("User name not found"));
        addOperationConfig(getOp);

        // POST request definition
        Operation postOp = new Operation("POST")
                .tag("hello")
                .description("registering user name")
                .summary("example post method")
                .response(new Response("200").content("text/plain").description("user name registered"))
                .response(new Response("400").description("Invalid request parameters"));

        SchemaProperty schemaProperty1 = new SchemaProperty("name", SchemaType.string, null, "");
        Schema schema = new Schema(SchemaType.object).property(schemaProperty1);
        //SchemaProperty schemaProperty2=new SchemaProperty("friend", SchemaType.string, null, "");
        //schema=schema.property(schemaProperty2);

        BodyContent bodyContent = new BodyContent("application/x-www-form-urlencoded", schema);
        //BodyContent bodyContent=new BodyContent("application/json", schema2);

        RequestBody body = new RequestBody(bodyContent, true, "description");
        //body.content(bc2);

        postOp.body(body);
        addOperationConfig(postOp);
    }

}
