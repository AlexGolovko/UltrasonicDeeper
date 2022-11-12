import uasyncio

from server import serve
import logger


# import sensor
# import math


class WSServer:
    def __init__(self,  callback):
        self.callback = callback

    def run(self):
        ws_server = serve(self.add_client, "0.0.0.0", 8080)
        uasyncio.create_task(ws_server)

    async def add_client(self, ws, path):
        logger.info("Connection on {}".format(path))

        try:
            while True:
                # dictResponse = {"event": "SONAR",
                #                 "data": {"status": 200, "depth": "4.20", "battery": "98",
                #                          "temperature": "24"}}
                await ws.send(self.callback())
                # await uasyncio.sleep_ms(1)
        finally:
            logger.debug("Disconnected")
