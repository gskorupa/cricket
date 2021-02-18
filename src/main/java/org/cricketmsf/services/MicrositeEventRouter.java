package org.cricketmsf.services;

import java.util.HashMap;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.annotation.EventHook;
import org.cricketmsf.api.Result;
import org.cricketmsf.api.ResultIface;
import org.cricketmsf.api.StandardResult;
import org.cricketmsf.event.Event;
import org.cricketmsf.event.GreeterEvent;
import org.cricketmsf.event.HttpEvent;
import org.cricketmsf.event.Procedures;
import org.cricketmsf.services.BasicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author greg
 */
public class MicrositeEventRouter {

    private static final Logger logger = LoggerFactory.getLogger(MicrositeEventRouter.class);
    
    private Microsite service;
    
    public MicrositeEventRouter(Microsite service){
        this.service=service;
    }

    

}
