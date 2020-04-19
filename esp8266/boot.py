# This file is executed on every boot (including wake-boot from deepsleep)
# import esp
# esp.osdebug(None)
# import gc
# import webrepl
# webrepl.start()
import network
import machine
import esp
import gc
import utime

machine.freq(160000000)
#esp.osdebug(None)


def do_connect():
    wlan = network.WLAN(network.STA_IF)
    wlan.active(True)
    if not wlan.isconnected():
        print('connecting to network...')
        wlan.connect("royter", "traveller22")
        while not wlan.isconnected():
            pass
    print('network config:', wlan.ifconfig())

do_connect()
gc.collect()
print('wifi connected')
