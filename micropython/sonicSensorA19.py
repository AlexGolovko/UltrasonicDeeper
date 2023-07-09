import uasyncio

import machine, utime
import uasyncio as asyncio
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

timeout = 60000
ds_pin = machine.Pin(0, machine.Pin.PULL_UP)

global ds_sensor, roms


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
def test():
    while True:
        depths = [measure_depth() for i in range(10)]
        print(str(depths))
        utime.sleep(3)


def measure_depth():
    curr_time = utime.ticks_ms()
    trig = machine.Pin(3, mode=machine.Pin.OUT, pull=machine.Pin.PULL_DOWN)
    trig.value(0)
    echo = machine.Pin(4, mode=machine.Pin.IN, pull=machine.Pin.PULL_DOWN)
    #
    trig.value(0)
    uasyncio.sleep_ms(60)
    trig.value(1)
    utime.sleep_us(20)
    trig.value(0)
    duration = machine.time_pulse_us(echo, 1, timeout)
    distance = 0
    if duration > 0:
        # d = v * t / 2
        distance = 1482.7 * duration / 1000000 / 2
        # t = ('t= {} us'.format(duration))
        # d = ('d= {:1.3f} m'.format(distance))
        # utime.sleep_us(timeout - duration)
    else:
        ulogging.info('measuring error' + str(duration))
    trig.value(0)
    execution_time = utime.ticks_diff(utime.ticks_ms(), curr_time)
    ulogging.debug('measure depth duration: ' + str(execution_time))
    return distance


def measure_air_distance():
    trig = machine.Pin(3, mode=machine.Pin.OUT, pull=machine.Pin.PULL_DOWN)
    trig.value(0)
    echo = machine.Pin(4, mode=machine.Pin.IN, pull=machine.Pin.PULL_DOWN)
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
        ulogging.info('measuring error' + str(duration))

    return distance, duration
