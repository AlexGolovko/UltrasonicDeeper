import machine

BLUE = machine.Pin(8, machine.Pin.OUT)
GREEN = machine.Pin(9, machine.Pin.OUT)
RED = machine.Pin(10, machine.Pin.OUT)
D0 = machine.Pin(2, machine.Pin.OUT)


def switch(pin):
    if pin.value():
        pin.off()
    else:
        pin.on()
