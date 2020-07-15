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
package org.cricketmsf.in.queue;

import java.util.HashMap;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.event.EventDecorator;
import org.cricketmsf.exception.QueueException;
import org.cricketmsf.in.InboundAdapter;
import org.cricketmsf.out.queue.QueueIface;

/**
 *
 * @author greg
 */
public class SimpleQueueSubscriber extends InboundAdapter implements SubscriberIface, QueueCallbackIface, Adapter {

    private QueueIface queue = null;
    String queueAdapterName = null;
    String channelNames = null;

    private QueueIface getQueue() throws QueueException {
        if (null == queue) {
            try {
                queue = (QueueIface) Kernel.getInstance().getAdaptersMap().get(queueAdapterName);
                String[] channels = channelNames.split(";");
                for (String channel : channels) {
                    subscribe(channel);
                }
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
    public void call(String channelName, Object value) {
        EventDecorator dev;
        Event ev;
        try {
            dev = (EventDecorator)Class.forName(channelName).newInstance();
            dev.setOriginalEvent(Event.fromJson((String) value));
            Kernel.getInstance().getEventProcessingResult(dev);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Kernel.getInstance().getEventProcessingResult(Event.fromJson((String) value));
        }
    }

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        queueAdapterName = properties.get("queue-adapter-name");
        if (null == queueAdapterName || queueAdapterName.isEmpty()) {
            Kernel.getInstance().getLogger().print("\tWARNING! queue-adapter-name parameter is not set.");
        } else {
            Kernel.getInstance().getLogger().print("\tqueue-adapter-name: " + queueAdapterName);
        }
        channelNames = properties.get("channels");
        if (null == channelNames || channelNames.isEmpty()) {
            Kernel.getInstance().getLogger().print("\tWARNING! channels parameter is not set.");
        } else {
            Kernel.getInstance().getLogger().print("\tchannels: " + channelNames);
        }
    }

    @Override
    public void init() throws QueueException {
        getQueue();
    }
}
