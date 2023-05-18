async def mqtt_publisher():
    import ujson
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
            client = MQTTClient(client_id="deeper", server=host, socket_timeout=1, message_timeout=3)
            try:
                client.connect()
                # Publish messages to a topic
                while True:
                    dictResponse = {
                        "data": {"status": str(store.status), "depth": str(store.depth), "battery": str(-1),
                                 "temperature": str(-1)}}
                    message = ujson.dumps(dictResponse)
                    client.publish("deeper/depth", message, qos=1)
                    ulogging.info("Message published: "+str(message))
                    store.deep_sleep_count = 0
                    await asyncio.sleep_ms(250)
            except Exception as e:
                ulogging.info(str(e))
                try:
                    # Disconnect from the MQTT broker
                    client.disconnect()
                except Exception as e:
                    ulogging.info(str(e))
            ip_parts = host.split('.')
            ip_parts[-1] = str(int(ip_parts[-1]) + 1)
            host = '.'.join(ip_parts)
