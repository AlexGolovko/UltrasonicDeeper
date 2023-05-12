import time

import uasyncio
import utime
import machine
import network
import ulogging
import pins



def go():
    wlan = network.WLAN(network.STA_IF)
    if not wlan.isconnected():
        print('not wlan.isconnected()')
        # import webrepl
        # webrepl.stop()
        ulogging.basicConfig(level=ulogging.WARNING)
    try:
        ulogging.basicConfig(level=ulogging.DEBUG)
        import runner
        runner.run()
    except Exception as err:
        ulogging.info(err)
        loop = uasyncio.get_event_loop()
        loop.stop()
        loop.close()
        machine.reset()




if __name__ == '__main__':
    go()
