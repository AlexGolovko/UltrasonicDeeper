import machine
import uasyncio

async def blink():
    led = machine.Pin(4, machine.Pin.OUT)
    while True:
        led.on()
        await uasyncio.sleep_ms(500)
        led.off()
        await uasyncio.sleep_ms(500)

def run():
    uasyncio.create_task(blink())
