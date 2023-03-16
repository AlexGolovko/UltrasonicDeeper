# Ultrasonic Depth Sensor with MQTT Communication
The Ultrasonic Depth Sensor pet project involves using an Android phone as an MQTT server, an ESP32 running MicroPython as a Wi-Fi access point and MQTT publisher for sonar data, and an Angular app as a user interface and MQTT subscriber.

The architecture consists of three main components:

1. Android Phone as MQTT Server
The Android phone runs an MQTT broker software such as Mosquitto or HiveMQ and acts as the MQTT server. It connects to the ESP32 access point over Wi-Fi and receives sonar data published by the ESP32.

2. ESP32 running MicroPython as Wi-Fi Access Point and MQTT Publisher
The ESP32 runs a MicroPython firmware and acts as a Wi-Fi access point. It publishes sonar data to the MQTT server running on the Android phone using the MQTT protocol. The MicroPython code uses the ultrasonic sensor to read the depth of the water and publishes the sonar data to the MQTT server.

3. Angular App as User Interface and MQTT Subscriber
The Angular app runs on a web browser on a computer or a mobile device and displays the real-time depth readings from the ultrasonic sensor. It subscribes to the MQTT topic where the sonar data is being published and receives the data as JSON payloads. The app parses the JSON payloads and updates the graph or chart with the new depth readings.

The interaction between the different components is as follows:

The ESP32 reads the depth data from the ultrasonic sensor and publishes it to the MQTT server running on the Android phone.
The Android phone MQTT server receives the sonar data and stores it in a queue.
The Angular app subscribes to the MQTT topic where the sonar data is being published and receives the data as JSON payloads.
The Angular app parses the JSON payloads and updates the graph or chart with the new depth readings.
Using MQTT as the communication protocol allows for a scalable and flexible design that can easily be adapted to different use cases and scenarios. The components can exchange data with each other in a secure and efficient manner.
