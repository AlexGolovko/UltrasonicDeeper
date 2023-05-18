import uasyncio as asyncio
import logger
import ulogging
import gc


class Server:

    def __init__(self, host='0.0.0.0', port=8123, backlog=5, timeout=20):
        self.host = host
        self.port = port
        self.backlog = backlog
        self.timeout = timeout

    async def run(self):
        # logger.debug('Awaiting client connection.')
        self.cid = 0
        self.server = await asyncio.start_server(self.handle_connection, self.host, self.port, self.backlog)
        while True:
            await asyncio.sleep(100)

    async def handle_connection(self, reader, writer):
        gc.collect()
        # Get HTTP request line
        data = await reader.readline()
        request_line = data.decode()
        # if not str(request_line).startswith('GET /generate_204'):
        #     await writer.aclose()
        #     logger.debug("is not a check")
        addr = writer.get_extra_info('peername')
        ulogging.debug('Received {} from {}'.format(request_line.strip(), addr))

        # Read headers
        headers = {}
        while True:
            gc.collect()
            line = await reader.readline()
            if line == b'\r\n': break
            frags = line.split(b':', 1)
            if len(frags) != 2:
                # logger.debug('Invalid request header:' + line)
                return
            headers[frags[0]] = frags[1].strip()
        ulogging.debug("Headers:" + str(headers))

        # Handle the request
        if len(request_line) > 0:
            response = 'HTTP/1.0 204 No Content\r\n'
            response += 'Content-Length: 0\r\n'
            response += 'Cross-Origin-Resource-Policy: cross-origin\r\n'
            response += 'Date: Fri, 12 May 2023 15:30:09 GMT\r\n\r\n'
            # response += ''
            # response += 'I am trying'
            # with open('index.html') as f:
            #     response += f.read()
            await writer.awrite(response)

        # Close the socket
        await writer.aclose()
        # logger.debug("client socket closed")

    async def close(self):
        logger.debug('Closing server')
        self.server.close()
        await self.server.wait_closed()
        logger.debug('Server closed.')


def httpServer():
    asyncio.create_task(Server(port=80).run())
