import uasyncio
import ulogging

import blink
import deeper
import switcher
import ultraServer


def run():
    loop = uasyncio.get_event_loop()
    try:
        loop.create_task(blink.run())
        loop.create_task(switcher.run())
        loop.create_task(deeper.run())
        loop.create_task(ultraServer.start_server())
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
