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
package org.cricketmsf;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import org.cricketmsf.config.ConfigSet;
import org.cricketmsf.config.Configuration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

/**
 * Runner class is used when running JAR distribution. The class parses the
 * command line arguments, reads config from cricket.json, then creates and runs
 * the service instance according to the configuration.
 *
 * @author greg
 */
public class Runner {
    
    public static Kernel getService(String[] args){
        long runAt = System.currentTimeMillis();
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
        String serviceId;
        String serviceName = null;
        Configuration configuration = null;
        if (arguments.containsKey("service")) {
            // if service name provided as command line option
            serviceId = (String) arguments.get("service");
        } else {
            // otherwise get first configured service
            serviceId = configSet.getDefault().getId();
        }

        configuration = configSet.getConfigurationById(serviceId);

        // if serviceName isn't configured print error and exit
        if (configuration == null) {
            System.out.println("Configuration not found for id=" + serviceId);
            System.exit(-1);
        } else if (arguments.containsKey("lift")) {
            serviceName = (String) arguments.get("lift");
            System.out.println("LIFT service " + serviceName);
        } else {
            serviceName = configuration.getService();
        }

        if (arguments.containsKey("help")) {
            System.out.println(runner.getHelp(serviceName));
            System.exit(0);
        }

        if (arguments.containsKey("print")) {
            System.out.println(runner.getConfigAsString(configSet));
            System.exit(0);
        }

        try {
            serviceClass = Class.forName(serviceName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.out.println("CRICKET RUNNER");
        try {
            service = (Kernel) Kernel.getInstanceWithProperties(serviceClass, configuration);
            service.configSet = configSet;
            service.liftMode = arguments.containsKey("lift");
            if (arguments.containsKey("run")) {
                service.setStartedAt(runAt);
                service.start();
                return service;
            } else {
                //service.setScheduler(new Scheduler());
                //System.out.println("Executing runOnce method");
                service.runOnce();
                service.shutdown();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        getService(args);
        /*
        long runAt = System.currentTimeMillis();
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
        String serviceId;
        String serviceName = null;
        Configuration configuration = null;
        if (arguments.containsKey("service")) {
            // if service name provided as command line option
            serviceId = (String) arguments.get("service");
        } else {
            // otherwise get first configured service
            serviceId = configSet.getDefault().getId();
        }

        configuration = configSet.getConfigurationById(serviceId);

        // if serviceName isn't configured print error and exit
        if (configuration == null) {
            System.out.println("Configuration not found for id=" + serviceId);
            System.exit(-1);
        } else if (arguments.containsKey("lift")) {
            serviceName = (String) arguments.get("lift");
            System.out.println("LIFT service " + serviceName);
        } else {
            serviceName = configuration.getService();
        }

        if (arguments.containsKey("help")) {
            System.out.println(runner.getHelp(serviceName));
            System.exit(0);
        }

        if (arguments.containsKey("print")) {
            System.out.println(runner.getConfigAsString(configSet));
            System.exit(0);
        }

        try {
            serviceClass = Class.forName(serviceName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.out.println("CRICKET RUNNER");
        try {
            service = (Kernel) Kernel.getInstanceWithProperties(serviceClass, configuration);
            service.configSet = configSet;
            service.liftMode = arguments.containsKey("lift");
            if (arguments.containsKey("run")) {
                service.setStartedAt(runAt);
                service.start();
            } else {
                //service.setScheduler(new Scheduler());
                //System.out.println("Executing runOnce method");
                service.runOnce();
                service.shutdown();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }

    /**
     * Returns content of the default help file ("help.txt")
     *
     * @return help content
     */
    public String getHelp() {
        return getHelp(null);
    }

    /**
     * Returns content of the custom service help file (serviceName+"-help.txt")
     * Example: if your service class name is
     * org.cricketmsf.services.EchoService then help file is
     * EchoService-help.txt
     *
     * @param serviceName the service class simple name
     * @return
     */
    public String getHelp(String serviceName) {
        String content = "Help file not found";
        String helpFileName = "/help.txt";
        if (null != serviceName) {
            helpFileName = "/" + serviceName.substring(serviceName.lastIndexOf(".") + 1) + "-help.txt";
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

    private String readHelpFile(String fileName) throws Exception {
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
        ConfigSet configSet = null;
        if (arguments.containsKey("config")) {
            //Properties props;
            Map args = new HashMap();
            args.put(JsonReader.USE_MAPS, false);
            Map types = new HashMap();
            types.put("java.utils.HashMap", "adapters");
            types.put("java.utils.HashMap", "properties");
            args.put(JsonReader.TYPE_NAME_MAP, types);
            try {
                InputStream propertyFile = new FileInputStream(new File((String) arguments.get("config")));
                String inputStreamString = new Scanner(propertyFile, "UTF-8").useDelimiter("\\A").next();
                configSet = (ConfigSet) JsonReader.jsonToJava(inputStreamString, args);
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
            configSet = (ConfigSet) JsonReader.jsonToJava(inputStreamString, args);
        }
        // read Kernel version
        Properties prop = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("cricket.properties")) {
            if (inputStream != null) {
                prop.load(inputStream);
            }
        } catch (IOException e) {
        }
        configSet.setKernelVersion(prop.getProperty("version", "unknown"));
        // force property changes based on command line --force param
        if (arguments.containsKey("force")) {
            ArrayList<String> forcedProps = (ArrayList) arguments.get("force");
            for (int i = 0; i < forcedProps.size(); i++) {
                configSet.forceProperty(forcedProps.get(i));
            }
        }

        return configSet;
    }

    public String getConfigAsString(ConfigSet c) {
        Map nameBlackList = new HashMap();
        ArrayList blackList = new ArrayList();
        blackList.add("host");
        blackList.add("port");
        blackList.add("threads");
        blackList.add("filter");
        blackList.add("cors");
        nameBlackList.put(Configuration.class, blackList);
        Map args = new HashMap();
        args.put(JsonWriter.PRETTY_PRINT, true);
        //args.put(JsonWriter., args)
        return JsonWriter.objectToJson(c, args);
    }
}
