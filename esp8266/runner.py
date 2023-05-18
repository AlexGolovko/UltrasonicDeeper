import uasyncio
import ulogging
import blink
from microDNSSrv import dns
from userver import httpServer
# from wsserver import WSServer
# from wsservice import WSService
import ultraServer, switcher, deeper


def run():
    loop = uasyncio.get_event_loop()
    try:
        import mqttpublisher
        loop.create_task(blink.blink())
        loop.create_task(switcher.run())
        loop.create_task(deeper.run())
        loop.create_task(ultraServer.start_server())
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
