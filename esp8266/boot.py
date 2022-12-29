# This file is executed on every boot (including wake-boot from deepsleep)
import utime
import esp
import gc
import machine
import network

esp.osdebug(None)


# machine.freq(160000000)

def do_create_apif():
    ap_if = network.WLAN(network.AP_IF)
    ap_if.active(True)
    ap_if.config(authmode=network.AUTH_OPEN)
    while not ap_if.active():
        pass
    print('Access Point created')
    print(ap_if.ifconfig())


def do_connect(wifi_name, wifi_pass):
    wlan = network.WLAN(network.STA_IF)
    wlan.active(True)
    utime.sleep_ms(200)
    wlans = wlan.scan()
    if wifi_name in str(wlans):
        print('connecting to network...')
        wlan.connect(wifi_name, wifi_pass)
        while not wlan.isconnected():
            pass
        print('network config:', wlan.ifconfig())
    else:
        wlan.active(False)


def install_packages():
    try:
        import network
        wlan = network.WLAN(network.STA_IF)
        if wlan.isconnected():
            try:
                import uasyncio
                import ulogging
                return
            except Exception as err:
                print(err)
                import upip
                upip.install('micropython-uasyncio')
                upip.install('micropython-ulogging')
    except Exception as err:
        print(err)


machine.Pin(2, machine.Pin.OUT).off()
do_create_apif()
do_connect('royter', 'traveller22')
do_connect('VseBudeUkraine', 'golalexser')
install_packages()
try:
    import webrepl
    webrepl.start()
except Exception as err:
    print(err)
gc.collect()
print('wifi connected')
