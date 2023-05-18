import uasyncio, ulogging, pins, machine, math, ujson, sonicSensorA19, store

async def run():
    import store
    while True:
        try:
            depths = [sonicSensorA19.measure_depth() for i in range(3)]
            if isCorrect(depths):
                store.depth = str(depths[0])
                store.status = 200
            else:
                store.depth = store.depth - 1
                store.status = 300
        except Exception as err:
            ulogging.info(err)
        await uasyncio.sleep(0.25)


def isCorrect(depths):
    for depth in depths:
        if depth == 0:
            return False
    deltas = [0 for i in range(3)]
    depthsLen = len(depths)
    for iter in range(depthsLen):
        if iter == (depthsLen - 1):
            deltas[iter] = math.fabs(depths[iter] - depths[0])
        else:
            deltas[iter] = math.fabs(depths[iter] - depths[iter + 1])
    for delta in deltas:
        if delta > 3:
            return False
    return True