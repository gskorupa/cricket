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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class ArgumentParser {
    
    private Map<String, String> arguments;
    
    /**
     * Default constructor
     */
    public ArgumentParser(){
        arguments=new HashMap<String,String>();
    }
    
    /**
     * Creates ArgumentParser based on command line arguments
     * 
     * @param args  command line arguments
     */
    public ArgumentParser(String[] args){
        arguments=getArguments(args);
    }
    
    public boolean containsKey(String key){
        return arguments.containsKey(key);
    }
    
    public String get(String key){
        return arguments.get(key);
    }
    
    public boolean isProblem(){
        return (get("error")!=null);
    }

    /**
     * Parse command line arguments 
     * 
     * @param args  array of command line arguments
     * @return  map of argument values related to argument names
     */
    public static Map<String, String> getArguments(String[] args) {
        HashMap<String, String> map = new HashMap<String,String>();
        String name="";
        String option="";
        boolean confToRead = false;
        if(args==null){
            return map;
        }
        for (int i = 0; i < args.length; i++) {
            name = args[i];
            if (confToRead) {
                if(name.startsWith("-")){
                    map.put("error", "option "+option+" must be followed by value");
                }else{
                    map.put(option, name);
                    confToRead=false;
                    option="";
                }
            } else {
                switch (name) {
                    case "--help":
                    case "-h":
                        map.put("help", "");
                        break;
                    case "--run":
                    case "-r":
                        map.put("run", "*");
                        break;
                    case "--config":
                    case "-c":
                        confToRead = true;
                        option="config";
                        break;
                    case "--service":
                    case "-s":
                        confToRead = true;
                        option="service";
                        break;
                    default:
                        map.put("error", "unknown option " + name);
                }
            }
        }
        if(confToRead){
            switch(option){
                case "config":
                    map.put("error", "-c or --config option requires an argument");
                    break;
                case "service":
                    map.put("error", "-s or --service option requires an argument");
                    break;
            }
            
        }
        return map;
    }

}
