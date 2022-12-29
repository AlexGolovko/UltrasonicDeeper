import uasyncio as asyncio
from microDNSSrv import dns
from userver import httpServer
import sensorservice as sensorservice
import logger, blink as blinker
from wsserver import WSServer
from wsservice import WSService


def run():
    try:
        # dns()
        # httpServer()
        # service = sensorservice.run()
        # WSServer(callback=service.callback).run()
        # uasyncio.run(blinker.blink())
        service = WSService()
        server = WSServer(service)
        asyncio.run(server.run())
    except Exception as err:
        logger.error(err)
        raise err
    finally:
        asyncio.run(server.close())
        _ = asyncio.new_event_loop()