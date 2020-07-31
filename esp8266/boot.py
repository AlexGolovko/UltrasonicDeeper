# This file is executed on every boot (including wake-boot from deepsleep)
import esp
esp.osdebug(None)
import gc
import machine
import network

# machine.freq(160000000)


def do_connect(wifi_name, wifi_pass):
    ssid = 'microsonar'
    password = 'microsonar'
    ap_if = network.WLAN(network.AP_IF)
    ap_if.active(True)
    ap_if.config(essid=ssid, password=password)
    while ap_if.active() == False:
        pass
    print('Connection successful')
    print(ap_if.ifconfig())
    wlan = network.WLAN(network.STA_IF)
    wlan.active(True)
    wlans = wlan.scan()
    if not wlan.isconnected() and wifi_name in str(wlans):
        print('connecting to network...')
        wlan.connect(wifi_name, wifi_pass)
        while not wlan.isconnected():
            pass
    print('network config:', wlan.ifconfig())


def do_connect_default():
    wlan = network.WLAN(network.STA_IF)
    wlan.active(True)
    if not wlan.isconnected():
        wlan.scan()
        print('connecting to network...')
        wlan.connect("royter", "traveller22")
        while not wlan.isconnected():
            pass
    print('network config:', wlan.ifconfig())

machine.Pin(2, machine.Pin.OUT).off()
do_connect('royter', 'traveller22')
gc.collect()
print('wifi connected')
