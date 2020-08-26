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
import org.cricketmsf.in.openapi.Operation;
import org.cricketmsf.in.openapi.Parameter;
import org.cricketmsf.in.openapi.ParameterLocation;
import org.cricketmsf.in.openapi.Response;
import org.cricketmsf.in.openapi.Schema;
import org.cricketmsf.in.openapi.SchemaFormat;
import org.cricketmsf.in.openapi.SchemaType;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class HelloAdapter extends HttpPortedAdapter{

    public static int PARAM_NOT_FOUND = 1;

    public HelloAdapter() {
        super();
    }
    
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
    }

    @Override
    protected ProcedureCall preprocess(RequestObject request, long rootEventId) {
        switch (request.method) {
            case "GET":
                return preprocessGet(request);
            case "POST":
                return preprocessPost(request);
            default:
                HashMap<String, Object> err = new HashMap<>();
                err.put("code", 405); //code<100 || code >1000
                err.put("message", String.format("method %1s not allowed", request.method));
                return ProcedureCall.respond(405, err);
        }
    }

    private ProcedureCall preprocessGet(RequestObject request) {
        HashMap<String, Object> err = new HashMap<>();
        
        /*
        * Validation using the API definition
        */
        String[] pathParams = new String[0];
        if (!request.pathExt.isBlank()) {
            pathParams = request.pathExt.split("/");
        }
        // checking required path parameters
        ArrayList<Parameter> requiredPathParams = getParams("GET", true, ParameterLocation.path);
        if (requiredPathParams.size() > pathParams.length) {
            err.put("code", PARAM_NOT_FOUND); //code<100 || code >1000
            for (int i = 0; i < requiredPathParams.size(); i++) {
                if (i >= pathParams.length) {
                    err.put("message" + i, String.format("path parameter '%1s' not found", requiredPathParams.get(i).getName()));
                }
            }
        }
        if(err.size()>0){
            return ProcedureCall.respond(PARAM_NOT_FOUND, err);
        }
        /* End of API validation */

        /*
        * Getting request parameters
        */
        String name = pathParams[0];
        String friend = (String) request.parameters.getOrDefault("friend", "");
        
        /*
        * Custom validation
        * The second parameter should be in query and it's optional
        * but we decide that value "world" is not allowed
        */
        if("world".equalsIgnoreCase(friend)){
            err.put("code", 400);
            err.put("message", String.format("friend value '%1s' is not allowed", "world"));
            return ProcedureCall.respond(400, err);
        }
        
        
        /*
        * Forwarding dedicated event type to the service
        */
        return ProcedureCall.forward(new HelloEvent(name, friend), "sayHello");
    }

    private ProcedureCall preprocessPost(RequestObject request) {
        // validation and translation 
        // the name parameter is required
        String name = (String) request.parameters.getOrDefault("name", "");
        if (name.isEmpty()) {
            HashMap<String, Object> err = new HashMap<>();
            err.put("code", PARAM_NOT_FOUND); //code<100 || code >1000
            // http status codes can be used directly:
            // err.put("code", 404);
            err.put("message", "unknown name");
            return ProcedureCall.respond(PARAM_NOT_FOUND, err);
        }
        // forwarding dedicated event type to the service
        return ProcedureCall.forward(new HelloEvent(name, ""), "addUser");
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
                                new Schema(SchemaType.string, SchemaFormat.string)
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
                .parameter(
                        new Parameter(
                                "name",
                                ParameterLocation.query,
                                true,
                                "new user name")
                )
                .response(new Response("200").content("text/plain").description("user name registered"))
                .response(new Response("400").description("Invalid request parameters"));
        addOperationConfig(postOp);
    }

}
