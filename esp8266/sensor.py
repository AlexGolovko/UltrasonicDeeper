import machine, utime, onewire, ds18x20
import uasyncio as asyncio
import logger

# global trig, echo, ds_pin, ds_sensor, timeout, roms

# Trig-D6-GPIO12
# Echo-D5-GPIO14
# trig = machine.Pin(12, machine.Pin.OUT)
# echo = machine.Pin(14, machine.Pin.IN)
trig = machine.Pin(12, machine.Pin.OUT)
echo = machine.Pin(14, machine.Pin.IN)

timeout = 60000
ds_pin = machine.Pin(13, machine.Pin.PULL_UP)

global ds_sensor, roms


# D3-GPIO0

def init():
    logger.info("In init")
    global ds_sensor
    global roms
    try:
        ds_sensor = ds18x20.DS18X20(onewire.OneWire(ds_pin))
        roms = ds_sensor.scan()
        logger.info('Found DS devices: ' + str(roms))
    except Exception as err:
        logger.error(err)


init()


def battery_level():
    return machine.ADC(0).read()


# machine.time_pulse_us from Pythondoc:
# Time a pulse on the given pin, and return the duration of the pulse in microseconds.
# The pulse_level argument should be 0 to time a low pulse or 1 to time a high pulse.

# If the current input value of the pin is different to pulse_level,
# the function first (*) waits until the pin input becomes equal to pulse_level,
# then (**) times the duration that the pin is equal to pulse_level.
# If the pin is already equal to pulse_level then timing starts straight away.

# The function will return -2 if there was timeout waiting for condition marked (*) above,
# and -1 if there was timeout during the main measurement, marked (**) above.
# The timeout is the same for both cases and given by timeout_us (which is in microseconds).
def measure_depth():
    trig.value(1)
    utime.sleep_us(1)
    trig.value(0)
    duration = machine.time_pulse_us(echo, 1, timeout)
    distance = 0
    if duration > 0:
        # d = v * t / 2
        distance = 1482.7 * duration / 1000000 / 2
        t = ('t= {} us'.format(duration))
        d = ('d= {:1.3f} m'.format(distance))
        utime.sleep_us(timeout - duration)
    else:
        logger.info('measuring error' + str(duration))
    return distance


def measure_air_distance():
    trig.value(1)
    utime.sleep_us(1)
    trig.value(0)
    duration = machine.time_pulse_us(echo, 1, timeout)
    distance = 0
    if duration > 0:
        # v = 331.5 + 0.6*T (C) (v=343.5 at 20C)
        # d = v * t / 2
        distance = 343.5 * duration / 1000000 / 2
        t = ('t= {} us'.format(duration))
        d = ('d= {:1.3f} m'.format(distance))
        print(t, d)
    else:
        logger.info('measuring error' + str(duration))

    return distance, duration


def temperature():
    try:
        if len(roms) == 0:
            return
        ds_sensor.convert_temp()
        utime.sleep_ms(750)
        return ds_sensor.read_temp(roms[0])
    except onewire.OneWireError as err:
        logger.error(err)
        return -273
    except Exception as err:
        logger.error(err)
        return -273
