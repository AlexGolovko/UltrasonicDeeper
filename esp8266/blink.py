import machine
import uasyncio
import pins

async def blink():
    while True:
        pins.GREEN.on()
        await uasyncio.sleep_ms(500)
        pins.GREEN.off()
        await uasyncio.sleep_ms(500)

def run():
    uasyncio.create_task(blink())
