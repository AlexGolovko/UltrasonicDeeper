# This file is executed on every boot (including wake-boot from deepsleep)
import esp
import gc
import machine
import network
esp.osdebug(None)


# machine.freq(160000000)


def do_connect(wifi_name, wifi_pass):
    ssid = 'microsonar'
    password = 'microsonar'
    ap_if = network.WLAN(network.AP_IF)
    ap_if.active(True)
    ap_if.config(essid=ssid, password=password)
    while not ap_if.active():
        pass
    print('Connection successful')
    print(ap_if.ifconfig())

    wlan = network.WLAN(network.STA_IF)
    wlan.active(True)
    wlans = wlan.scan()
    if wifi_name in str(wlans):
        print('connecting to network...')
        wlan.connect(wifi_name, wifi_pass)
        while not wlan.isconnected():
            pass
    else:
        wlan.active(False)

    print('network config:', wlan.ifconfig())

machine.Pin(2, machine.Pin.OUT).off()
do_connect('royter', 'traveller22')
gc.collect()
print('wifi connected')
