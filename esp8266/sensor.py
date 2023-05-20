import ds18x20
import machine
import onewire
import uasyncio
import ulogging

# global trig, echo, ds_pin, ds_sensor, timeout, roms
# LOLIN D1 mini v3.1.0
# Trig-D6-GPIO12
# Echo-D5-GPIO14
# trig = machine.Pin(12, machine.Pin.OUT)
# echo = machine.Pin(14, machine.Pin.IN)
# trig = machine.Pin(12, machine.Pin.OUT, pull=None)
# trig.value(0)
# echo = machine.Pin(14, machine.Pin.IN, pull=None)
# Ai-Thinker ESP-C3-12F
# Trig-IO1-GPIO1
# Echo-IO2-GPIO2
# moved to methods
# trig = machine.Pin(1, mode=machine.Pin.OUT, pull=machine.Pin.PULL_DOWN)
# trig.value(0)
# echo = machine.Pin(2, mode=machine.Pin.IN, pull=machine.Pin.PULL_DOWN)

ds_pin = machine.Pin(0, machine.Pin.PULL_UP)

global ds_sensor, roms


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
