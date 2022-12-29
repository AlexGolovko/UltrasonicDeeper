import uasyncio as asyncio
import ulogging as log

class WSService:
    subscribers = 0
    data = "Hello from Esp32-c3"

    def __init__(self, service):
        self.service = service
        asyncio.create_task(self.updateData())

    def sub(self):
        log.debug("subsctibe client on Service")
        self.subscribers += 1
        log.debug("subscribers count {}".format(self.subscribers))

    def unsub(self):
        log.debug("unsubscribe client on Servise")
        self.subscribers += -1
        log.debug("subscribers count {}".format(self.subscribers))

    def getData(self):
        log.debug("subscribers count {}".format(self.subscribers))
        return self.data

    async def updateData(self):
        while True:
            if self.subscribers < 1:
                log.debug("no active subscribers")
                await asyncio.sleep(3)
                continue
            log.debug("Updata data")
            data = self.service.callback()
            await asyncio.sleep_ms(200)
