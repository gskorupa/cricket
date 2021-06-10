package org.cricketmsf.services;

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
