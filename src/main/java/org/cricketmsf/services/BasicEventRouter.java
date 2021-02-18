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
public class BasicEventRouter {

    private static final Logger logger = LoggerFactory.getLogger(BasicEventRouter.class);
    
    private BasicService service;
    
    public BasicEventRouter(BasicService service){
        this.service=service;
    }

    @EventHook(className = "org.cricketmsf.event.Event", procedure = Procedures.SYSTEM_STATUS)
    public ResultIface handleStatusRequest(Event requestEvent) {
        logger.info("Hello from {}", this.getClass().getName());
        return new StandardResult(((BasicService) Kernel.getInstance()).reportStatus());
    }

    @EventHook(className = "org.cricketmsf.event.Event", procedure = Procedures.PRINT_INFO)
    public Result printInfo(Event event) {
        logger.info("INFO {} {} {}", service.getProceduresDictionary().getName(event.getProcedure()), event.getTimeDefinition(), event.getData());
        return null;
    }
    
    /**
     *
     * @param event
     * @return ParameterMapResult with the file content as a byte array
     */
    @EventHook(className = "org.cricketmsf.event.HttpEvent", procedure = Procedures.WWW)
    public ResultIface doGet(HttpEvent event) {
        return service.wwwFileReader.getFile(
                (RequestObject) event.getData(),
                service.cacheDB,
                "webcache"
        );
    }

    @EventHook(className = "org.cricketmsf.event.GreeterEvent", procedure = Procedures.GREET)
    public ResultIface doGreet(GreeterEvent event) {
        String name = ((HashMap<String, String>) event.getData()).get("name");
        ResultIface result = new Result("Hello " + name);
        result.setProcedure(Procedures.GREET);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {

        }
        return result;
    }


}
