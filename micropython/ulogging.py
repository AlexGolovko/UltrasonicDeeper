WARNING = 0
DEBUG = 1
INFO = 2
global log_level


def basicConfig(level):
    global log_level
    log_level = level


def info(err):
    print(str(err))


def debug(log):
    print(str(log))
