package org.cricketmsf.event;

import com.cedarsoftware.util.io.JsonWriter;
import java.util.HashMap;
import org.cricketmsf.util.JsonReader;

/**
 *
 * @author greg
 */
public class EventDto {

    public String eventClassName;
    public HashMap<String, Object> data;

    public EventDto() {
    }

    public EventDto(String eventClassName, HashMap<String,Object> data) {
        this.eventClassName = eventClassName;
        this.data = data;
    }
    
    public EventDto(Event event){
        eventClassName=event.getClass().getName();
        data=(HashMap)event.getData();
    }
    
    public String toJson() {
        return JsonWriter.objectToJson(this);
    }
    
    public static EventDto fromJson(String json) {
        return (EventDto) JsonReader.jsonToJava(json);
    }

}
