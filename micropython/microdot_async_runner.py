import json

import uasyncio, utime

import battery
import deeper
import pins
import store
import switcher
from microdot_asyncio import Microdot
from microdot_asyncio_websocket import with_websocket

app = Microdot()


@app.route('/sonar')
@with_websocket
async def sonar(request, ws):
    while True:
        # data = await ws.receive()
        pins.switch(pins.GREEN)
        battery.save_battery_level()
        deeper.depth()
        store.deep_sleep_count = 0
        await ws.send(json.dumps({"status": str(store.status), "depth": str(store.depth),
                                  "battery": str(store.battery), "temperature": str(store.ds_temperature)}))
        print("all send")
        print(utime.time())
        # import machine
        # machine.idle()
        print(utime.time())
        await uasyncio.sleep_ms(200)
        print(utime.time())
        print("after sleep")


uasyncio.create_task(switcher.run())
pins.RED.on()
app.run()
