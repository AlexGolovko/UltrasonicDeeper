# Ultrasonic Depth Sensor with MQTT Communication
The Ultrasonic Depth Sensor pet project involves using an Android phone as an ws client, an ESP32 running MicroPython as a Wi-Fi access point and ws server for sonar data, and an Angular app as a user interface.

The architecture consists of three main components:

1. Android Phone as ws client

2. ESP32 running MicroPython as Wi-Fi Access Point and ws server
The ESP32 runs a MicroPython firmware and acts as a Wi-Fi access point. It publishes sonar data to the ws client running on the Android phone using the websocket protocol. The MicroPython code uses the ultrasonic sensor to read the depth of the water and publishes the sonar data to the ws client.

3. Angular App as User Interface and ws client
The Angular app runs on a web browser on a computer or a mobile device and displays the real-time depth readings from the ultrasonic sensor.

The interaction between the different components is as follows:

The ESP32 creates a wifi access point
The Android phone connects to ESP32 via ws
The ESP32 reads the depth data from the ultrasonic sensor and publishes it to the ws client running on the Android phone.
The Android phone store data to db, and publishes it to the UI ws client

