/*
 * Copyright 2018 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package org.cricketmsf.in.mqtt;

import java.util.HashMap;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.in.InboundAdapter;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 *
 * @author greg
 */
public class MqttSubscriber extends InboundAdapter implements Adapter {

    private String rootTopic = "org.cricketmsf/events";
    private String topicFilter = "/#";
    private int qos = 1;
    private String brokerURL = "tcp://test.mosquitto.org:1883";
    private String clientID = "CricketService";
    private boolean debug = false;
    private String typeSuffix = "";

    /**
     * This method is executed while adapter is instantiated during the service
     * start. It's used to configure the adapter according to the configuration.
     *
     * @param properties map of properties readed from the configuration file
     * @param adapterName name of the adapter set in the configuration file (can
     * be different from the interface and class name.
     */
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        clientID = properties.getOrDefault("client-id", "CricketService");
        this.properties.put("client-id", clientID);
        Kernel.getInstance().getLogger().printIndented("client-id: " + clientID);
        brokerURL = properties.get("url");
        this.properties.put("url", brokerURL);
        Kernel.getInstance().getLogger().printIndented("url: " + brokerURL);
        try {
            this.properties.put("qos", properties.getOrDefault("qos", "0"));
            qos = Integer.parseInt(this.properties.getOrDefault("qos", "0"));
            if (qos > 2) {
                qos = 2;
            }
        } catch (NumberFormatException e) {
        }
        Kernel.getInstance().getLogger().printIndented("qos: " + qos);
        try {
            this.properties.put("debug", properties.getOrDefault("debug", "false"));
            debug = Boolean.parseBoolean(this.properties.getOrDefault("debug", "false"));
        } catch (NumberFormatException e) {
        }
        Kernel.getInstance().getLogger().printIndented("debug: " + debug);
        rootTopic = properties.getOrDefault("root-topic", "");
        this.properties.put("root-topic", rootTopic);
        Kernel.getInstance().getLogger().printIndented("root-topic: " + rootTopic);
        topicFilter = properties.getOrDefault("topic-filter", "");
        this.properties.put("topic-filter", topicFilter);
        Kernel.getInstance().getLogger().printIndented("topicFilter: " + topicFilter);
        typeSuffix = properties.getOrDefault("type-suffix", "");
        this.properties.put("type-suffix", typeSuffix);
        Kernel.getInstance().getLogger().printIndented("type-suffix: " + typeSuffix);
    }

    @Override
    public void run() {
        Callback listener;
        MqttClient sampleClient;
        try {
            while (true) {
                try {
                    listener = new Callback(rootTopic, typeSuffix);
                    sampleClient = new MqttClient(brokerURL, clientID);
                    sampleClient.setCallback(listener);
                    sampleClient.connect();
                    sampleClient.subscribe(rootTopic + topicFilter, qos);
                    Thread.sleep(10000);
                    sampleClient.disconnect();
                } catch (MqttException me) {
                    if (debug) {
                        System.out.println("reason " + me.getReasonCode());
                        System.out.println("msg " + me.getMessage());
                        System.out.println("loc " + me.getLocalizedMessage());
                        System.out.println("cause " + me.getCause());
                        System.out.println("excep " + me);
                        me.printStackTrace();
                    }
                }
                //Thread.yield();
            }
        } catch (InterruptedException e) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "interrupted"));
        }
    }

}
