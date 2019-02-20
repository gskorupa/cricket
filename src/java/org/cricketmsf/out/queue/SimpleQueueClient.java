/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cricketmsf.out.queue;

import java.util.HashMap;
import org.cricketmsf.Kernel;
import org.cricketmsf.exception.QueueException;
import org.cricketmsf.out.OutboundAdapter;

/**
 *
 * @author greg
 */
public class SimpleQueueClient extends OutboundAdapter implements QueueClientIface {
    
    private QueueIface queue = null;
    String queueAdapterName;

    private QueueIface getQueue() throws QueueException {
        if (null == getQueue()) {
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
    public Object show(String channel, String key) throws QueueException {
        return getQueue().show(channel, key);
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
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        //this.properties = (HashMap<String,String>)properties.clone();        
        //getStatus(adapterName); //required if we need to overwrite updateStatusItem() method
        queueAdapterName = properties.get("queue-adapter-name");
        Kernel.getInstance().getLogger().print("\tqueue-adapter-name: " + queueAdapterName);
    }
    
}
