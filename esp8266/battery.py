from machine import Pin, ADC
import uasyncio

import store


async def run():
    while True:
        save_battery_level()
        await uasyncio.sleep(300)


def save_battery_level():
    store.battery = battery_level()


def battery_level():
    adc = ADC(Pin(5))
    adc.atten(ADC.ATTN_11DB)
    # range 0-4095 for 0-3.3V
    return adc.read()
