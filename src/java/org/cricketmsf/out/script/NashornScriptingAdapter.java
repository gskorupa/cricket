/*
 * Copyright 2016 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package org.cricketmsf.out.script;

import java.io.BufferedInputStream;
import org.cricketmsf.Adapter;
import org.cricketmsf.out.OutboundAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.ParameterMapResult;
import org.cricketmsf.in.http.StandardResult;

/**
 *
 * @author greg
 */
public class NashornScriptingAdapter extends OutboundAdapter implements Adapter, ScriptingAdapterIface {

    private String scriptLocation = null;
    private String script = null;
    private Invocable invocable;
    ScriptEngineManager manager;
    ScriptEngine engine;

    /**
     * This method is executed while adapter is instantiated during the service
     * start. It's used to configure the adapter according to the configuration.
     *
     * @param properties map of properties readed from the configuration file
     * @param adapterName name of the adapter set in the configuration file (can
     * be different from the interface and class name.
     */
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        scriptLocation = properties.get("script-file");
        System.out.println("script location: " + scriptLocation);
        script = readScript(scriptLocation);      
        if (script != null) {
            manager = new ScriptEngineManager();
            engine = manager.getEngineByName("nashorn");
            try {
                engine.eval(script);
                invocable = (Invocable) engine;
                System.out.println("script OK");
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("script not found");
        }
    }

    /**
     * Reads script from file
     *
     * @param path the file location
     * @return script content
     */
    @Override
    public String readScript(String path) {
        File file = new File(path);
        byte[] result = new byte[(int) file.length()];
        InputStream input = null;
        try {
            int totalBytesRead = 0;
            input = new BufferedInputStream(new FileInputStream(file));
            while (totalBytesRead < result.length) {
                int bytesRemaining = result.length - totalBytesRead;
                //input.read() returns -1, 0, or more :
                int bytesRead = input.read(result, totalBytesRead, bytesRemaining);
                if (bytesRead > 0) {
                    totalBytesRead = totalBytesRead + bytesRead;
                }
            }
            /*
         the above style is a bit tricky: it places bytes into the 'result' array; 
         'result' is an output parameter;
         the while loop usually has a single iteration only.
             */

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                }
            }
        }
        return new String(result);
    }

    @Override
    public StandardResult processRequest(RequestObject request) {
        StandardResult result = new StandardResult();
        //result.setData(new HashMap(request.parameters));

        Kernel.getInstance().handleEvent(Event.logFinest("NashornScriptAdapter", "evaluating"));
        
        String scriptResult = "";
        try {
            if (script != null && invocable!=null) {
                Object objectResult = invocable.invokeFunction("processRequest", request.method, request.pathExt);
                //System.out.println("result="+objectResult);
                //System.out.println(objectResult.getClass().getName());
                scriptResult = objectResult.toString();
            } else {
                result.setCode(HttpAdapter.SC_NOT_IMPLEMENTED);
            }
            result.setPayload(scriptResult.getBytes());
            result.setFileExtension(null);
            result.setCode(HttpAdapter.SC_OK);
            result.setModificationDate(new Date());
            result.setMessage("");
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(HttpAdapter.SC_NOT_IMPLEMENTED);
            result.setMessage(e.getMessage());
        }

        return result;
    }

}
