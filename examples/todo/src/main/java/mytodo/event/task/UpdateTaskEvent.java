package mytodo.event.task;

import java.util.Date;
import java.util.HashMap;
import org.cricketmsf.event.Event;

/**
 *
 * @author greg
 */
public class UpdateTaskEvent extends Event {
    
    private HashMap<String, Object> data;
    
    public UpdateTaskEvent(){
        super();
        data=new HashMap<>();
    }
    
    public void setData(Long id, String title, Boolean done, Date deadline){
        data.put("id",id);
        data.put("title", title);
        data.put("done",done);
        data.put("deadline",deadline);
    }
    
    public HashMap<String,Object> getData(){
        return data;
    }
    
}
