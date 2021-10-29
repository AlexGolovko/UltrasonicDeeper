import time
import machine
import network
import logger
import ulogging

if __name__ == '__main__':
    wlan = network.WLAN(network.STA_IF)
    if not wlan.isconnected():
        import webrepl

        webrepl.stop()
        ulogging.basicConfig(level=ulogging.WARNING)
    try:
        import runner

        runner.run()
    except Exception as err:
        logger.error(err)
        time.sleep(10)
        machine.reset()
