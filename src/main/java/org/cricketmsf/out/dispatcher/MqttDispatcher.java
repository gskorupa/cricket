/*
 * Copyright 2018 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package org.cricketmsf.out.dispatcher;

import org.cricketmsf.exception.DispatcherException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.event.EventDecorator;
import org.cricketmsf.out.OutboundAdapter;
import org.cricketmsf.out.mqtt.MqttPublisher;
import org.cricketmsf.out.mqtt.MqttPublisherException;

public class MqttDispatcher extends OutboundAdapter implements Adapter, DispatcherIface{

    private String clientID;
    private String brokerURL;
    private boolean debug = false;
    private int qos = 1;
    private String rootTopic = "events/";
    private ConcurrentHashMap eventMap = new ConcurrentHashMap();

    @Override
    public void dispatch(Event event) throws DispatcherException {
        String topic = event.getCategory() + "/" + event.getType();
        if (eventMap.containsKey(event.getCategory() + "/*") || eventMap.containsKey(topic)) {
            try {
                MqttPublisher.publish(brokerURL, clientID, qos, debug, rootTopic+topic, (String) event.getPayload());
            } catch (MqttPublisherException ex) {
                Kernel.getInstance().dispatchEvent(Event.logSevere(this, ex.getCode() + " " + ex.getMessage()));
            }
        } else {
            throw new DispatcherException(DispatcherException.UNKNOWN_EVENT);
        }
    }

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        clientID = properties.getOrDefault("client-id", "");
        if(clientID.isEmpty()){
            clientID=Kernel.getInstance().getUuid().toString();
        }
        this.properties.put("client-id", clientID);
        Kernel.getInstance().getLogger().print("    client-id: " + clientID);
        brokerURL = properties.get("url");
        this.properties.put("url", brokerURL);
        Kernel.getInstance().getLogger().print("    url: " + brokerURL);
        try {
            this.properties.put("qos", properties.getOrDefault("qos", "0"));
            qos = Integer.parseInt(this.properties.getOrDefault("qos", "0"));
            if (qos > 2) {
                qos = 2;
            }
        } catch (NumberFormatException e) {
        }
        Kernel.getInstance().getLogger().print("    qos: " + qos);
        try {
            this.properties.put("debug", properties.getOrDefault("debug", "false"));
            debug = Boolean.parseBoolean(this.properties.getOrDefault("debug", "false"));
        } catch (NumberFormatException e) {
        }
        Kernel.getInstance().getLogger().print("    debug: " + debug);
        try {
            rootTopic = properties.getOrDefault("root-topic", "CricketService");
            if (!rootTopic.endsWith("/")) {
                rootTopic = rootTopic.concat("/");
            }
            this.properties.put("root-topic", rootTopic);
            Kernel.getInstance().getLogger().print("    root-topic: " + rootTopic);
            String eventTypes = properties.getOrDefault("event-types", "");
            registerEventTypes(eventTypes);
            Kernel.getInstance().getLogger().print("    event-types: " + eventTypes);
            Kernel.getInstance().getLogger().print("    event-types-configured: " + eventMap.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerEventType(String category, String type) throws DispatcherException {
        if (type == null || type.isEmpty()) {
            eventMap.put(category + "/*", null);
        } else {
            eventMap.put(category + "/" + type, null);
        }
    }

    @Override
    public void registerEventTypes(String pathsConfig) {
        String[] paths = pathsConfig.split(";");
        for (String path : paths) {
            if (!path.isEmpty()) {
                eventMap.put(path, null);
            }
        }
    }

    //TODO: all EventDecorator events are handled by the Kernel. Should be send to the queue?
    @Override
    public void dispatch(EventDecorator event) throws DispatcherException {
        throw new DispatcherException(DispatcherException.UNKNOWN_EVENT);
    }

    @Override
    public DispatcherIface getDispatcher() {
        return this;
    }

    @Override
    public void start() {
    }

    @Override
    public boolean isReady() {
        return true;
    }
}
