# This file is executed on every boot (including wake-boot from deepsleep)
import utime
import esp
import gc
import machine
import network
import pins

esp.osdebug(None)
pins.BLUE.on()
pins.D0.on()

machine.freq(160000000)


def do_create_apif():
    ssid = 'sonar'
    password = 'microsonar'
    ap_if = network.WLAN(network.AP_IF)
    ap_if.active(True)
    ap_if.config(essid=ssid, password=password, authmode=network.AUTH_WPA_WPA2_PSK)
    while not ap_if.active():
        pass
    print('Access Point created')
    print(ap_if.ifconfig())


def do_connect(wifi_name, wifi_pass):
    wlan = network.WLAN(network.STA_IF)
    wlan.active(True)
    utime.sleep_ms(200)
    if wifi_name in str(wlan.scan()):
        print('connecting to network...')
        wlan.connect(wifi_name, wifi_pass)
        while not wlan.isconnected():
            pass
        print('network config:', wlan.ifconfig())
        print('wifi connected')
    else:
        wlan.active(False)


def install_packages():
    do_connect('VseBudeUkraine', 'golalexser')
    try:
        import network
        wlan = network.WLAN(network.STA_IF)
        if wlan.isconnected():
            try:
                # import uasyncio
                import ulogging
                # import urequests
                return
            except Exception as err:
                print(err)
                import upip
                # upip.install('micropython-uasyncio')
                upip.install('micropython-ulogging')
                # upip.install('micropython-urequests')
    except Exception as err:
        print(err)


do_create_apif()
# do_connect('VseBudeUkraine', 'golalexser')
install_packages()
# try:
#     import webrepl
#     webrepl.start()
# except Exception as err:
#     print(err)
gc.collect()
pins.BLUE.off()
print('boot finished')
