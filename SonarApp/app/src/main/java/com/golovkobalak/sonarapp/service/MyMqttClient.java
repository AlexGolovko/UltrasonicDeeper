package com.golovkobalak.sonarapp.service;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MyMqttClient {

    private static final String SERVER_URI = "tcp://localhost:1883";
    private static final String CLIENT_ID = "my-android-client";

    private IMqttAsyncClient mqttClient;

    public void connect() throws MqttException {
        // Create the MQTT client
        mqttClient = new MqttAsyncClient(SERVER_URI, CLIENT_ID, new MemoryPersistence());

        // Set the connect options
        MqttConnectOptions connectOptions = new MqttConnectOptions();

        // Connect to the broker
        IMqttToken connectToken = mqttClient.connect(connectOptions);
        connectToken.waitForCompletion();
    }

    public void subscribe(String topic, IMqttMessageListener listener) throws MqttException {
        // Subscribe to the topic
        IMqttToken subscribeToken = mqttClient.subscribe(topic, 1, listener);
        subscribeToken.waitForCompletion();
    }

    public void publish(String topic, String message) throws MqttException {
        // Create the MQTT message
        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
        mqttMessage.setQos(0);

        // Publish the message to the topic
        mqttClient.publish(topic, mqttMessage);
    }

    public void disconnect() throws MqttException {
        // Disconnect from the broker
        mqttClient.disconnect();
    }
}
