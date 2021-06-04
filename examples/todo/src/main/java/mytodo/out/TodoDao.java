package mytodo.out;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import mytodo.Task;
import mytodo.event.task.CreateTaskEvent;
import mytodo.event.task.UpdateTaskEvent;
import org.cricketmsf.Adapter;
import org.cricketmsf.out.OutboundAdapter;

public class TodoDao extends OutboundAdapter implements Adapter,TodoDaoIface {
    
    HashMap<Long,Task> tasks;

    public TodoDao() {
        super();
        tasks=new HashMap<>();
    }
    
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
    }

    @Override
    public boolean createTask(CreateTaskEvent event) {
        Task t=new Task();
        t.id=System.currentTimeMillis();
        t.title=(String)event.getData().get("title");
        tasks.put(t.id, t);
        return true;
    }

    @Override
    public Task readTask(long id) {
        return tasks.get(id);
    }

    @Override
    public List<Task> readAll() {
        return tasks.values().stream().collect(Collectors.toList());
    }

    @Override
    public boolean updateTask(UpdateTaskEvent event) {
        Task t=new Task();
        t.id=(Long)event.getData().get("id");
        t.title=(String)event.getData().get("title");
        if(tasks.containsKey(t.id)){
            tasks.put(t.id,t);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteTask(long id) {
        tasks.remove(id);
        return true;
    }

}
