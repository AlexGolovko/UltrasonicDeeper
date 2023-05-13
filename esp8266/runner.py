import uasyncio
import ulogging
import blink
from microDNSSrv import dns
from userver import httpServer
import sensorservice as sensorservice
# from wsserver import WSServer
# from wsservice import WSService


def run():
    loop = uasyncio.get_event_loop()
    try:
        import mqttpublisher
        loop.create_task(blink.blink())
        loop.create_task(mqttpublisher.mqtt_publisher())
        sensorservice.run()
        # dns()
        # httpServer()
        # WSServer(callback=service.callback).run()
        # service = WSService()
        # server = WSServer(service)
        # uasyncio.run(server.run())
        loop.run_forever()
    except Exception as err:
        ulogging.info(str(err))
        loop.stop()
        loop.close()
        raise err
    finally:
        loop.stop()
        loop.close()
        # asyncio.run(server.close())