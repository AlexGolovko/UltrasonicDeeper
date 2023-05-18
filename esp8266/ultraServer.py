import uasyncio as asyncio
import ujson
import ulogging
import store
import switcher
import utime


def response():
    return {"status": str(store.status), "depth": str(store.depth), "battery": str(-1),
                    "temperature": str(store.ds_temperature)}


async def handle_client(reader, writer):
    try:
        curr_time = utime.ticks_ms()
        request_line = await reader.readline()
        ulogging.debug('Received {} from {}'.format(request_line.strip(), writer.get_extra_info('peername')))
        method, path, _ = request_line.decode().split(' ')

        if method == 'GET' and '/sonar' in path:
            switcher.reset()
            response_body = response()
            await send_response(writer, store.status, response_body)
        # elif method == 'POST' and path == '/hello':
        #     data = await reader.read()
        #     data = ujson.loads(data)
        #     response_body = {'message': 'Hello, POST request!', 'data': data}
        #     await send_response(writer, 200, response_body)
        else:
            await send_response(writer, 404, {'error': 'Not found'})
        ulogging.debug('response time: {}'.format(utime.ticks_ms() - curr_time))
        await writer.aclose()
    except Exception as err:
        await writer.aclose()

async def send_response(writer, status_code, body):
    response_body = ujson.dumps(body)
    response = 'HTTP/1.1 {} OK\r\nContent-Type: application/json\r\nContent-Length: {}\r\nAccess-Control-Allow-Origin: *\r\n\r\n{}'.format(
        status_code, len(response_body), str(response_body)
    )
    await writer.awrite(response)


async def start_server():
    server = await asyncio.start_server(handle_client, '0.0.0.0', 8080)
    # addr = server.sockets[0].getsockname()
    # print("Server started on", addr)
    await server.wait_closed()
