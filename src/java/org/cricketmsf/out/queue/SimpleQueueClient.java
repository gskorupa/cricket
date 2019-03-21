/*
 * Copyright 2019 Grzegorz Skorupa <g.skorupa at gmail.com>.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.cricketmsf.out.queue;

import java.util.HashMap;
import org.cricketmsf.Adapter;
import org.cricketmsf.Kernel;
import org.cricketmsf.exception.QueueException;
import org.cricketmsf.out.OutboundAdapter;

/**
 *
 * @author greg
 */
public class SimpleQueueClient extends OutboundAdapter implements QueueClientIface, Adapter {
    
    private QueueIface queue = null;
    String queueAdapterName=null;

    private QueueIface getQueue() throws QueueException {
        if (null == queue) {
            try {
                queue = (QueueIface) Kernel.getInstance().getAdaptersMap().get(queueAdapterName);
            } catch (Exception e) {
            }
        }
        if(null == queue){
            throw new QueueException(QueueException.QUEUE_NOT_DEFINED);
        }
        return queue;
    }
    

    @Override
    public void publish(String channel, String key, Object value) throws QueueException {
        getQueue().add(channel, key, value);
    }

    @Override
    public void publish(String channel, Object value) throws QueueException {
        getQueue().push(channel, value);
    }

    @Override
    public Object getCopy(String channel, String key) throws QueueException {
        return getQueue().show(channel, key);
    }
    
    @Override
    public Object getCopy(String channel) throws QueueException {
        return getQueue().show(channel);
    }

    @Override
    public Object get(String channel, String key) throws QueueException {
        return getQueue().get(channel, key);
    }

    @Override
    public void push(String channel, Object value) throws QueueException {
        getQueue().push(channel, value);
    }

    @Override
    public Object pop(String channel) throws QueueException {
        return getQueue().pop(channel);
    }

    @Override
    public void purge(String channel) throws QueueException {
        getQueue().purge(channel);
    }
    
    @Override
    public long getQueueSize(String channel) throws QueueException {
        return getQueue().getSize(channel);
    }
    
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        //this.properties = (HashMap<String,String>)properties.clone();        
        //getStatus(adapterName); //required if we need to overwrite updateStatusItem() method
        queueAdapterName = properties.get("queue-adapter-name");
        if (null == queueAdapterName || queueAdapterName.isEmpty()) {
            Kernel.getInstance().getLogger().print("\tWARNING! queue-adapter-name parameter is not set.");
        }else{
            Kernel.getInstance().getLogger().print("\tqueue-adapter-name: " + queueAdapterName);
        }
        Kernel.getInstance().getLogger().print("\tqueue-adapter-name: " + queueAdapterName);
    }
    
}
