package org.cricketmsf.event;

import org.cricketmsf.Event;
import org.cricketmsf.exception.EventException;

/**
 *
 * @author greg
 */
public class EventDecorator extends Event{
    
    protected Event originalEvent;
    
    public EventDecorator(){
        super();
        originalEvent = this;
    }
    
    public EventDecorator(Event event){
        originalEvent =event;
    }
    
    public Event getOriginalEvent(){
        return originalEvent;
    }
    
}
