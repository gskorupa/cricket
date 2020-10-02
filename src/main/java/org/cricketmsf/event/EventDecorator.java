package org.cricketmsf.event;

import com.cedarsoftware.util.io.JsonWriter;
import org.cricketmsf.Event;
import org.cricketmsf.JsonReader;

/**
 *
 * @author greg
 */
public class EventDecorator extends Event {

    protected Event originalEvent;
    protected Object data;

    public EventDecorator() {
        super();
        data = null;
        originalEvent = this;
    }

    public EventDecorator(Event event) {
        if (null == event) {
            originalEvent = this;
        } else {
            originalEvent = event;
            setTimePoint(event.getTimePoint());
            setId(event.getId());
            setName(event.getName());
        }
        data = null;
    }
    
    public EventDecorator timePoint(String timePointDefinition){
        setTimePoint(timePointDefinition);
        return this;
    }

    public Event getOriginalEvent() {
        return originalEvent;
    }

    public void setOriginalEvent(Event event) {
        originalEvent = event;
    }

    /**
     * Can be used to replace Event.toJson() method.
     *
     * Must be implemented when event payload data are not stored in
     * originalEvent but there are dedicated method fields used for that.
     *
     * @return
     */
    public String serialize() {
        return JsonWriter.objectToJson(getData());
    }

    /**
     * Must be implemented when event data are not stored in
     * originalEvent.payload but there are dedicated method fields used for
     * that.
     *
     * @param serialized
     * @throws Exception
     */
    public void deserialize(String jsonString) {
        setData(JsonReader.jsonToJava(jsonString));
    }

    /**
     * @return the data
     */
    public Object getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(Object data) {
        this.data = data;
    }

}
