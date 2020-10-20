import gc
import machine
import utime
import uasyncio as asyncio
import ujson
import sensor
import math

# import logging

deep_sleep_count = 0
deep_sleep_time = 600
ds_temperature = 0


# logging.basicConfig(logging.basicConfig(filename='app.log', filemode='w', format='%(name)s - %(levelname)s - %(message)s'))

@asyncio.coroutine
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


@asyncio.coroutine
def serve(reader, writer):
    try:
        request = (yield from reader.read()).decode('utf-8')
        print("================")
        print(request)
        yield from writer.awrite("""HTTP/1.0 200 OK\r\n""")
        yield from writer.awrite("""Content-Type: application/json\r\n""")
        if request.startswith('GET /sonar'):
            yield from writer.awrite("""Access-Control-Allow-Origin: *\r\n\r\n""")
            yield from writer.awrite(response())
            print("Finished processing request")
            print("deep_sleep= " + str(reset()))
        if request.startswith('GET /feature'):
            yield from writer.awrite("""Access-Control-Allow-Origin: *\r\n\r\n""")
            yield from writer.awrite(responseFeature())
            print("deep_sleep= " + str(reset()))
        yield from writer.aclose()
        gc.collect()
    except Exception as err:
        print(err)


@asyncio.coroutine
async def temperature():
    while True:
        try:
            sensor.ds_sensor.convert_temp()
            await asyncio.sleep_ms(750)
            global ds_temperature
            ds_temperature = sensor.ds_sensor.read_temp(sensor.roms[0])
            print('temperature= ' + str(ds_temperature))
            await asyncio.sleep(10)
        except Exception as err:
            print(err)


def increase():
    global deep_sleep_count
    deep_sleep_count += 1
    print("I am going to sleep in " + str(deep_sleep_time - deep_sleep_count))
    if deep_sleep_count > deep_sleep_time:
        print('I am going to sleep')
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
        # logging.error(err)
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
        if delta > 1:
            return False
    return True


def responseFeature():
    dictResponse = {}
    try:
        depths = [sensor.measure_depth() for i in range(3)]
        if isCorrect(depths):
            dictResponse = {"status": 200, "depth": str(depths[0]), "battery": sensor.battery_level(),
                            "temperature": str(ds_temperature)}
        else:
            dictResponse = {"status": 300, "depth": "-1", "battery": sensor.battery_level(),
                            "temperature": str(ds_temperature)}

    except Exception as err:
        print(err)
        pass
    return ujson.dumps(dictResponse)


def run():
    loop = asyncio.get_event_loop()
    loop.call_soon(asyncio.start_server(serve, host="0.0.0.0", port=80))
    loop.create_task(blink())
    loop.create_task(temperature())
    try:
        loop.run_forever()
    except Exception as err:
        loop.close()
        raise err
