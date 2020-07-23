/*
 * Copyright 2020 Grzegorz Skorupa
 */
package org.cricketmsf.microsite.in.http;

import org.cricketmsf.Adapter;
import java.util.HashMap;
import org.cricketmsf.Kernel;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.HttpAdapterIface;
import org.cricketmsf.out.openapi.Operation;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class SystemApi extends HttpAdapter implements HttpAdapterIface, Adapter {
    
    /**
     * This method is executed while adapter is instantiated during the service start.
     * It's used to configure the adapter according to the configuration.
     * 
     * @param properties    map of properties read from the configuration file
     * @param adapterName   name of the adapter set in the configuration file (can be different
     *  from the interface and class name.
     */
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        super.getServiceHooks(adapterName);
        setContext(properties.get("context"));
        Kernel.getInstance().getLogger().printIndented("context=" + getContext());
    }
    
    @Override
    public void defineOperations(){
        Operation op=new Operation();
        op.setDescription("get sys config");
        addOperationConfig("get", op);
    }    
    

}
