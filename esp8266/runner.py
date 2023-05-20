import gc

import uasyncio
import ulogging
import utime

import battery
import blink
import client
import deeper
import ipfinder
import pins
import sender
import switcher
import ultraServer


def run():
    loop = uasyncio.get_event_loop()
    try:
        loop.create_task(blink.run())
        loop.create_task(switcher.run())
        loop.create_task(deeper.run())
        loop.create_task(battery.run())
        # loop.create_task(ultraServer.start_server())
        loop.create_task(sender.run())
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


def start():
    start_time = utime.ticks_ms()
    client_ip = None
    wsClient = None
    while True:
        try:
            pins.switch(pins.GREEN)
            switcher.check(start_time)
            if client_ip is None:
                client_ip = ipfinder.get_client_ip()
                wsClient = client.connect("ws://" + client_ip + ":7070/sonar")
            ulogging.debug('execution time get ip: {}'.format(utime.ticks_diff(utime.ticks_ms(), start_time)))
            battery.save_battery_level()
            deeper.depth()
            ulogging.debug('execution time deeper: {}'.format(utime.ticks_diff(utime.ticks_ms(), start_time)))
            sender.sendWsSonarData(wsClient)
            execution_time_ms = utime.ticks_diff(utime.ticks_ms(), start_time)
            ulogging.debug('execution time: {}'.format(execution_time_ms))
            utime.sleep_ms(100 - execution_time_ms)
            start_time = utime.ticks_ms()
        except Exception as err:
            ulogging.info(str(err))
            client_ip = None
            wsClient = None
            pins.GREEN.off()
            pins.RED.on()
            utime.sleep(1)
            pins.RED.off()
        finally:
            gc.collect()
