import machine
import uasyncio
import ulogging

import pins

pins.D0.on()



def goAsync():
    ulogging.basicConfig(level=ulogging.WARNING)
    try:
        import microdot_async_runner
    except Exception as err:
        ulogging.info(str(err))
        machine.reset()


def goSync():
    ulogging.basicConfig(level=ulogging.WARNING)
    import runner
    runner.start()


if __name__ == '__main__':
    goAsync()