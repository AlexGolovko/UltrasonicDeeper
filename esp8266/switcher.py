import time
import uasyncio, ulogging, pins, machine
import utime

deep_sleep_time = 600


async def run():
    while True:
        await uasyncio.sleep(120)
        increase()


def increase():
    import store
    store.deep_sleep_count += 120
    if store.deep_sleep_count > deep_sleep_time:
        go_sleep()


def go_sleep():
    print('I am going to sleep')
    pins.D0.off()
    time.sleep_ms(100)
    machine.reset()


def reset():
    import store
    store.deep_sleep_count = 0


def check(start_time):
    if utime.ticks_diff(utime.ticks_ms(), start_time) > 600000:
        go_sleep()
    return None