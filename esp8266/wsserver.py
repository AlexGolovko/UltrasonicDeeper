# userver.py Demo of simple uasyncio-based echo server

# Released under the MIT licence
# Copyright (c) Peter Hinch 2019-2020

import usocket as socket
import uasyncio as asyncio
import uselect as select
import ujson
import re
import uhashlib
import ubinascii
from protocol import Websocket
import ulogging as log


class WSServer:
    REQ_RE = re.compile(
    r'^(([^:/\\?#]+):)?' +  # scheme                # NOQA
    r'(//([^/\\?#]*))?' +   # user:pass@hostport    # NOQA
    r'([^\\?#]*)' +         # route                 # NOQA
    r'(\\?([^#]*))?' +      # query                 # NOQA
    r'(#(.*))?')            # fragment              # NOQA

    def __init__(self, host='0.0.0.0', port=43, backlog=5, timeout=20, service=None):
        self.host = host
        self.port = port
        self.backlog = backlog
        self.timeout = timeout
        self.service = service

    async def run(self):
        log.info('Awaiting client connection.')
        self.cid = 0
        self.server = await asyncio.start_server(self.run_client, self.host, self.port, self.backlog)
        while True:
            log.debug("alive")
            await asyncio.sleep(60)

    async def run_client(self, sreader, swriter):
        self.cid += 1
        cid = self.cid
        log.debug('Got connection from client {}'.format(cid))
        webkey = None
        try:
            webkey = await self.getWebKey(sreader)
            if not webkey:
                raise OSError
            respkey = self.make_respkey(webkey)
            await self.switchProtocol(swriter, respkey)
            self.service.sub()
            ws = Websocket(swriter)
            while True:
                await asyncio.sleep_ms(250)
                await ws.send(self.service.getData())
        except OSError as err:
            print(err)
            pass
        log.debug('Client {} disconnect.'.format(cid))
        self.service.unsub()
        await sreader.wait_closed()
        log.debug('Client {} socket closed.'.format(cid))

    async def switchProtocol(self, swriter, respkey):
        swriter.write(b"HTTP/1.1 101 Switching Protocols\r\n")
        swriter.write(b"Upgrade: websocket\r\n")
        swriter.write(b"Connection: Upgrade\r\n")
        swriter.write(b"Sec-WebSocket-Accept: " + respkey + b"\r\n")
        swriter.write(b"Server: Micropython\r\n")
        swriter.write(b"\r\n")
        await swriter.drain()

    async def getWebKey(self, sreader):
        while True:
            try:
                res = await asyncio.wait_for(sreader.readline(), self.timeout)
            except asyncio.TimeoutError:
                res = b''
            if res == b'':
                raise OSError
            log.info('Received {} from client {}'.format(
                res.rstrip(), self.cid))
            if res.rstrip().startswith(b'Sec-WebSocket-Key:'):
                webkey = res.rstrip().split(b":", 1)[1]
                webkey = webkey.strip()
                log.info('webkey {}'.format(webkey))
                break
        return webkey

    def make_respkey(self, webkey):
        d = uhashlib.sha1(webkey)
        d.update(b"258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
        respkey = d.digest()
        return ubinascii.b2a_base64(respkey).strip()

    async def close(self):
        log.info('Closing server')
        self.server.close()
        await self.server.wait_closed()
        log.info('Server closed.')
