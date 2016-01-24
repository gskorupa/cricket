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
package com.gskorupa.cricket;

import com.cedarsoftware.util.io.JsonReader;
import com.gskorupa.cricket.config.ConfigSet;
import com.gskorupa.cricket.config.Configuration;
import com.gskorupa.cricket.in.Scheduler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * EchoService
 *
 * @author greg
 */
public class Runner {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        final Kernel service;
        final ConfigSet configSet;
        Runner runner = new Runner();

        ArgumentParser arguments = new ArgumentParser(args);
        if (arguments.isProblem()) {
            if (arguments.containsKey("error")) {
                System.out.println(arguments.get("error"));
            }
            System.out.println(runner.getHelp());
            System.exit(-1);
        }

        configSet = runner.readConfig(arguments);

        Class serviceClass = null;
        String serviceName;
        Configuration configuration = null;
        if (arguments.containsKey("service")) {
            // if service name provided as command line option
            serviceName = arguments.get("service");
        } else {
            // otherwise get first configured service
            serviceName = configSet.getDefault().getService();
        }

        configuration = configSet.getConfiguration(serviceName);
        
        // if serviceName isn't configured print error and exit
        if(configuration==null){
            System.out.println("Configuration not found for "+serviceName);
            System.exit(-1);
        }
        
        if(arguments.containsKey("help")){
            System.out.println(runner.getHelp(serviceName));
            System.exit(0);
        }

        try {
            serviceClass = Class.forName(serviceName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.out.println("RUNNER");
        try {
            service = (Kernel) Kernel.getInstanceWithProperties(serviceClass, configuration);
            service.configSet=configSet;
            if (arguments.containsKey("run")) {
                service.start();
            } else {
                //service.setScheduler(new Scheduler());
                System.out.println("Executing runOnce method");
                service.runOnce();
                service.shutdown();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String getHelp(){
        return getHelp(null);
    }

    public String getHelp(String serviceName) {
        String content = "Help file not found";
        String helpFileName="/help.txt";
        if(null!=serviceName){
            helpFileName="/"+serviceName.substring(serviceName.lastIndexOf(".")+1)+"-help.txt";
        }
        try {
            content = readHelpFile(helpFileName);
        } catch (Exception e) {
            try {
                content = readHelpFile("/help.txt");
            } catch (Exception x) {
                e.printStackTrace();
            }
        }
        return content;
    }

    public String readHelpFile(String fileName) throws Exception {
        String content = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(fileName)))) {
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
                out.append("\r\n");
            }
            content = out.toString();
        }
        return content;
    }

    public ConfigSet readConfig(ArgumentParser arguments) {
        ConfigSet cs=null;
        if (arguments.containsKey("config")) {
            //Properties props;
            Map args = new HashMap();
            args.put(JsonReader.USE_MAPS, false);
            Map types = new HashMap();
            types.put("java.utils.HashMap", "adapters");
            types.put("java.utils.HashMap", "properties");
            args.put(JsonReader.TYPE_NAME_MAP, types);
            try {
                InputStream propertyFile = new FileInputStream(new File(arguments.get("config")));
                String inputStreamString = new Scanner(propertyFile, "UTF-8").useDelimiter("\\A").next();
                cs = (ConfigSet) JsonReader.jsonToJava(inputStreamString, args);
            } catch (Exception e) {
                e.printStackTrace();
                //LOGGER.log(Level.SEVERE, "Adapters initialization error. Configuration: {0}", path);
            }
        } else {
            String propsName = "cricket.json";
            InputStream propertyFile = getClass().getClassLoader().getResourceAsStream(propsName);
            String inputStreamString = new Scanner(propertyFile, "UTF-8").useDelimiter("\\A").next();
            Map args = new HashMap();
            args.put(JsonReader.USE_MAPS, false);
            Map types = new HashMap();
            types.put("java.utils.HashMap", "adapters");
            types.put("java.utils.HashMap", "properties");
            args.put(JsonReader.TYPE_NAME_MAP, types);
            cs = (ConfigSet) JsonReader.jsonToJava(inputStreamString, args);
        }
        return cs;
    }
}
