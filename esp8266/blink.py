import machine
import uasyncio
import pins

async def run():
    while True:
        pins.GREEN.on()
        await uasyncio.sleep_ms(500)
        pins.GREEN.off()
        await uasyncio.sleep_ms(500)
