import uasyncio, ulogging, pins, machine, math, ujson, sonicSensorA19, store


async def run():
    while True:
        try:
            depth()
        except Exception as err:
            ulogging.info(err)
        await uasyncio.sleep_ms(300)


def precise_depth():
    depths = [sonicSensorA19.measure_depth() for i in range(3)]
    if isCorrect(depths):
        store.depth = str(depths[0])
        store.status = 200
    else:
        store.depth = store.depth - 1
        store.status = 300


def depth():
    curr_depth = sonicSensorA19.measure_depth()
    if curr_depth == 0 or curr_depth > 37:
        store.depth = - 1
        store.status = 300
        return
    store.depth = str(curr_depth)
    store.status = 200


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
