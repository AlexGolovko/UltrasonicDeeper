import json
import uasyncio, urequests
import ulogging
import utime

import store


async def run():
    while True:
        curr_time = utime.ticks_ms()
        try:
            sendSonarData()
        except Exception as err:
            ulogging.debug(str(err))
        ulogging.debug('send time: {}'.format(utime.ticks_ms() - curr_time))
        await uasyncio.sleep(0.3)


def sendSonarData(client_ip):
    urequests.post('http://' + client_ip + ':8080/sonar',
                   json=message())


def message():
    return json.dumps({"status": str(store.status), "depth": str(store.depth),
                       "battery": str(store.battery), "temperature": str(store.ds_temperature)})


def sendSonarDataLoop():
    import ulogging
    ulogging.basicConfig(level=ulogging.DEBUG)
    while True:
        curr_time = utime.ticks_ms()
        sendSonarData('192.168.4.2')
        ulogging.debug('send time: {}'.format(utime.ticks_ms() - curr_time))


def sendWsSonarData(wsclient):
    uasyncio.run(wsclient.send(message()))
