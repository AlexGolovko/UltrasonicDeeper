import gc

import machine, onewire
import utime
import uasyncio as asyncio
import ujson
import sensor
import math
import logging
from server import WSReader, WSWriter

deep_sleep_count = 0
deep_sleep_time = 600
ds_temperature = 0
http_server_port = 80
ws_server_port = 8080

logging.basicConfig(level=logging.DEBUG)

SONAR = "sonar"


# logging.basicConfig(logging.basicConfig(filename='app.log', filemode='w', format='%(name)s - %(levelname)s - %(message)s'))


async def blink():
    led = machine.Pin(2, machine.Pin.OUT);
    while True:
        led.on()
        await asyncio.sleep_ms(500)
        led.off()
        await asyncio.sleep_ms(500)
        increase()
        # print('deep_sleep=' + str(deep_sleep_count) + ' depth= ' + str(sensor.measure_depth()) + 'voltage=' + str(
        #     sensor.battery_level()))


def serve(reader, writer):
    try:
        request = (yield from reader.read()).decode('utf-8')
        logging.debug("================")
        logging.debug(request)
        yield from writer.awrite("""HTTP/1.0 200 OK\r\n""")
        yield from writer.awrite("""Content-Type: application/json\r\n""")
        if request.startswith('GET /sonar'):
            yield from writer.awrite("""Access-Control-Allow-Origin: *\r\n\r\n""")
            yield from writer.awrite(response())
            logging.debug("Finished processing request")
            logging.debug("deep_sleep= " + str(reset()))
        if request.startswith('GET /feature'):
            yield from writer.awrite("""Access-Control-Allow-Origin: *\r\n\r\n""")
            yield from writer.awrite(responseFeature())
            logging.debug("deep_sleep= " + str(reset()))
        yield from writer.aclose()
        gc.collect()
    except Exception as err:
        logging.debug(err)


async def temperature():
    while True:
        try:
            ds_temperature = sensor.temperature()
            # sensor.ds_sensor.convert_temp()
            # await asyncio.sleep_ms(750)
            # global ds_temperature
            # ds_temperature = sensor.ds_sensor.read_temp(sensor.roms[0])
            logging.info('temperature= ' + str(ds_temperature))
            await asyncio.sleep(10)
        except Exception as err:
            logging.debug(err)


def increase():
    global deep_sleep_count
    deep_sleep_count += 1
    if deep_sleep_count % 10 == 0:
        logging.debug("I am going to sleep in " + str(deep_sleep_time - deep_sleep_count))
    if deep_sleep_count > deep_sleep_time:
        logging.debug('I am going to sleep')
        machine.deepsleep(0)
    return deep_sleep_count


def reset():
    global deep_sleep_count
    deep_sleep_count = 0
    return deep_sleep_count


def response():
    try:
        dict = {"status": 200, "depth": str(sensor.measure_depth()), "battery": sensor.battery_level(),
                "temperature": str(ds_temperature)}
    except Exception as err:
        logging.error(err)
        pass
    return ujson.dumps(dict)


def isCorrect(depths):
    for depth in depths:
        if depth == 0:
            return False
    deltas = [0 for i in range(3)]
    depthsLen = len(depths)
    for iter in range(depthsLen):
        if iter == (depthsLen - 1):
            deltas[iter] = math.fabs(depths[iter] - depths[0])
        else:
            deltas[iter] = math.fabs(depths[iter] - depths[iter + 1])
    for delta in deltas:
        if delta > 3:
            return False
    return True


def responseFeature():
    dictResponse = {}
    try:
        depths = [sensor.measure_depth() for i in range(3)]
        if isCorrect(depths):
            dictResponse = {"event": SONAR,
                            "data": {"status": 200, "depth": str(depths[0]), "battery": sensor.battery_level(),
                                     "temperature": str(ds_temperature)}}

        else:
            dictResponse = {"event": SONAR,
                            "data": {"status": 300, "depth": "-1", "battery": sensor.battery_level(),
                                     "temperature": str(ds_temperature)}}

    except Exception as err:
        logging.debug(err)
        pass
    reset()
    logging.info(dictResponse)
    return ujson.dumps(dictResponse)


def websocketHandle(reader, writer):
    try:
        logging.debug("echo")
        # Consume GET line
        yield from reader.readline()

        reader = yield from WSReader(reader, writer)
        writer = WSWriter(reader, writer)

        while 1:
            line = yield from reader.read(256)
            logging.debug("line:" + str(line))
            if line == b"\r":
                await writer.awrite(b"\r\n")
            else:

                await writer.awrite(responseFeature())
            gc.collect()
    except Exception as err:
        logging.debug(str(err))


def run():
    loop = asyncio.get_event_loop()
    # httpServer = asyncio.start_server(serve, host="0.0.0.0", port=http_server_port)
    wsServer = asyncio.start_server(websocketHandle, host="0.0.0.0", port=ws_server_port)
    # loop.create_task(httpServer)
    loop.create_task(wsServer)
    loop.create_task(blink())
    loop.create_task(temperature())
    try:
        loop.run_forever()
    except Exception as err:
        # httpServer.close()
        wsServer.close()
        loop.close()
        raise err
