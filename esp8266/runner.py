import uasyncio

from microDNSSrv import dns
from services.userver import httpServer
from websockets.ws_sonar_server import WSServer
import services.sensorservice as sensorservice
import logger, services.blink as blinker


def run():
    try:
        dns()
        httpServer()
        service = sensorservice.run()
        WSServer(callback=service.callback).run()
        uasyncio.run(blinker.blink())
    except Exception as err:
        logger.error(err)
        raise err
