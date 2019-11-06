package org.cricketmsf.event;

import org.cricketmsf.Event;

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
        setTimePoint(event.getTimePoint());
        setId(event.getId());
        setName(event.getName());
    }
    
    public Event getOriginalEvent(){
        return originalEvent;
    }
    
    public void setOriginalEvent(Event event){
        originalEvent=event;
    }
    
}
