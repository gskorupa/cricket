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

import org.cricketmsf.RequestObject;
import org.cricketmsf.Kernel;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Map;
import org.cricketmsf.in.InboundAdapter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.cricketmsf.Adapter;
import org.cricketmsf.util.Stopwatch;
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
    public Object handleInput(Object input) {
        if (input instanceof HttpExchange) {
            try {
                handle((HttpExchange) input);
            } catch (IOException ex) {
                logger.warn(ex.getMessage());
            }
        }
        return null;
    }

    @Override
    public synchronized void handle(HttpExchange exchange) throws IOException {
        long rootEventId = Kernel.getEventId();
        try {
            Stopwatch timer = null;
            if (Kernel.getInstance().isFineLevel()) {
                timer = new Stopwatch();
            }
            String acceptedResponseType = JSON;
            try {
                acceptedResponseType
                        = acceptedTypesMap.getOrDefault(exchange.getRequestHeaders().get("Accept").get(0), JSON);
            } catch (Exception e) {
            }
            // cerating Result object
            RequestObject requestObject = buildRequestObject(exchange, acceptedResponseType);
            if (null != properties.get("dump-request") && "true".equalsIgnoreCase(properties.get("dump-request"))) {
                logger.info(dumpRequest(requestObject));
            }

            Result result = createResponse(requestObject, rootEventId);

            acceptedResponseType = setResponseType(acceptedResponseType, result.getFileExtension());
            //set content type and print response to string format as JSON if needed
            Headers headers = exchange.getResponseHeaders();
            byte[] responseData;
            Iterator it = result.getHeaders().keySet().iterator();
            String key;
            while (it.hasNext()) {
                key = (String) it.next();
                List<String> values = result.getHeaders().get(key);
                for (int i = 0; i < values.size(); i++) {
                    headers.set(key, values.get(i));
                }
            }
            switch (result.getCode()) {
                case ResponseCode.MOVED_PERMANENTLY:
                case ResponseCode.MOVED_TEMPORARY:
                    if (!headers.containsKey("Location")) {
                        String newLocation = result.getMessage() != null ? result.getMessage() : "/";
                        headers.set("Location", newLocation);
                        responseData = ("moved to ".concat(newLocation)).getBytes("UTF-8");
                    } else {
                        responseData = "".getBytes();
                    }
                    break;
                case ResponseCode.NOT_FOUND:
                    headers.set("Content-type", "text/html");
                    responseData = result.getPayload();
                    break;
                default:
                    if (!headers.containsKey("Content-type")) {
                        if (acceptedTypesMap.containsKey(acceptedResponseType)) {
                            headers.set("Content-type", acceptedResponseType.concat("; charset=UTF-8"));
                            responseData = formatResponse(acceptedResponseType, result);
                        } else {
                            headers.set("Content-type", getMimeType(result.getFileExtension()));
                            responseData = result.getPayload();
                        }
                    } else {
                        String rContentType = headers.getFirst("Content-type");
                        headers.set("Content-type", rContentType.concat("; charset=UTF-8"));
                        if (acceptedTypesMap.containsKey(rContentType)) {
                            responseData = formatResponse(rContentType, result);
                        } else {
                            responseData = result.getPayload();
                        }
                    }
                    headers.set("Last-Modified", result.getModificationDateFormatted());
                    //TODO: get max age and no-cache info from the result object
                    if (result.getMaxAge() > 0) {
                        headers.set("Cache-Control", "max-age=" + result.getMaxAge());  // 1 hour
                    } else {
                        headers.set("Pragma", "no-cache");
                    }
                    if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                        CorsProcessor.getResponseHeaders(headers, exchange.getRequestHeaders(), Kernel.getInstance().getCorsHeaders());
                    } else if (exchange.getRequestURI().getPath().startsWith("/api/")) { //TODO: this is workaround
                        CorsProcessor.getResponseHeaders(headers, exchange.getRequestHeaders(), Kernel.getInstance().getCorsHeaders());
                    } else if (exchange.getRequestURI().getPath().endsWith(".tag")) { //TODO: this is workaround
                        CorsProcessor.getResponseHeaders(headers, exchange.getRequestHeaders(), Kernel.getInstance().getCorsHeaders());
                    }
                    if (result.getCode() == 0) {
                        result.setCode(ResponseCode.OK);
                    } else {
                        if (responseData.length == 0) {
                            if (result.getMessage() != null) {
                                responseData = result.getMessage().getBytes("UTF-8");
                            }
                        }
                    }
                    break;
            }

            if (Kernel.getInstance().isFineLevel()) {
                logger.debug("event " + rootEventId + " processing takes " + timer.time(TimeUnit.MILLISECONDS) + "ms");
            }

            if (responseData.length > 0) {
                exchange.sendResponseHeaders(result.getCode(), responseData.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseData);
                }
            } else {
                exchange.sendResponseHeaders(result.getCode(), -1);
            }
            //sendLogEvent(exchange, responseData.length);
            result = null;
        } catch (IOException e) {
            logger.warn(exchange.getRequestURI().getPath() + " " + e.getMessage());
        }
        exchange.close();
    }

    private String getMimeType(String fileExt) {
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

    public byte[] formatResponse(String type, Result result) {
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

    RequestObject buildRequestObject(HttpExchange exchange, String acceptedResponseType) {
        // Remember that "parameters" attribute is created by filter
        Map<String, Object> parameters = (Map<String, Object>) exchange.getAttribute("parameters");
        String method = exchange.getRequestMethod();
        String pathExt = exchange.getRequestURI().getPath();
        if (null != pathExt) {
            pathExt = pathExt.substring(exchange.getHttpContext().getPath().length());
            while (pathExt.startsWith("/")) {
                pathExt = pathExt.substring(1);
            }
        }

        RequestObject requestObject = new RequestObject();
        requestObject.method = method;
        requestObject.parameters = parameters;
        requestObject.uri = exchange.getRequestURI().toString();
        requestObject.pathExt = pathExt;
        requestObject.headers = exchange.getRequestHeaders();
        requestObject.clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
        requestObject.acceptedResponseType = acceptedResponseType;
        requestObject.body = (String) exchange.getAttribute("body");
        if (null == requestObject.body) {
            requestObject.body = "";
        }
        return requestObject;
    }

    protected abstract ProcedureCall preprocess(RequestObject request, long rootEventId);

    protected Result postprocess(Result result) {
        return result;
    }

    private Result createResponse(RequestObject requestObject, long rootEventId) {
        String methodName = null;
        Result result = new StandardResult();
        if (mode == WEBSITE_MODE) {
            if (!requestObject.uri.endsWith("/")) {
                if (requestObject.uri.lastIndexOf("/") > requestObject.uri.lastIndexOf(".")) {
                    // redirect to index.file but only if property index.file is not null
                    result.setCode(ResponseCode.MOVED_PERMANENTLY);
                    result.setMessage(requestObject.uri.concat("/"));
                    return result;
                }
            }
        }

        try {
            ProcedureCall pCall = preprocess(requestObject, Kernel.getEventId());
            if (pCall.requestHandled) { // request processed by the adapter
                if (pCall.responseCode < 100 || pCall.responseCode > 1000) {
                    result.setCode(ResponseCode.BAD_REQUEST);
                } else {
                    result.setCode(pCall.responseCode);
                }
                result.setData(pCall.response);
                result.setHeader("Content-type", pCall.contentType);
            } else { // pCall must be processed by the Kernel
                logger.debug("sending request to hook method " + pCall.procedureName + "@" + pCall.event.getClass().getSimpleName());
                result = (Result) Kernel.getInstance().getEventProcessingResult(
                        pCall.event,
                        pCall.procedureName
                );
                if (null != result) {
                    if (pCall.responseCode != 0) {
                        result.setCode(pCall.responseCode);
                    } else {
                        result = postprocess(result);
                        if (null!=result && (result.getCode() < 100 || result.getCode() > 1000)) {
                            result.setCode(ResponseCode.BAD_REQUEST);
                        }
                    }
                }
            }
        } catch (ClassCastException e) {
            logger.warn(e.getMessage());
            result.setCode(ResponseCode.INTERNAL_SERVER_ERROR);
            result.setMessage("handler method error");
            result.setFileExtension(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null == result) {
            result = new StandardResult("null result returned by the service");
            result.setCode(ResponseCode.INTERNAL_SERVER_ERROR);
        }
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

    public static String dumpRequest(RequestObject req) {
        StringBuilder sb = new StringBuilder();
        sb.append("************** REQUEST ****************").append("\r\n");
        sb.append("URI:").append(req.uri).append("\r\n");
        sb.append("PATHEXT:").append(req.pathExt).append("\r\n");
        sb.append("METHOD:").append(req.method).append("\r\n");
        sb.append("ACCEPT:").append(req.acceptedResponseType).append("\r\n");
        sb.append("CLIENT IP:").append(req.clientIp).append("\r\n");
        sb.append("***BODY:").append("\r\n");
        sb.append(req.body).append("\r\n");
        sb.append("***BODY.").append("\r\n");
        sb.append("***HEADERS:").append("\r\n");
        req.headers.keySet().forEach(key -> {
            sb.append(key)
                    .append(":")
                    .append(req.headers.getFirst(key))
                    .append("\r\n");
        });
        sb.append("***HEADERS.").append("\r\n");
        sb.append("***PARAMETERS:").append("\r\n");
        req.parameters.keySet().forEach(key -> {
            sb.append(key)
                    .append(":")
                    .append(req.parameters.get(key))
                    .append("\r\n");
        });
        sb.append("***PARAMETERS.").append("\r\n");
        return sb.toString();
    }

    @Override
    public final void addOperationConfig(Operation operation) {
        System.out.println(">>> "+getName()+" adding operation "+operation.getMethod()+" "+operation.getParameters().size());
        operations.put(operation.getMethod(), operation);
    }

    protected final ArrayList<Parameter> getParams(String method, boolean required, ParameterLocation location) {
        ArrayList<Parameter> params = new ArrayList();
        try {
            getOperations().get(method).getParameters().forEach(param -> {
                if (required == param.isRequired() && location==param.getIn()) {
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
