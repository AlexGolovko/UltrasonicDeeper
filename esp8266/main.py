import machine
import uasyncio
import ulogging

import pins

pins.D0.on()



def go():
    ulogging.basicConfig(level=ulogging.WARNING)
    try:
        import runner
        runner.run()
    except Exception as err:
        ulogging.info(str(err))
        loop = uasyncio.get_event_loop()
        loop.stop()
        loop.close()
        machine.reset()




if __name__ == '__main__':
    go()