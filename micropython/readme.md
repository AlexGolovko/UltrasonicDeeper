Install micropython 1.20.0:  
* pip install esptool
* hold boot, click en, stop holding boot
* python -m esptool --port COM3 erase_flash
* python -m esptool --chip esp32c3 --port COM3 --baud 460800 write_flash -z 0x0 venv/esp32c3-usb-20230426-v1.20.0.bin\

battery test:
2x1800 = 3600 mAh => 20h working hours, and only 4.2v - 3.4v range used because of xiao esp32c3 "bug"