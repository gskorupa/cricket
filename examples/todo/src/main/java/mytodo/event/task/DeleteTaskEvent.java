package mytodo.event.task;

import java.util.Date;
import java.util.HashMap;
import org.cricketmsf.event.Event;

/**
 *
 * @author greg
 */
public class DeleteTaskEvent extends Event {
    
    private Long data;
    
    public DeleteTaskEvent(){
        super();
        data=null;
    }
    
    public void setData(Long id){
        data=id;
    }
    
}
