package mytodo;

import mytodo.out.TodoDaoIface;
import org.cricketmsf.services.MinimalService;
import org.slf4j.LoggerFactory;

public class TodoService extends MinimalService {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TodoService.class);
  
    public TodoDaoIface todoDao=null;
    
    public TodoService() {
        super();
        setEventRouter(new TodoEventRouter(this));
    }

    @Override
    public void getAdapters() {
        super.getAdapters();
        // your code
        todoDao = (TodoDaoIface) getRegistered("TodoDao");
    }

    @Override
    public void runInitTasks() {
        //try {
            super.runInitTasks();
        //} catch (InitException ex) {

        //}
        // your code here
        setInitialized(true);
    }

    @Override
    public void shutdown() {
        // your code here
        super.shutdown();
    }
    
}
