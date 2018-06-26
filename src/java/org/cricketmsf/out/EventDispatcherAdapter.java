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
package org.cricketmsf.out;

import java.util.HashMap;
import org.cricketmsf.Event;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class EventDispatcherAdapter extends OutboundAdapter implements OutboundAdapterIface, DispatcherIface{
    
    //protected HashMap<String,String> statusMap=null;
    //protected HashMap<String, String> properties;
    
    public EventDispatcherAdapter(){
    }
    
    @Override
    public void dispatch(Event event) throws DispatcherException{
        throw new DispatcherException(DispatcherException.NOT_IMPLEMENTED);
    } 
    
    @Override
    public void destroy(){   
    }
    
    @Override
    public void loadProperties(HashMap<String,String> properties, String adapterName){
        super.loadProperties(properties, adapterName);
        //this.properties = (HashMap<String,String>)properties.clone();        
        //getStatus(adapterName); //required if we need to overwrite updateStatusItem() method
    }

    @Override
    public void clearEventsMap() {        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void registerEventType(String category, String type) throws DispatcherException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
