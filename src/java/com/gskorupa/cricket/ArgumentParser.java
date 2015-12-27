/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gskorupa.cricket;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class ArgumentParser {
    
    private Map<String, String> arguments;
    
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
        return (get("error")!=null || get("help")!=null);
    }

    public static Map<String, String> getArguments(String[] args) {
        HashMap<String, String> map = new HashMap();
        String name="";
        String option="";
        boolean confToRead = false;

        for (int i = 0; i < args.length; i++) {
            name = args[i].toLowerCase();
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
            }
            
        }
        return map;
    }

}
