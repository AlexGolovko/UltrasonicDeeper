import ujson, utime
import logger
import math

import pins
import sensor
import sonicSensorA19 as ultrasonic_sensor_a19
import uasyncio, machine
import ulogging

ds_temperature = 0
SONAR = "sonar"

def run():
    service = SensorService()
    service.run()
    return service


class SensorService:

    def run(self):

        uasyncio.create_task(depth())

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