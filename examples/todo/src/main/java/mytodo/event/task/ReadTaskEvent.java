package mytodo.event.task;

import org.cricketmsf.event.Event;

public class ReadTaskEvent extends Event {
    
    private Long data;
    
    public ReadTaskEvent(){
        super();
        data=null;
    }
    
    public void setData(Long id){
        data=id;
    }
    
    @Override
    public Long getData(){
        return data;
    }
    
}
