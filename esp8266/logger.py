import sys
import ulogging




def error(err):
    sys.print_exception(err)


def debug(message):
    ulogging.debug(message)


def info(message):
    ulogging.info(message)
