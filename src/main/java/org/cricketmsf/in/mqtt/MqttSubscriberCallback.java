package org.cricketmsf.in.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;

/**
 *
 * @author greg
 */
public interface MqttSubscriberCallback {
    public void setClient(MqttClient client);
}
