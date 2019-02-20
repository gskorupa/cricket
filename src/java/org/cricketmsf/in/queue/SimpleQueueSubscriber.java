/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cricketmsf.in.queue;

import java.util.HashMap;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.exception.QueueException;
import org.cricketmsf.in.InboundAdapter;
import org.cricketmsf.out.queue.QueueIface;

/**
 *
 * @author greg
 */
public class SimpleQueueSubscriber extends InboundAdapter implements SubscriberIface, QueueCallbackIface {

    private QueueIface queue = null;
    String queueAdapterName = null;
    String channelName = null;

    private QueueIface getQueue() throws QueueException {
        if (null == getQueue()) {
            try {
                queue = (QueueIface) Kernel.getInstance().getAdaptersMap().get(queueAdapterName);
                subscribe(channelName);
            } catch (Exception e) {
            }
        }
        if (null == queue) {
            throw new QueueException(QueueException.QUEUE_NOT_DEFINED);
        }
        return queue;
    }

    @Override
    public void subscribe(String channel) throws QueueException {
        getQueue().subscribe(channel, this);
    }

    @Override
    public void unsubscribe(String channel) throws QueueException {
        getQueue().unsubscribe(channel, this);
    }

    @Override
    public void call(Object value) {
        Kernel.getInstance().handleEvent(Event.fromJson((String) value));
    }

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        //this.properties = (HashMap<String,String>)properties.clone();        
        //getStatus(adapterName); //required if we need to overwrite updateStatusItem() method
        queueAdapterName = properties.get("queue-adapter-name");
        Kernel.getInstance().getLogger().print("\tqueue-adapter-name: " + queueAdapterName);
        channelName = properties.get("channel-name");
        Kernel.getInstance().getLogger().print("\tchannel-name: " + channelName);
    }
}
