package mytodo.out;

import java.util.List;
import mytodo.Task;
import mytodo.event.task.CreateTaskEvent;
import mytodo.event.task.UpdateTaskEvent;

/**
 *
 * @author greg
 */
public interface TodoDaoIface {
    
    public boolean createTask(CreateTaskEvent event);
    public Task readTask(long id);
    public List<Task> readAll();
    public boolean updateTask(UpdateTaskEvent event);
    public boolean deleteTask(long id);
    
}
