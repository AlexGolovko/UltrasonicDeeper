import utime
import machine
import network
import logger
import ulogging


def go():
    wlan = network.WLAN(network.STA_IF)
    if not wlan.isconnected():
        print('not wlan.isconnected()')
        import webrepl
        webrepl.stop()
        ulogging.basicConfig(level=ulogging.WARNING)
    try:
        import runner
        runner.run()
    except Exception as err:
        logger.error(err)
        utime.sleep(10)
        machine.reset()


if __name__ == '__main__':
    go()
