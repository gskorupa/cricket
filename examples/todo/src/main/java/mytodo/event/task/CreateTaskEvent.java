package mytodo.event.task;

import java.util.Date;
import java.util.HashMap;
import org.cricketmsf.event.Event;

/**
 *
 * @author greg
 */
public class CreateTaskEvent extends Event {
    
    private HashMap<String, Object> data;
    
    public CreateTaskEvent(){
        super();
        data=new HashMap<>();
    }
    
    public void setData(String title, Boolean done, Date deadline){
        data.put("title", title);
        data.put("done",done);
        data.put("deadline",deadline);
    }
    
    public HashMap<String,Object> getData(){
        return data;
    }
    
}
