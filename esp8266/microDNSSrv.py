import uasyncio, usocket, uselect
from re import match
import logger
import gc


class MicroDNSSrv:

    def Create(domainsList):
        global instance
        mds = MicroDNSSrv()
        if mds.SetDomainsList(domainsList) and mds.Start():
            return mds
        return None

    async def serve(self, host, port, backlog=5):
        # logger.debug('serve:' + str(host) + ':' + str(port))
        ai = usocket.getaddrinfo(host, port)[0]  # blocking!
        s = usocket.socket(usocket.AF_INET, usocket.SOCK_DGRAM)
        self.sock = s
        s.setblocking(False)
        s.bind(ai[-1])

        p = uselect.poll()
        p.register(s, uselect.POLLIN)
        to = self.polltimeout
        while True:
            try:
                if p.poll(to):
                    buf, addr = s.recvfrom(256)
                    #
                    ret = self.cb(buf)
                    # logger.info(ret)
                    # await uasyncio.sleep_ms(1)
                    if ret:
                        s.sendto(ret, addr)  # blocking
                    #
                await uasyncio.sleep_ms(1)
            except Exception as err:
                logger.error(err)
                # Shutdown server
                s.close()
                return

    def _ipV4StrToBytes(ipStr):
        try:
            parts = ipStr.split('.')
            if len(parts) == 4:
                return bytes([int(parts[0]),
                              int(parts[1]),
                              int(parts[2]),
                              int(parts[3])])
        except:
            pass
        return None

    def _getAskedDomainName(packet):
        try:
            queryType = (packet[2] >> 3) & 15
            qCount = (packet[4] << 8) | packet[5]
            if queryType == 0 and qCount == 1:
                pos = 12
                domName = ''
                while True:
                    domPartLen = packet[pos]
                    if (domPartLen == 0):
                        break
                    domName += ('.' if len(domName) > 0 else '') \
                               + packet[pos + 1: pos + 1 + domPartLen].decode()
                    pos += 1 + domPartLen
                return domName
        except:
            pass
        return None

    def _getPacketAnswerA(packet, ipV4Bytes):

        try:

            queryEndPos = 12
            while True:
                domPartLen = packet[queryEndPos]
                if (domPartLen == 0):
                    break
                queryEndPos += 1 + domPartLen
            queryEndPos += 5

            result = b''.join([
                packet[:2],  # Query identifier
                b'\x85\x80',  # Flags and codes
                packet[4:6],  # Query question count
                b'\x00\x01',  # Answer record count
                b'\x00\x00',  # Authority record count
                b'\x00\x00',  # Additional record count
                packet[12:queryEndPos],  # Query question
                b'\xc0\x0c',  # Answer name as pointer
                b'\x00\x01',  # Answer type A
                b'\x00\x01',  # Answer class IN
                b'\x00\x00\x00\x1E',  # Answer TTL 30 secondes
                b'\x00\x04',  # Answer data length
                ipV4Bytes])  # Answer data
            # logger.debug(result)
            return result
        except:
            pass

        return None

    def __init__(self):
        self._domList = {}
        self._started = False
        self.polltimeout = 1

    def _serverProcess(self):
        self._started = True
        while True:
            try:
                packet, cliAddr = self._server.recvfrom(256)
                packet = self.cd(packet)
                if packet:
                    self._server.sendto(packet, cliAddr)

                domName = MicroDNSSrv._getAskedDomainName(packet)
                if domName:
                    domName = domName.lower()
                    ipB = self._domList.get(domName, None)
                    if not ipB:
                        for domChk in self._domList.keys():
                            if domChk.find('*') >= 0:
                                r = domChk.replace('.', '\.').replace('*', '.*') + '$'
                                if match(r, domName):
                                    ipB = self._domList.get(domChk, None)
                                    break
                        if not ipB:
                            ipB = self._domList.get('*', None)
                    if ipB:
                        packet = MicroDNSSrv._getPacketAnswerA(packet, ipB)
                        if packet:
                            #
                            self._server.sendto(packet, cliAddr)

            except:
                if not self._started:
                    break

    def cb(self, packet):
        domName = MicroDNSSrv._getAskedDomainName(packet)
        # logger.debug(domName)
        if domName:
            domName = domName.lower()
            ipB = self._domList.get(domName, None)
            if not ipB:
                for domChk in self._domList.keys():
                    if domChk.find('*') >= 0:
                        r = domChk.replace('.', '\.').replace('*', '.*') + '$'
                        if match(r, domName):
                            ipB = self._domList.get(domChk, None)
                            break
                if not ipB:
                    ipB = self._domList.get('*', None)
            if ipB:
                return MicroDNSSrv._getPacketAnswerA(packet, ipB)

    def Start(self):
        uasyncio.create_task(self.serve('0.0.0.0', 53))
        return True

    def Stop(self):
        if self._started:
            self._started = False
            self._server.close()
            return True
        return False

    def IsStarted(self):
        return self._started

    def SetDomainsList(self, domainsList):
        if domainsList and isinstance(domainsList, dict):
            o = {}
            for dom, ip in domainsList.items():
                if isinstance(dom, str) and len(dom) > 0:
                    ipB = MicroDNSSrv._ipV4StrToBytes(ip)
                    if ipB:
                        o[dom.lower()] = ipB
                        continue
                break
            if len(o) == len(domainsList):
                self._domList = o
                return True
        return False


def dns():
    import network
    ap_if = network.WLAN(network.AP_IF)
    if MicroDNSSrv.Create({
        "test.com": ap_if.ifconfig()[0],
        "*test2.com": "2.2.2.2",
        "*google*": ap_if.ifconfig()[0],
        "*.toto.com": "192.168.4.1",
        "www.site.*": "192.168.4.1",
        "*connectivitycheck*": ap_if.ifconfig()[0]}):
        logger.debug("MicroDNSSrv started.")
    else:
        logger.debug("Error to starts MicroDNSSrv...")
