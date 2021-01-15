/*
 * Copyright 2017 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package org.cricketmsf.services;

import java.util.HashMap;
import org.cricketmsf.event.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.event.GreeterEvent;
import org.cricketmsf.event.HttpEvent;
import org.cricketmsf.exception.InitException;
import org.cricketmsf.api.ResponseCode;
import org.cricketmsf.api.StandardResult;
import org.cricketmsf.in.openapi.OpenApiIface;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.KeyValueDBIface;
import org.cricketmsf.out.file.FileReaderAdapterIface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cricketmsf.annotation.EventHook;
import org.cricketmsf.event.Procedures;
import org.cricketmsf.api.Result;
import org.cricketmsf.api.ResultIface;

/**
 * This is example service.
 *
 * @author greg
 */
public class BasicService extends Kernel {
    private static final Logger logger = LoggerFactory.getLogger(BasicService.class);

    // outbound adapters
    KeyValueDBIface cacheDB = null;
    FileReaderAdapterIface wwwFileReader = null;
    // other adapters whose methods must be available
    OpenApiIface apiGenerator = null;

    public BasicService() {
        super();
        this.configurationBaseName = "BasicService";
    }

    @Override
    public void getAdapters() {
        cacheDB = (KeyValueDBIface) getRegistered("CacheDB");
        wwwFileReader = (FileReaderAdapterIface) getRegistered("WwwFileReader");
        apiGenerator = (OpenApiIface) getRegistered("OpenApi");
    }

    @Override
    public void runInitTasks() {
        try {
            super.runInitTasks();
        } catch (InitException ex) {
            ex.printStackTrace();
            shutdown();
        }
        try {
            cacheDB.addTable("webcache", 100, false);
        } catch (NullPointerException|KeyValueDBException e) {
        }
        apiGenerator.init(this);
        setInitialized(true);
    }

    @Override
    public void runFinalTasks() {
        //System.out.println(printStatus());
    }

    /**
     *
     * @param event
     * @return ParameterMapResult with the file content as a byte array
     */
    @EventHook(className = "org.cricketmsf.event.HttpEvent", procedure = Procedures.WWW)
    public ResultIface doGet(HttpEvent event) {
        return wwwFileReader.getFile(
                (RequestObject)event.getData(), 
                cacheDB, 
                "webcache"
        );
    }

    @EventHook(className = "org.cricketmsf.event.Event", procedure = Procedures.SYSTEM_STATUS)
    public ResultIface handleStatusRequest(Event requestEvent) {
        return new Result(reportStatus());
    }

    @EventHook(className = "org.cricketmsf.event.GreeterEvent", procedure = Procedures.GREET)
    public ResultIface doGreet(GreeterEvent event) {
        String name =  ((HashMap<String,String>)event.getData()).get("name");
        ResultIface result = new Result("Hello " + name);
        result.setProcedure(Procedures.GREET);
        return result;
    }
    
    @EventHook(className = "org.cricketmsf.event.Event", procedure = Procedures.PRINT_INFO)
    public Result printInfo(Event event) {
        logger.info("INFO {} {} {}",getProceduresDictionary().getName(event.getProcedure()), event.getInitialTimePoint(), event.getData());
        return null;
    }

    public Object sendEcho(RequestObject request) {
        StandardResult r = new StandardResult();
        r.setCode(ResponseCode.OK);
        //if (!echoAdapter.isSilent()) {
        HashMap<String, Object> data = new HashMap<>(request.parameters);
        data.put("&_service.id", getId());
        data.put("&_service.uuid", getUuid().toString());
        data.put("&_service.name", getName());
        data.put("&_request.method", request.method);
        data.put("&_request.pathExt", request.pathExt);
        data.put("&_request.body", request.body);
        if (data.containsKey("error")) {
            int errCode = ResponseCode.INTERNAL_SERVER_ERROR;
            try {
                errCode = Integer.parseInt((String) data.get("error"));
            } catch (Exception e) {
            }
            r.setCode(errCode);
            data.put("error", "error forced by request");
        }
        r.setData(data);
        r.setHeader("x-echo-greeting", "hello");
        return r;
    }

}
