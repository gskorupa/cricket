package org.cricketmsf.in.openapi;

import com.cedarsoftware.util.io.JsonWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.cricketmsf.Adapter;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.event.ProcedureCall;
import org.cricketmsf.in.InboundAdapterIface;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.HttpAdapterIface;
import org.cricketmsf.in.http.HttpPortedAdapter;

/**
 *
 * @author greg
 */
public class OpenApi extends HttpPortedAdapter implements OpenApiIface, InboundAdapterIface, Adapter {

    private String openapi = "3.0.3";
    private Info info = null;
    private Map<String,Server> servers=null;
    private ArrayList<PathItem> paths = null;

    @Override
    protected ProcedureCall preprocess(RequestObject request, long rootEventId) {
        // validation and translation 
        String method = request.method;
        if ("GET".equalsIgnoreCase(method)) {
            return ProcedureCall.response(200, "text/plain", toYaml());
        } else {
            return ProcedureCall.response(HttpAdapter.SC_NOT_IMPLEMENTED, "method not implemented");
        }
    }

    /**
     * @return the openapi
     */
    public String getOpenapi() {
        return openapi;
    }

    /**
     * @param openapi the openapi to set
     */
    public void setOpenapi(String openapi) {
        this.openapi = openapi;
    }

    /**
     * @return the info
     */
    public Info getInfo() {
        return info;
    }

    /**
     * @param info the info to set
     */
    public void setInfo(Info info) {
        this.info = info;
    }

    /**
     * @return the paths
     */
    //public Map<String, PathItem> getPaths() {
    //    return paths;
    //}

    /**
     * @param pathMap the pathMap to set
     */
    public void setPaths(Map<String, PathItem> pathMap) {
        this.paths = new ArrayList<>();
        pathMap.values().forEach(item->{
            this.paths.add(item);
        });
        Collections.sort(paths);
    }

    @Override
    public void init(Kernel service) {
        // info
        Info info = new Info();
        info.setTitle(service.getName());
        info.setDescription(service.getDescription());
        info.setTermsOfService("");
        info.setVersion(properties.getOrDefault("version", "1.0.0"));
        setInfo(info);
        // servers
        servers=new HashMap<>();
        Server server=new Server((String)service.getProperties().getOrDefault("serviceurl", ""));
        if(!server.getUrl().isBlank()){
            servers.put(server.getUrl(), server);
        }
        // paths
        HashMap<String, PathItem> paths = new HashMap<>();
        Iterator it = service.getAdaptersMap().values().iterator();
        Object ad;
        HttpAdapterIface hta;
        PathItem item;
        Operation operation;
        while (it.hasNext()) {
            ad = it.next();
            if (ad instanceof HttpAdapterIface) {
                hta = (HttpAdapterIface) ad;
                item = new PathItem(hta.getProperty("context"));
                if (hta.getOperations().size()>=0) {
                    operation = hta.getOperations().get("get");
                    if (null != operation) {
                        item.setGet(operation);
                    }
                    paths.put(item.getPath(), item);
                }
            }
        }
        setPaths(paths);
    }

    @Override
    public String toJson() {
        return JsonWriter.objectToJson(this);
    }

    public String toYaml() {
        String myIndent = "";
        String indent = "  ";
        String lf = "\r\n";
        StringBuilder sb = new StringBuilder();
        sb.append("openapi: '").append(this.getOpenapi()).append("'").append(lf);
        if (null != info) {
            sb.append("info:").append(lf);
            sb.append(getInfo().toYaml(myIndent + indent));
        }
        if(servers.size()>0){
            sb.append("servers:").append(lf);
            servers.keySet().forEach(pathElement -> {
                sb.append(servers.get(pathElement).toYaml(myIndent + indent));
            });            
        }
        if (null != paths && paths.size()>0) {
            sb.append("paths:").append(lf);
            paths.forEach(pathElement -> {
                sb.append(indent).append(pathElement.getPath()).append(":").append(lf);
                sb.append(pathElement.toYaml());
            });
        }
        return sb.toString();
    }
}
