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
package org.cricketmsf.in.http;

import org.cricketmsf.api.ResultIface;
import org.cricketmsf.RequestObject;
import org.cricketmsf.Kernel;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Map;
import org.cricketmsf.in.InboundAdapter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import org.cricketmsf.Adapter;
import org.cricketmsf.event.ProcedureCall;
import org.cricketmsf.in.InboundAdapterIface;
import org.cricketmsf.in.openapi.Operation;
import org.cricketmsf.in.openapi.Parameter;
import org.cricketmsf.in.openapi.ParameterLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public abstract class HttpPortedAdapter
        extends InboundAdapter
        implements Adapter, HttpAdapterIface, HttpHandler, InboundAdapterIface/*, org.eclipse.jetty.server.Handler*/ {

    private static final Logger logger = LoggerFactory.getLogger(HttpPortedAdapter.class);

    public final static String JSON = "application/json";
    public final static String XML = "text/xml";
    public final static String CSV = "text/csv";
    public final static String HTML = "text/html";
    public final static String TEXT = "text/plain";

    public final static int SERVICE_MODE = 0;
    public final static int WEBSITE_MODE = 1;

    private final String[] acceptedTypes = {
        "application/json",
        "text/xml",
        "text/html",
        "text/csv",
        "text/plain"
    };
    protected HashMap<String, String> acceptedTypesMap;

    private String context;
    private boolean extendedResponse = false;
    SimpleDateFormat dateFormat;

    protected int mode = SERVICE_MODE;

    protected HashMap<String, Operation> operations = new HashMap();

    public HttpPortedAdapter() {
        super();
        try {
            acceptedTypesMap = new HashMap<>();
            for (String acceptedType : acceptedTypes) {
                acceptedTypesMap.put(acceptedType, acceptedType);
            }
            dateFormat = Kernel.getInstance().dateFormat;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        setContext(properties.get("context"));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        new Thread(
                new HttpRequestWorker(exchange, this)
        ).start();
    }

    protected String getMimeType(String fileExt) {
        if (null == fileExt) {
            return TEXT;
        }
        switch (fileExt.toLowerCase()) {
            case ".ico":
                return "image/x-icon";
            case ".jpg":
                return "image/jpg";
            case ".jpeg":
                return "image/jpeg";
            case ".gif":
                return "image/gif";
            case ".png":
                return "image/png";
            case ".css":
                return "text/css";
            case ".csv":
                return "text/csv";
            case ".js":
                return "text/javascript";
            case ".svg":
                return "image/svg+xml";
            case ".htm":
            case ".html":
                return "text/html; charset=utf-8";
            case ".json":
                return JSON;
            default:
                return TEXT;
        }
    }

    /**
     * Calculates response type based on the file type
     *
     * @param acceptedResponseType
     * @param fileExt
     * @return response type
     */
    protected String setResponseType(String acceptedResponseType, String fileExt) {
        //return fileExt != null ? expectedResponseType : NONE;
        return acceptedResponseType;
    }

    public byte[] formatResponse(String type, ResultIface result) {
        byte[] r = {};
        String formattedResponse;
        switch (type) {
            case JSON:
                formattedResponse = JsonFormatter.getInstance().format(true, isExtendedResponse() ? result : result.getData());
                break;
            case XML:
                //TODO: extended response is not possible because of "java.util.List is an interface, and JAXB can't handle interfaces"
                formattedResponse = XmlFormatter.getInstance().format(true, result.getData());
                break;
            case CSV:
                // formats only Result.getData() object
                formattedResponse = CsvFormatter.getInstance().format(result);
                break;
            case TEXT:
                // formats only Result.getData() object
                formattedResponse = TxtFormatter.getInstance().format(result);
                break;
            default:
                formattedResponse = JsonFormatter.getInstance().format(true, result);
                break;
        }

        try {
            r = formattedResponse.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage());
        }
        return r;
    }

    protected abstract ProcedureCall preprocess(RequestObject request, long rootEventId);

    protected ResultIface postprocess(ResultIface result) {
        return result;
    }


    /**
     * @param context the context to set
     */
    public void setContext(String context) {
        this.context = context;
    }

    public String getContext() {
        return context;
    }

    @Override
    public void addHookMethodNameForMethod(String requestMethod, String hookMethodName) {
        hookMethodNames.put(requestMethod, hookMethodName);
    }

    @Override
    public String getHookMethodNameForMethod(String requestMethod) {
        String result = null;
        if ("HEAD".equalsIgnoreCase(requestMethod)) {
            result = hookMethodNames.get("GET");
        } else {
            result = hookMethodNames.get(requestMethod);
        }
        if (null == result) {
            result = hookMethodNames.get("*");
        }
        return result;
    }

    /**
     * @return the extendedResponse
     */
    public boolean isExtendedResponse() {
        return extendedResponse;
    }

    /**
     * @param paramValue
     */
    public void setExtendedResponse(String paramValue) {
        this.extendedResponse = !("false".equalsIgnoreCase(paramValue));
    }

    public void setDateFormat(String dateFormat) {
        if (dateFormat != null) {
            this.dateFormat = new SimpleDateFormat(dateFormat);
        }
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }
    
    @Override
    public final void addOperationConfig(Operation operation) {
        logger.debug(">>> {} adding operation {} with {} params", getName(), operation.getMethod(), operation.getParameters().size());
        operations.put(operation.getMethod(), operation);
    }

    protected final ArrayList<Parameter> getParams(String method, boolean required, ParameterLocation location) {
        ArrayList<Parameter> params = new ArrayList();
        try {
            getOperations().get(method).getParameters().forEach(param -> {
                if (required == param.isRequired() && location == param.getIn()) {
                    params.add(param);
                }
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return params;
    }

    protected int getParamIndex(ArrayList<Parameter> params, String name) {
        for (int i = 0; i < params.size(); i++) {
            if (params.get(i).getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * This method is called while generating OpenAPI specification for the
     * adapter.
     *
     * @return map of declared operations
     */
    @Override
    public final Map<String, Operation> getOperations() {
        return operations;
    }

    /**
     * Can be overriden to provide OpenAPI specification of the adapter class.
     */
    @Override
    public void defineApi() {
    }


}
