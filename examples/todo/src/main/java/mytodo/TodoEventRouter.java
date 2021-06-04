package mytodo;

import mytodo.event.task.CreateTaskEvent;
import mytodo.event.task.DeleteTaskEvent;
import mytodo.event.task.ReadTaskEvent;
import mytodo.event.task.UpdateTaskEvent;
import org.cricketmsf.annotation.EventHook;
import org.cricketmsf.api.HttpResult;
import org.cricketmsf.api.ResponseCode;
import org.cricketmsf.api.Result;
import org.cricketmsf.api.ResultIface;
import org.slf4j.LoggerFactory;

/**
 *
 * @author greg
 */
public class TodoEventRouter {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TodoEventRouter.class);

    private final TodoService service;

    public TodoEventRouter(TodoService service) {
        this.service = service;
    }

    @EventHook(className = "mytodo.event.task.CreateTaskEvent")
    public ResultIface createTodo(CreateTaskEvent event) {
        logger.debug(event.toString());
        Result result = new Result();
        boolean ok = service.todoDao.createTask(event);
        if (ok) {
            result.setData("created");
        } else {
            result.setCode(ResponseCode.BAD_REQUEST);
            result.setData("error");
        }
        return result;
    }
    
    @EventHook(className = "mytodo.event.task.UpdateTaskEvent")
    public ResultIface updateTodo(UpdateTaskEvent event) {
        logger.debug(event.toString());
        Result result = new Result();
        boolean ok = service.todoDao.updateTask(event);
        if (ok) {
            result.setData("updated");
        } else {
            result.setCode(ResponseCode.BAD_REQUEST);
            result.setData("error");
        }
        return result;
    }

    @EventHook(className = "mytodo.event.task.ReadTaskEvent")
    public ResultIface readTodo(ReadTaskEvent event) {
        logger.debug(event.toString());
        HttpResult result = new HttpResult();
        Long id = event.getData();
        Task task=service.todoDao.readTask(id);
        if(null!=task){
            result.setData(task);
        }else{
            result.setCode(ResponseCode.NOT_FOUND);
        }
        return result;
    }

    @EventHook(className = "mytodo.event.task.DeleteTaskEvent")
    public ResultIface deleteTodo(DeleteTaskEvent event) {
        logger.debug(event.toString());
        Result result = new Result();
        Long id = (Long) event.getData();
        boolean ok = service.todoDao.deleteTask(id);
        if (ok) {
            result.setData("deleted");
        } else {
            result.setCode(ResponseCode.BAD_REQUEST);
            result.setData("error");
        }
        return result;
    }

}
