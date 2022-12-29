import uasyncio
import utime
import machine
import network
import ulogging


def go():
    wlan = network.WLAN(network.STA_IF)
    if not wlan.isconnected():
        print('not wlan.isconnected()')
        import webrepl
        webrepl.stop()
        ulogging.basicConfig(level=ulogging.WARNING)
    try:
        ulogging.basicConfig(level=ulogging.DEBUG)
        import runner
        runner.run()
    except Exception as err:
        ulogging.info(err)
        _ = uasyncio.new_event_loop()
        utime.sleep(10)
        machine.reset()


if __name__ == '__main__':
    go()
