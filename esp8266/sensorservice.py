import ujson, utime
import logger
import math
import sensor
import uasyncio, machine

ds_temperature = 0
SONAR = "sonar"


def run():
    service = SensorService()
    service.run()
    return service


class DeepSleepTimer:
    def __init__(self, deep_sleep_time=600, deep_sleep_count=0):
        self.deep_sleep_time = deep_sleep_time
        self.deep_sleep_count = deep_sleep_count


class SensorService:
    def __init__(self, timer=DeepSleepTimer()):
        self.timer = timer

    def run(self):
        uasyncio.create_task(temperature())
        uasyncio.create_task(switcher(self.timer))

    def callback(self):
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
            logger.error(err)
            pass
        self.reset()
        logger.info(dictResponse)
        return ujson.dumps(dictResponse)

    def reset(self):
        self.timer.deep_sleep_count = 0


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


async def temperature():
    while True:
        try:
            global ds_temperature
            ds_temperature = sensor.temperature()
            # sensor.ds_sensor.convert_temp()
            # await asyncio.sleep_ms(750)
            # global ds_temperature
            # ds_temperature = sensor.ds_sensor.read_temp(sensor.roms[0])
            logger.info('temperature= ' + str(ds_temperature))
            await uasyncio.sleep(10)
        except Exception as err:
            logger.error(err)


async def switcher(timer):
    while True:
        await uasyncio.sleep(1)
        increase(timer)


def increase(timer):
    # logger.debug('increase:' + str(timer.deep_sleep_time) + ':' + str(timer.deep_sleep_count))
    timer.deep_sleep_count += 1
    # if timer.deep_sleep_count % 10 == 0:
    # logger.debug("I am going to sleep in " + str(timer.deep_sleep_time - timer.deep_sleep_count))
    if timer.deep_sleep_count > timer.deep_sleep_time:
        logger.debug('I am going to sleep')
        machine.deepsleep(0)
