package org.cricketmsf.in.event;

import org.cricketmsf.Event;
import org.cricketmsf.Kernel;

/**
 *
 * @author greg
 */
public class EventHandler implements Runnable {
    
    Event event;
    
    public EventHandler(Event event){
        this.event=event;
    }

    @Override
    public void run() {
        Kernel.getInstance().getEventProcessingResult(event);
    }
    
}
