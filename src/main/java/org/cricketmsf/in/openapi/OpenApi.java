/*
 * Copyright 2020 Grzegorz Skorupa <g.skorupa at gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.cricketmsf.in.http.HttpAdapterIface;
import org.cricketmsf.in.http.HttpPortedAdapter;
import org.cricketmsf.api.ResponseCode;

/**
 *
 * @author greg
 */
public class OpenApi extends HttpPortedAdapter implements OpenApiIface, InboundAdapterIface, Adapter {

    private String openapi = "3.0.3";
    private Info info = null;
    private Map<String, Server> servers = null;
    private ArrayList<PathItem> paths = null;

    @Override
    protected ProcedureCall preprocess(RequestObject request) {
        // validation and translation 
        String method = request.method;
        if ("GET".equalsIgnoreCase(method)) {
            return ProcedureCall.toRespond(200, "application/vnd.oai.openapi", toYaml());
        } else {
            return ProcedureCall.toRespond(ResponseCode.NOT_IMPLEMENTED, "method not implemented");
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
     * @param pathMap the pathMap to set
     */
    public void setPaths(Map<String, PathItem> pathMap) {
        this.paths = new ArrayList<>();
        pathMap.values().forEach(item -> {
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
        info.setTermsOfService((String) service.getProperties().getOrDefault("terms", ""));
        info.setVersion(properties.getOrDefault("version", "1.0.0"));
        setInfo(info);
        // servers
        servers = new HashMap<>();
        Server server = new Server((String) service.getProperties().getOrDefault("serviceurl", ""));
        if (!server.getUrl().isBlank()) {
            servers.put(server.getUrl(), server);
        }
        // pathsMap
        String[] methods = {"GET", "POST", "PUT", "PATCH", "OPTIONS", "HEAD", "CONNECT", "TRACE", "DELETE"};
        HashMap<String, PathItem> pathsMap = new HashMap<>();
        Iterator it = service.getAdaptersMap().values().iterator();
        Object ad;
        HttpAdapterIface hta;
        //HashMap<String, PathItem> paths = new HashMap<>();
        PathItem pathItem;
        Operation operation;
        while (it.hasNext()) {
            ad = it.next();
            if (ad instanceof HttpAdapterIface) {
                hta = (HttpAdapterIface) ad;
                pathItem = new PathItem(hta.getProperty("context"));
                if (hta.getOperations().size() > 0) {
                    for (String methodName : methods) {
                        operation = hta.getOperations().get(methodName);
                        if (null != operation) {
                            pathItem = pathsMap.get(hta.getProperty("context") + operation.getPathModifier());
                            if (null == pathItem) {
                                pathItem = new PathItem(hta.getProperty("context"));
                            }
                            pathItem.setOperation(operation);
                            pathsMap.put(pathItem.getPath(), pathItem);
                        }
                    }
                    pathsMap.put(pathItem.getPath(), pathItem);
                }
            }
        }
        setPaths(pathsMap);
    }

    @Override
    public String toJson() {
        return JsonWriter.objectToJson(this);
    }

    public String toYaml() {
        String myIndent = "";
        String indentStep = "  ";
        String lf = "\r\n";
        StringBuilder sb = new StringBuilder();
        sb.append("openapi: \"").append(this.getOpenapi()).append("\"").append(lf);
        if (null != info) {
            sb.append("info:").append(lf);
            sb.append(getInfo().toYaml(myIndent + indentStep));
        }
        if (servers.size() > 0) {
            sb.append("servers:").append(lf);
            servers.keySet().forEach(pathElement -> {
                sb.append(servers.get(pathElement).toYaml(myIndent + indentStep));
            });
        }
        if (null != paths && paths.size() > 0) {
            sb.append("paths:").append(lf);
            paths.forEach(pathItem -> {
                sb.append(indentStep).append(pathItem.getPath()).append(":").append(lf);
                sb.append(pathItem.toYaml(myIndent + indentStep + indentStep));
            });
        }
        return sb.toString();
    }

    /**
     * Defines API of this addapter
     */
    @Override
    public void defineApi() {
        Operation getOp = new Operation("GET")
                .description("get the service API specification as OpenAPI 3.0 YAML")
                .tag("api")
                .summary("get the service API specification")
                .response(new Response("200").content("application/vnd.oai.openapi").description("API specification file"));
        addOperationConfig(getOp);
    }
}
