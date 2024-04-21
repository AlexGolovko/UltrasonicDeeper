import machine

BLUE = machine.Pin(10, machine.Pin.OUT)
GREEN = machine.Pin(6, machine.Pin.OUT)
RED = machine.Pin(7, machine.Pin.OUT)
D0 = machine.Pin(0, machine.Pin.OUT)


def switch(pin):
    if pin.value():
        pin.off()
    else:
        pin.on()
