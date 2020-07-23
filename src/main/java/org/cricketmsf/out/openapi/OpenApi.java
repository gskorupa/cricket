package org.cricketmsf.out.openapi;

import com.cedarsoftware.util.io.JsonWriter;
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
    private Map<String, PathItem> paths = null;
    
    protected ProcedureCall preprocess(RequestObject request, long rootEventId) {
        // validation and translation 
        String method = request.method;
        if ("GET".equalsIgnoreCase(method)) {
            return new ProcedureCall(true, 200, "text/plain", toYaml());
        } else {
            return new ProcedureCall(HttpAdapter.SC_NOT_IMPLEMENTED,"method not implemented");
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
    public Map<String, PathItem> getPaths() {
        return paths;
    }

    /**
     * @param paths the paths to set
     */
    public void setPaths(Map<String, PathItem> paths) {
        this.paths = paths;
    }

    @Override
    public void init(Kernel service) {
        // info
        Info info = new Info();
        info.setTitle(service.getName());
        info.setDescription(service.getDescription());
        info.setTermsOfService("");
        setInfo(info);
        // paths
        HashMap<String, PathItem> paths = new HashMap<>();
        Iterator it = service.getAdaptersMap().values().iterator();
        Object ad;
        HttpAdapterIface hta;
        PathItem item;
        Operation operation;
        while(it.hasNext()){
            ad=it.next();
            if(ad instanceof HttpAdapterIface){
                hta=(HttpAdapterIface)ad;
                //System.out.println(hta.getProperty("context"));
                item=new PathItem();
                operation=hta.getOperations().get("get");
                if(null!=operation) item.setGet(operation);
                paths.put(hta.getProperty("context"), item);
            }
        }
        
        setPaths(paths);
    }

    @Override
    public String toJson() {
        return JsonWriter.objectToJson(this);
    }

    public String toYaml() {
        String indent = "";
        String lf = "\r\n";
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("openapi: \"").append(this.getOpenapi()).append("\"").append(lf);
        if (null != info) {
            sb.append("info:").append(lf);
            sb.append(getInfo().toYaml());
        }
        if(null!=paths){
            sb.append("paths:").append(lf);
            paths.keySet().forEach(key->{
                sb.append("  ").append(key).append(":").append(lf);
                sb.append(paths.get(key).toYaml());
            });
        }
        return sb.toString();
    }
}
