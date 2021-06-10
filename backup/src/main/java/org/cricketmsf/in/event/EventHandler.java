package org.cricketmsf.in.event;

import org.cricketmsf.event.Event;
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
        Kernel.getInstance().handleEvent(event);
    }
    
}
