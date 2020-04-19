import webrepl
import machine
import utime
import time
import gc

global pin,trig,echo


def pingLED():
    pin.on()
    print("on")
    utime.sleep(1)
    pin.off()
    print("off")
    utime.sleep(1)
    gc.collect()


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
def measure_air_distance():
    # Trig-D1-GPIO5
    # Echo-D2-GPIO4
    utime.ticks_ms()
    # trig = machine.Pin(5, machine.Pin.OUT)
    # echo = machine.Pin(4, machine.Pin.IN)
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
    # trig = machine.Pin(5, machine.Pin.OUT)
    # echo = machine.Pin(4, machine.Pin.IN)
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


def work_loop():
    pin.on()
    time.sleep(0.5)
    print('Air: ')
    measure_air_distance()
    print('Depth: ')
    measure_depth()
    pin.off()
    time.sleep(0.5)


if __name__ == '__main__':
    pin = machine.Pin(2, machine.Pin.OUT)
    trig = machine.Pin(5, machine.Pin.OUT)
    echo = machine.Pin(4, machine.Pin.IN)
    webrepl.start()
    while True:
        try:
            work_loop()
        except Exception as err:
            print(err)
            machine.reset()
