import uasyncio
import ulogging

import sensor
import store
import ds18x20
import machine
import onewire

global ds_sensor, roms
ds_pin = machine.Pin(0, machine.Pin.PULL_UP)


def init():
    ulogging.info("In init")
    global ds_sensor
    global roms
    try:
        ds_sensor = ds18x20.DS18X20(onewire.OneWire(ds_pin))
        roms = ds_sensor.scan()
        ulogging.info('Found DS devices: ' + str(roms))
    except Exception as err:
        ulogging.error(str(err))


init()


def temperature():
    try:
        if len(roms) == 0:
            return -273
        ds_sensor.convert_temp()
        uasyncio.sleep_ms(750)
        return ds_sensor.read_temp(roms[0])
    except onewire.OneWireError as err:
        ulogging.error(str(err))
        return -273
    except Exception as err:
        ulogging.error(str(err))
        return -273


async def run():
    while True:
        try:
            store.ds_temperature = temperature()
            ulogging.debug('temperature= ' + str(store.ds_temperature))
            await uasyncio.sleep(30)
        except Exception as err:
            ulogging.debug(str(err))
