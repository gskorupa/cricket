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
package org.cricketmsf.in.http;

import java.util.HashMap;
import org.cricketmsf.RequestObject;
import org.cricketmsf.event.ProcedureCall;
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
public class EchoAdapter extends HttpPortedAdapter {

    public EchoAdapter() {
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
            default:
                HashMap<String, Object> err = new HashMap<>();
                err.put("code", 405); //code<100 || code >1000
                err.put("message", String.format("method %1s not allowed", request.method));
                return ProcedureCall.respond(405, err);
        }
    }

    private ProcedureCall preprocessGet(RequestObject request) {
        String name = (String) request.parameters.getOrDefault("name", "");
        if(name.isEmpty()){
            HashMap<String, Object> err = new HashMap<>();
            err.put("code", 400);
            err.put("message", "parameter 'name' not found");
            return ProcedureCall.respond(400, err);
        }else{
            return ProcedureCall.respond(200, "text/plain", String.format("Hello %1s!",name));
        }
    }

    /**
     * The method provides API documentation for this adapter.
     */
    @Override
    public void defineApi() {
        // GET request definition
        Operation getOp = new Operation("GET")
                .tag("echo")
                .description("get greetings")
                .summary("example get method")
                .parameter(
                        new Parameter(
                                "name",
                                ParameterLocation.query,
                                true,
                                "User name.",
                                new Schema(SchemaType.string)
                        )
                )
                .response(new Response("200").content("text/plain").description("response"))
                .response(new Response("400").description("Invalid request parameters "));
        addOperationConfig(getOp);
    }

}
