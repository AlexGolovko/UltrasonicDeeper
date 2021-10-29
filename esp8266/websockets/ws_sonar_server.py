import uasyncio

from websockets.server import serve
import logger
import ujson
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
            async for msg in ws:
                logger.info(msg)
                # dictResponse = {"event": "SONAR",
                #                 "data": {"status": 200, "depth": "4.20", "battery": "98",
                #                          "temperature": "24"}}
                await ws.send(self.callback())
        finally:
            logger.debug("Disconnected")
