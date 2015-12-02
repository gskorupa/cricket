/*
 * Copyright 2015 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package com.gskorupa.cricket.example;

import com.gskorupa.cricket.Adapter;
import java.util.Properties;

/**
 *
 * @author greg
 */
public class SimpleStorageStandard implements SimpleStorage, Adapter{
    
    String myParam1=null;
    
    public void storeData(){
        System.out.println("Storing data with param1="+myParam1);
    }
    
    public void loadProperties(Properties properties){
        myParam1=properties.getProperty("SimpleStorage-param1");
        System.out.println("param1="+myParam1);
    }
    
    public String getContext(){
        return null;
    }
    
}
