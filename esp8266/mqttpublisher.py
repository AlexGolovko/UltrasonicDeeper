async def mqtt_publisher():
    import uasyncio as asyncio
    import ulogging
    from umqtt.simple2 import MQTTClient
    import config
    import store

    while True:
        host = config.mqtt_broker_host
        for i in range(3):
            # Create an instance of the MQTT client
            ulogging.info("current host: " + str(host))
            await asyncio.sleep_ms(500)
            client = MQTTClient(client_id="deeper", server=host, socket_timeout=1, message_timeout=5)
            try:
                # Connect to the MQTT broker with a timeout of 5 seconds
                client.connect()
                # Publish messages to a topic
                while True:
                    message = str(store.depth)
                    client.publish("deeper/depth", message)
                    print("Message published: ", message)
                    store.deep_sleep_count = 0
                    await asyncio.sleep_ms(250)
            except Exception as e:
                ulogging.info(e)
                try:
                    # Disconnect from the MQTT broker
                    client.disconnect()
                except Exception as e:
                    ulogging.info(e)
            ip_parts = host.split('.')
            ip_parts[-1] = str(int(ip_parts[-1]) + 1)
            host = '.'.join(ip_parts)
