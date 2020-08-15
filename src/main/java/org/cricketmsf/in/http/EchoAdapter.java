/*
 * Copyright 2020 Grzegorz Skorupa
 */
package org.cricketmsf.in.http;

import org.cricketmsf.microsite.in.http.*;
import org.cricketmsf.Adapter;
import java.util.HashMap;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.event.ProcedureCall;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.HttpAdapterIface;
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
public class EchoAdapter extends HttpPortedAdapter implements HttpAdapterIface, Adapter {

    /**
     * This method is executed while adapter is instantiated during the service
     * start. It's used to configure the adapter according to the configuration.
     *
     * @param properties map of properties read from the configuration file
     * @param adapterName name of the adapter set in the configuration file (can
     * be different from the interface and class name.
     */
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        super.getServiceHooks(adapterName);
        setContext(properties.get("context"));
        Kernel.getInstance().getLogger().printIndented("context=" + getContext());
    }

    /**
     * The method provides api documentation for this adapter.
     */
    @Override
    public void defineApi() {
        Operation getOp = new Operation()
                .tag("echo")
                .description("example get method")
                .summary("example get method")
                .parameter(
                        new Parameter(
                                "param1",
                                ParameterLocation.path,
                                true,
                                "some description1",
                                new Schema(SchemaType.string, SchemaFormat.string)
                        )
                )
                .parameter(
                        new Parameter(
                                "param2",
                                ParameterLocation.query,
                                false,
                                "some description2")
                )
                .response(new Response("200").content("application/json").description("echo response"))
                .response(new Response("400").description("Invalid request parameters "));
        addOperationConfig("get", getOp);
        
    }

    @Override
    protected ProcedureCall preprocess(RequestObject request, long rootEventId) {
        String method = request.method;
        if ("GET".equalsIgnoreCase(method)) {
            return preprocessGet(request);
        } else {
            return preprocessPost(request);
        }
    }
    
    private ProcedureCall preprocessGet(RequestObject request){
        return ProcedureCall.respond(ResponseCode.OK,"text/plain", "OK");
    }
    
    private ProcedureCall preprocessPost(RequestObject request){
        return ProcedureCall.respond(ResponseCode.OK, "text/paain","OK");
    }

}
