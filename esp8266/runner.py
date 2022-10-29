import uasyncio

from microDNSSrv import dns
from userver import httpServer
from ws_sonar_server import WSServer
import sensorservice as sensorservice
import logger, blink as blinker


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
