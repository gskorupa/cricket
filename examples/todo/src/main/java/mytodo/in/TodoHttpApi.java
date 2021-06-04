package mytodo.in;

import java.util.HashMap;
import mytodo.event.task.CreateTaskEvent;
import mytodo.event.task.ReadTaskEvent;
import org.cricketmsf.Adapter;
import org.cricketmsf.RequestObject;
import org.cricketmsf.api.ResponseCode;
import org.cricketmsf.event.ProcedureCall;
import org.cricketmsf.in.http.HttpPortedAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TodoHttpApi extends HttpPortedAdapter implements Adapter {

    private static final Logger logger = LoggerFactory.getLogger(TodoHttpApi.class);

    public TodoHttpApi() {
        super();
    }

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        setContext(properties.get("context"));
    }

    protected ProcedureCall preprocess(RequestObject request) {
        // validation and translation 
        String method = request.method;
        if ("POST".equalsIgnoreCase(method)) {
            return preprocessPost(request);
        } else if ("GET".equalsIgnoreCase(method)) {
            return preprocessGet(request);
        } else if ("OPTIONS".equalsIgnoreCase(method)) {
            return ProcedureCall.toRespond(200, "OK");
        } else {
            return ProcedureCall.toRespond(ResponseCode.METHOD_NOT_ALLOWED, "error");
        }
    }

    private ProcedureCall preprocessPost(RequestObject request) {
        String errorMessage = "";
        ProcedureCall result;

        HashMap<String, Object> data = new HashMap<>();
        data.put("title", (String) request.parameters.getOrDefault("title", ""));

        if (errorMessage.isEmpty()) {
            CreateTaskEvent ev = new CreateTaskEvent();
            ev.setData(data);
            result = ProcedureCall.toForward(ev);
        } else {
            result = ProcedureCall.toRespond(400, errorMessage);
        }
        return result;
    }

    private ProcedureCall preprocessGet(RequestObject request) {
        String errorMessage = "";
        ProcedureCall result;
        Long id = null;

        try {
            id = Long.parseLong((String) request.parameters.get("id"));
        } catch (NullPointerException | NumberFormatException ex) {
            errorMessage = ex.getMessage();
        }

        if (errorMessage.isEmpty()) {
            ReadTaskEvent ev = new ReadTaskEvent();
            ev.setRootId(request.rootEventId);
            ev.setData(id);
            result = ProcedureCall.toForward(ev);
        } else {
            result = ProcedureCall.toRespond(400, errorMessage);
        }
        return result;
    }

}
