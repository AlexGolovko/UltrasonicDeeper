import gc
import time

import machine
import utime
import webrepl
import runner

global pin, trig, echo


def measure_air_distance():
    # Trig-D1-GPIO5
    # Echo-D2-GPIO4
    utime.ticks_ms()
    trig = machine.Pin(5, machine.Pin.OUT)
    echo = machine.Pin(4, machine.Pin.IN)
    timeout = 30000
    trig.value(1)
    time.sleep_us(1)
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
        print('measuring error' + str(duration))

    return distance, duration


def measure_depth():
    # Trig-D1-GPIO5
    # Echo-D2-GPIO4
    trig = machine.Pin(5, machine.Pin.OUT)
    echo = machine.Pin(4, machine.Pin.IN)
    timeout = 30000
    trig.value(1)
    time.sleep_us(1)
    trig.value(0)
    duration = machine.time_pulse_us(echo, 1, timeout)
    distance = 0
    if duration > 0:
        # d = v * t / 2
        distance = 1482.7 * duration / 1000000 / 2
        t = ('t= {} us'.format(duration))
        d = ('d= {:1.3f} m'.format(distance))
        print(t, d)
    else:
        print('measuring error' + str(duration))
    return distance, duration


def tojson():
    # Should be changed to Data Transfer Object
    json = """{
   "status":"%s",
   "depth":"%s"
}"""
    return json


if __name__ == '__main__':
    webrepl.start()
    try:
        runner.run()
    except Exception as err:
        print(err)
        time.sleep(10)
        machine.reset()
