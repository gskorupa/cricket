/*
 * Copyright 2020 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
import org.cricketmsf.Kernel;
import org.cricketmsf.in.InboundAdapter;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author greg
 */
public class MqttSubscriber extends InboundAdapter implements Adapter {
    
    private static final Logger logger = LoggerFactory.getLogger(MqttSubscriber.class);
    private String filters = "";
    private String[] topicFilters = {};
    private int qos = 0;
    private int[] qosArray = {};
    private String brokerURL = "tcp://test.mosquitto.org:1883";
    private String clientID = "CricketService";
    private MqttClient mqttClient;

    //TODO: pola poniżej
    private boolean cleanSession = false;
    private String user;
    private String password;
    private boolean acceptAllCertificates = true;

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
        clientID = properties.getOrDefault("client-id", "");
        if (clientID.isEmpty()) {
            clientID = Kernel.getInstance().getUuid().toString() + ".sub";
        }
        this.properties.put("client-id", clientID);
        brokerURL = properties.get("url");
        this.properties.put("url", brokerURL);
        try {
            this.properties.put("qos", properties.getOrDefault("qos", "0"));
            qos = Integer.parseInt(this.properties.getOrDefault("qos", "0"));
            if (qos > 2) {
                qos = 2;
            }
        } catch (NumberFormatException e) {
            logger.warn(e.getMessage());
        }
        filters = properties.getOrDefault("topics", "#");
        this.properties.put("topics", filters);
        topicFilters = parse(filters);
        qosArray = getQos(qos, topicFilters.length);
        
        logger.info("\turl: " + brokerURL);
        logger.info("\tclient-id: " + clientID);
        logger.info("\tqos: " + qos);
        logger.info("\ttopics: " + filters);
    }
    
    public void start() {
        try {
            mqttClient = new MqttClient(brokerURL, clientID);
            Callback callback = new Callback();
            callback.setClient(mqttClient);
            mqttClient.setCallback(callback);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(cleanSession);
            mqttClient.connect(options);
            mqttClient.subscribe(topicFilters, qosArray);
        } catch (MqttException me) {
            logger.warn("{} {} {}", me.getReasonCode(), me.getMessage(), me.getCause());
        } finally {
            mqttClient = null;
        }
    }
    
    public void stop() {
        try {
            mqttClient.disconnect();
        } catch (MqttException ex) {
            logger.warn(ex.getMessage());
        }
    }
    
    private String[] parse(String definition) {
        return definition.split(";");
    }
    
    private int[] getQos(int size, int value) {
        int[] result = new int[size];
        for (int i = 0; i < size; i++) {
            result[i] = value;
        }
        return result;
    }
    
}
