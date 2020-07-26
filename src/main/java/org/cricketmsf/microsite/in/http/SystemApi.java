/*
 * Copyright 2020 Grzegorz Skorupa
 */
package org.cricketmsf.microsite.in.http;

import org.cricketmsf.Adapter;
import java.util.HashMap;
import org.cricketmsf.Kernel;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.HttpAdapterIface;
import org.cricketmsf.in.openapi.Operation;
import org.cricketmsf.in.openapi.Response;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class SystemApi extends HttpAdapter implements HttpAdapterIface, Adapter {

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
     *
     */
    @Override
    public void defineApi() {
        Operation getOp = new Operation()
                .description("get system configuration")
                .response(new Response("200").content("application/json").description("system configuration"))
                .response(new Response("403").description("not authenticated"));
        addOperationConfig("get", getOp);
    }

}
