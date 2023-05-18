import uasyncio, ulogging, pins, machine

deep_sleep_time = 600


async def run():
    while True:
        await uasyncio.sleep(60)
        increase()


def increase():
    import store
    store.deep_sleep_count += 60
    if store.deep_sleep_count > deep_sleep_time:
        ulogging.info('I am going to sleep')
        pins.D0.off()
        machine.deepsleep(0)


def reset():
    import store
    store.deep_sleep_count = 0
