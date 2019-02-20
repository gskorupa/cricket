/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cricketmsf.out.queue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.cricketmsf.exception.QueueException;
import org.cricketmsf.out.OutboundAdapter;
import org.cricketmsf.out.OutboundAdapterIface;
import org.cricketmsf.in.queue.QueueCallbackIface;

/**
 *
 * @author greg
 */
public class SimpleQueue extends OutboundAdapter implements QueueIface, OutboundAdapterIface {

    private HashMap<String, ArrayList<QueueCallbackIface>> subscribers;
    private HashMap<String, HashMap<String, Object>> channels;
    private HashMap<String, ArrayList<Object>> listChannels;
    private boolean notifyAll = false;

    @Override
    public void add(String channel, String key, Object value) throws QueueException {
        if (!channels.containsKey(channel)) {
            channels.put(channel, new HashMap<String, Object>());
        }
        if(!notify(channel, value)){
            channels.get(channel).put(key, value);
        }
    }

    @Override
    public Object get(String channel, String key) throws QueueException {
        if (!channels.containsKey(channel)) {
            return null;
        }
        Object value = channels.get(channel).get(key);
        if (null != value) {
            channels.get(channel).remove(key);
        }
        return value;
    }

    @Override
    public Object show(String channel, String key) throws QueueException {
        if (!channels.containsKey(channel)) {
            return null;
        }
        return channels.get(channel).get(key);
    }

    @Override
    public void subscribe(String channel, QueueCallbackIface callback) throws QueueException {
        if (!subscribers.containsKey(channel)) {
            subscribers.put(channel, new ArrayList<>());
        }
        subscribers.get(channel).add(callback);
    }

    @Override
    public void unsubscribe(String channel, QueueCallbackIface callback) throws QueueException {
        if (!subscribers.containsKey(channel)) {
            return;
        }
        subscribers.get(channel).remove(subscribers.get(channel).indexOf(callback));
    }

    @Override
    public void purge(String channel) throws QueueException {
        if (channels.containsKey(channel)) {
            channels.get(channel).clear();
        }
        if (listChannels.containsKey(channel)) {
            listChannels.get(channel).clear();
        }
    }

    @Override
    public String getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public Map<String, String> getStatus(String name) {
        return super.getStatus(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void push(String channel, Object value) throws QueueException {
        if (!listChannels.containsKey(channel)) {
            listChannels.put(channel, new ArrayList<>());
        }
        if(!notify(channel, value)){
            listChannels.get(channel).add(value);
        }
    }

    @Override
    public Object pop(String channel) throws QueueException {
        if (!listChannels.containsKey(channel)) {
            return null;
        }
        try {
            return listChannels.get(channel).remove(0);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private boolean notify(String channel, Object value) {
        if(subscribers.get(channel).size()>0){
            return false;
        }
        if (subscribers.containsKey(channel)) {
            if (notifyAll) {
                subscribers.get(channel).forEach((QueueCallbackIface client) -> {
                    client.call(value);
                });
            } else {
                subscribers.get(channel).get(0).call(value);
            }
        }
        return true;
    }
    
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        //this.properties = (HashMap<String,String>)properties.clone();        
        //getStatus(adapterName); //required if we need to overwrite updateStatusItem() method
        subscribers = new HashMap<>();
        channels = new HashMap<>();
        listChannels = new HashMap<>();
    }
}
