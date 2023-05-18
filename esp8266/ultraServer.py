import uasyncio as asyncio
import ujson
import ulogging
import store
import switcher


def response():
    dictResponse = {"status": str(store.status), "depth": str(store.depth), "battery": str(-1),
                    "temperature": str(-1)}
    message = ujson.dumps(dictResponse)
    return message


async def handle_client(reader, writer):
    request_line = await reader.readline()
    ulogging.debug('Received {} from {}'.format(request_line.strip(), writer.get_extra_info('peername')))
    method, path, _ = request_line.decode().split(' ')

    if method == 'GET' and path == '/sonar':
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

    await writer.aclose()


# async def send_response(writer, status_code, body):
#     response = 'HTTP/1.1 {}\r\nContent-Type: application/json\r\n'.format(status_code)
#     response += ujson.dumps(body)
#     response += '\r\n'
#     ulogging.debug('Sending response: {}'.format(response))
#     await writer.awrite(response)

async def send_response(writer, status_code, body):
    response_body = ujson.dumps(body)
    response = 'HTTP/1.1 {} OK\r\nContent-Type: application/json\r\nContent-Length: {}\r\n\r\n{}'.format(
        status_code, len(response_body), response_body
    )
    await writer.awrite(response)


async def start_server():
    server = await asyncio.start_server(handle_client, '0.0.0.0', 8080)
    # addr = server.sockets[0].getsockname()
    # print("Server started on", addr)
    await server.wait_closed()
