import socket

import ulogging
import utime


def check_ip_address(ip_address):
    ulogging.debug('check ip: {}'.format(ip_address))
    sock = None
    try:
        sockaddr = socket.getaddrinfo(ip_address, 8080)[0][-1]
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect(sockaddr)
        sock.close()
        return True
    except Exception as e:
        sock.close()
        ulogging.info(str(e))
        if 'ECONNRESET' in str(e):
            return True
        return False


ip_address = '192.168.4.'


def any_connected():
    import network
    ap = network.WLAN(network.AP_IF)
    if ap.status('stations'):
        return True
    return False


def get_client_ip():
    if not any_connected():
        raise Exception('no client found')
    for i in range(2, 5):
        curr_ip = ip_address + str(i)
        if check_ip_address(curr_ip):
            return curr_ip
    raise Exception('no client found')
