package org.cricketmsf.out.log;

import java.util.HashMap;
import org.cricketmsf.Adapter;
import org.cricketmsf.event.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.out.OutboundAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author greg
 */
public class Slf4jLogger extends OutboundAdapter implements Adapter, LoggerAdapterIface{
    
    private static final Logger logger = LoggerFactory.getLogger(Slf4jLogger.class);

    public Slf4jLogger getDefault() {
        return this;
    }

    /**
     * This method is executed while adapter is instantiated during the service
     * start. It's used to configure the adapter according to the configuration.
     *
     * @param properties map of properties readed from the configuration file
     * @param adapterName name of the adapter set in the configuration file (can
     * be different from the interface and class name.
     */
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        Kernel.getInstance().getLogger().print("\tlogger: " + logger.getName());
    }

    @Override
    public void log(Event event) {
            switch (event.getType()) {
                case "LOG_INFO":
                case "INFO":
                    logger.info(event.getOrigin()+" "+(String)event.getPayload());
                    break;
                case "LOG_FINEST":
                case "FINEST":
                case "LOG_FINER":
                case "FINER":
                case "LOG_FINE":
                case "FINE":
                    logger.debug(event.getOrigin()+" "+(String)event.getPayload());
                    break;
                case "LOG_WARNING":
                case "WARNING":
                    logger.warn(event.getOrigin()+" "+(String)event.getPayload());
                    break;
                case "LOG_SEVERE":
                case "SEVERE":
                    logger.error(event.getOrigin()+" "+(String)event.getPayload());
                    break;
                default:
                    logger.debug(event.getOrigin()+" "+(String)event.getPayload());
                    break;
            }
    }

    @Override
    public void print(String message) {
        if (logger != null) {
            logger.info(message);
        }
    }
    
    @Override
    public void printIndented(String message) {
        print("    ".concat(message));
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isFineLevel() {
        return true;
    }
    
}
