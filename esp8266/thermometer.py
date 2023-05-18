import uasyncio
import ulogging

import sensor
import store


async def run():
    while True:
        try:
            store.ds_temperature = sensor.temperature()
            ulogging.debug('temperature= ' + str(store.ds_temperature))
            await uasyncio.sleep(30)
        except Exception as err:
            ulogging.debug(str(err))
