import time
import machine
import network
import webrepl
import runner

global pin, trig, echo

if __name__ == '__main__':
    wlan = network.WLAN(network.STA_IF)
    if wlan.isconnected():
        webrepl.start()
    try:
        runner.run()
    except Exception as err:
        print(err)
        time.sleep(10)
        machine.reset()
